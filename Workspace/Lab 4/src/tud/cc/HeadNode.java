package tud.cc;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import amazonTests.Configurations;
import amazonTests.EC2CloudService;
import amazonTests.NodeDetails;
import data.Request;
import emulator.Emulator;


abstract class CloseableThread
	extends Thread
	implements AutoCloseable
{
	public CloseableThread()
	{
	}
	
	public CloseableThread(String name)
	{
		super(name);
	}
}



public class HeadNode 
	implements AutoCloseable
{
	
	public static final int HeadWorkerPort = 6048;
	public static final int HeadClientPort = 6049;	
	

//	private final ServerSocket workerSocket;
//	private final ServerSocket clientSocket;
	private final BlockingQueue<Task> jobQueue = new java.util.concurrent.LinkedBlockingDeque<Task>();
	private final BlockingQueue<Task> processed = new java.util.concurrent.LinkedBlockingDeque<Task>();
	private final ConcurrentHashMap<InetAddress, WorkerHandle> workerPool = new ConcurrentHashMap<>();
	
	private ArrayList<CloseableThread> threads = new ArrayList<>();
	
	
	public HeadNode() throws IOException
	{
		System.out.println("Initialising head node...");
		
		// TODO Start all the threads
		threads.add(new HeadNode.WorkerReceptionThread(HeadWorkerPort, workerPool));
		threads.add(new HeadNode.ClientReceptionThread(HeadClientPort, jobQueue));
		threads.add(new HeadNode.SchedulerThread(jobQueue, workerPool));
		// TODO response thread
		// TODO monitor thread
		for (Thread t : threads)
			t.start();
	}
	
	
	/**
	 * AutoCloseable implementation
	 */
	@Override
	public void close() throws IOException
	{
		// Close every thread
		for (CloseableThread t : threads)
		{
			try 
			{
				t.close();
			} catch (Exception e) {	}
		}
		
		System.out.println("Head node closed");
	}


	/**
	 * A blocking function that starts a command-line loop on this thread.
	 */
	public void runCommandLine() 
	{
		try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in)))
		{
			System.out.println("Command-line started");
			boolean isCl = true; 
			while (isCl)
			{
				System.out.print("> ");
				String command = in.readLine().trim();
				switch (command)
				{
					case "workers":
						for (InetAddress inet : workerPool.keySet())
							System.out.println(inet.getCanonicalHostName());
						break;
					case "queue":
						for (Task job : jobQueue)
							System.out.println(job);
						break;
					case "ping":
						System.out.println("pong");
						break;
					case "break":
						isCl = false;
						break;
					default:
						System.out.println("Unknown command " + command);
						break;
				}
			}
			System.out.println("Command-line ended");
		}
		catch (Exception e)	{ }
	}
	
	
	/**
	 * Deploys a new node and returns the IP of the node
	 * @return The details of the node just deployed
	 */
	public NodeDetails startWorker()
	{
		System.out.println("Leasing new worker...");
		
		ExecutorService service = Executors.newFixedThreadPool(8);
		EC2CloudService cloudService = new EC2CloudService("AwsCredentials.properties", "CC", "ec2.eu-west-1.amazonaws.com", service);
		
		NodeDetails details = cloudService.leaseNode(new Configurations("random", null));
		
		return details;
	}
	
	
	/**
	 * This receives jobs asynchronously from the client.
	 * Incoming jobs are queued in the job queue.
	 * 
	 * @author Chris
	 *
	 */
	public static class ClientReceptionThread
		extends CloseableThread
	{
		private final ServerSocket socket;
		private final BlockingQueue<Task> jobQueue;
		
		public ClientReceptionThread(int port, BlockingQueue<Task> jobQueue) throws IOException
		{
			super("ClientReception");
			this.socket = new ServerSocket(port);
			this.jobQueue = jobQueue;
		}
		
		@Override
		public void run()
		{
			System.out.println(getName() + " started");
			
			// Accept connection
			try (Socket clientSocket = socket.accept();
				 ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
				 ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) 
			{
				System.out.println("Accepted client connection: " + clientSocket.getInetAddress().getCanonicalHostName());
				
				// Start accepting images
				while (true)
				{
					// Get request
					Request request = (Request) in.readObject();
	    			System.out.println(getName() + " received request " + request.getImage().length + "b");
	
	    			// TODO Image backup
	    			
					// Queue task
	    			Task task = new Task(request.getId(), request.getImage());
	    			task.queued();
	    			jobQueue.add(task);			
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		@Override
		public void close() throws Exception
		{
			this.socket.close();
			
			System.out.println(getName() + " closed.");
		}
	}
	
	
	/**
	 * Accepts connections from new workers
	 * 
	 * @author Chris
	 *
	 */
	public static class WorkerReceptionThread
		extends CloseableThread
	{
		private final ServerSocket socket;
		private final ConcurrentHashMap<InetAddress, WorkerHandle> workerPool;
		
		public WorkerReceptionThread(int port, ConcurrentHashMap<InetAddress, WorkerHandle> workerPool) throws IOException
		{
			super("WorkerReception");
			this.socket = new ServerSocket(port);
			this.workerPool = workerPool;
		}
		
		
		@Override
		public void run()
		{
			System.out.println(getName() + " started");
			
			// Loop: accept connections from workers
			while (true)
			{
				try
				{
					Socket clientSocket = socket.accept(); // The worker handle will close this
							
					InetAddress workerAddress = clientSocket.getInetAddress();
					System.out.println("Accepted worker connection: " + clientSocket.getInetAddress().getCanonicalHostName());
					
					// Create handle
					WorkerHandle handle = new WorkerHandle(clientSocket);
					handle.start();
					
					// Add to worker pool
					workerPool.put(workerAddress, handle);
					
					System.out.println("Worker added to worker pool.");
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}


		@Override
		public void close() throws Exception 
		{
			this.socket.close();
			
			System.out.println(getName() + " closed.");
		}
	}
	
	
	public static class SchedulerThread
		extends CloseableThread
	{
		private final BlockingQueue<Task> jobQueue;
		private final Map<InetAddress, WorkerHandle> workerPool;
		
		
		public SchedulerThread(BlockingQueue<Task> jobQueue, Map<InetAddress, WorkerHandle> workerPool)
		{
			super("Scheduler");
			this.jobQueue = jobQueue;
			this.workerPool = workerPool;
		}
		
		
		@Override
		public void run()
		{
			System.out.println(getName() + " started");
			
			while (true)
			{
				try
				{
					if ( workerPool.values().size() < 1 )
					{	// No workers
						try {
							sleep(5000);
						} catch (InterruptedException e) {}
						continue;
					}
					
					// Take job from queue
					Task job = jobQueue.take();
					
					// TODO Send to worker
					workerPool.values().toArray(new WorkerHandle[0])[0].sendJob(job);
				}
				catch (IOException | InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}


		@Override
		public void close() throws Exception 
		{
			// TODO Auto-generated method stub
			System.out.println(getName() + " closed.");	
		}
	}
	
	
	/**
	 * Handles input from one worker
	 * @author Chris
	 */
	public static class WorkerHandle
		extends CloseableThread
	{
		
		private final Socket workerSocket;
		private final ObjectInputStream in;
		private final ObjectOutputStream out;
		
		public WorkerHandle(Socket workerSocket) throws IOException
		{
			super("WorkerHandle");
			this.workerSocket = workerSocket;
			this.in = new ObjectInputStream(workerSocket.getInputStream());
			this.out = new ObjectOutputStream(workerSocket.getOutputStream());
		}
		
		@Override
		public void run()
		{
			System.out.println(getName() + " started");
			
			try
			{
				while (true)
				{
					// TODO read processed jobs from worker
					byte[] image = (byte[]) in.readObject();
					System.out.println(getName() + ": received from worker: " + image.length + "b");
					
					// TODO queue response to client --> asynchronous because clients are unreliable
				}
			}
			catch (Exception e)
			{
				 e.printStackTrace();
			}
		}
		
		
		public synchronized void sendJob(Task job) throws IOException
		{
			if (job == null)
				throw new NullPointerException("Job cannot be null");
			
			System.out.println("Sending job to " + workerSocket.getInetAddress().getCanonicalHostName());
			
			// Send job
			// TODO Send job with ids
			out.writeObject(job);
		}
		

		@Override
		public void close() throws Exception 
		{
			this.in.close();
			this.out.close();
			this.workerSocket.close();
			
			System.out.println(getName() + " closed.");
		}
	}
	
	
	public static byte[] toByteArray(BufferedImage image) throws IOException {
		try (ByteArrayOutputStream outbytes = new ByteArrayOutputStream()) {
			ImageIO.write(image, "JPG", outbytes);
			return outbytes.toByteArray();
		}
	}

	public static BufferedImage toBufferedImage(byte[] bytes) throws IOException {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
			return ImageIO.read(bais);
		}
	}
	
	
	public static void beHead(boolean noChild)
	{
		try (HeadNode head = new HeadNode();)
		{
			if (!noChild)
			{
				NodeDetails workerDetails = head.startWorker();
				System.out.println("Leased: " + workerDetails);
			}
//			head.acceptAndFeed();
			head.runCommandLine();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) throws UnknownHostException, IOException
	{
		switch (args[0])
		{
			case "head":
				boolean nochild = false;
				if (args.length > 1)
					nochild = args[1].equals("nochild");
				HeadNode.beHead(nochild);
				break;
			case "worker":
				//beWorker(args[1]); //TODO remove hardcoding
				WorkerNode.beWorker(args[1]);
				break;
			case "emulator":
				Emulator emu = new Emulator(args[1], new File(args[2]));
				break;
			default:
				System.err.println("Unknown role: " + args[0]);
				return;
		}
	}
	
}
