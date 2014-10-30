package tud.cc;

import java.io.BufferedReader;
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
import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import scheduler.*;
import amazonTests.Configurations;
import amazonTests.EC2CloudService;
import amazonTests.NodeDetails;
import data.Request;
import data.Task;
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
		super("[T-" + name + "]");
	}
}



class Connection
	implements AutoCloseable
{
	public final Socket socket;
	public final ObjectOutputStream out;
	public final ObjectInputStream in;
	
	public Connection(Socket socket) throws IOException
	{
		this.socket = socket;
		this.in = new ObjectInputStream(socket.getInputStream());
		this.out = new ObjectOutputStream(socket.getOutputStream());
	}

	@Override
	public void close() throws Exception 
	{
		this.in.close();
		this.out.close();
		this.socket.close();
	}
	
	public void send(Object o) throws IOException
	{
		synchronized (out)
		{
			out.writeObject(o);
		}
	}
	
	public <T> T receive() throws ClassNotFoundException, IOException
	{
		synchronized (in)
		{
			T readObject = (T) this.in.readObject();
			return readObject;
		}
	}
	
	@Override
	public int hashCode() 
	{
		return Objects.hash(socket, out, in);
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		if (obj instanceof Connection)
		{
			Connection other = (Connection) obj;
			return this.socket.equals(other.socket);
		}
		return false;
	}
}



public class HeadNode 
	implements AutoCloseable
{
	
	public static final int HeadWorkerPort = 6048;
	public static final int HeadClientPort = 6049;	
	
	private static ExecutorService _service = null;
	private static EC2CloudService _cloudService = null;
	private static EC2CloudService getService()
	{
		if (_cloudService == null)
		{
			 _service = Executors.newFixedThreadPool(8);
			 _cloudService = new EC2CloudService("AwsCredentials.properties", "CC", "ec2.eu-west-1.amazonaws.com", _service);
		}
		return _cloudService;
	}

//	private final ServerSocket workerSocket;
//	private final ServerSocket clientSocket;
	private final BlockingQueue<Task> jobQueue = new java.util.concurrent.LinkedBlockingDeque<Task>();
	private final BlockingQueue<Task> processed = new java.util.concurrent.LinkedBlockingDeque<Task>();
	private final ConcurrentHashMap<String, WorkerHandle> workerPool = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<String, NodeDetails> workerDetails = new ConcurrentHashMap<>();
	private final Map<UUID, ClientHandle> requestMap = new ConcurrentHashMap<>();	
	
	
	private Deque<CloseableThread> threads = new LinkedBlockingDeque<>();
	
	
	public HeadNode() throws IOException
	{
		System.out.println("Initialising head node...");
		
		// Start all the threads
		threads.add(new HeadNode.WorkerReceptionThread(HeadWorkerPort, workerPool, processed, threads, workerDetails));
		threads.add(new HeadNode.ClientReceptionThread(HeadClientPort, jobQueue, threads, requestMap));
		threads.add(new HeadNode.SchedulerThread(jobQueue, workerPool));
		threads.add(new HeadNode.Responder(processed, requestMap));
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
				try
				{
					System.out.print("> ");
					String[] tokens = in.readLine().split("\\s");
					String command = tokens[0];
					switch (command)
					{
						case "workers":
							for (String inet : workerPool.keySet())
								System.out.println(inet);
							break;
						case "worker-details":
							for (String details : workerDetails.keySet())
								System.out.println(details);
							break;
						case "queue":
							for (Task job : jobQueue)
								System.out.println(job);
							break;
						case "processed":
							for (Task job : processed)
								System.out.println(job);
							break;
						case "lease":
							System.out.println("Leasing a new worker...");
							NodeDetails workerDetails = startWorker();
							System.out.println("Leased: " + workerDetails);
							break;
						case "release":
							WorkerHandle handle = workerPool.get(tokens[1]);
							handle.close();
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
				catch (Exception e)
				{
					e.printStackTrace();
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
		NodeDetails details = getService().leaseNode(new Configurations("random", null));
		this.workerDetails.put(details.getNodePrivateIP().getHostAddress(), details);
		
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
		private final Deque<CloseableThread> threads;
		private final Map<UUID, ClientHandle> requestMap;
		
		public ClientReceptionThread(int port, BlockingQueue<Task> jobQueue, Deque<CloseableThread> threads, Map<UUID, ClientHandle> requestMap) throws IOException
		{
			super("ClientReception");
			this.socket = new ServerSocket(port);
			this.jobQueue = jobQueue;
			this.threads = threads;
			this.requestMap = requestMap;
		}
		
		@Override
		public void run()
		{
			System.out.println(getName() + " started");
			
			while (true)
			{
				// Accept connection
				try 
				{
					Connection connection = new Connection(socket.accept());
					
					System.out.println("Accepted client connection: " + connection.socket.getInetAddress().getHostAddress());
					
					ClientHandle handle = new ClientHandle(connection, jobQueue, requestMap);
					threads.add(handle);
					handle.start();
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
	
	
	/**
	 * Handles input from one client
	 * @author Chris
	 */
	public static class ClientHandle
		extends CloseableThread
	{
		
		private final Connection connection;
		private final Queue<Task> jobQueue;
		private final Map<UUID, ClientHandle> requestMap;
		
		
		public ClientHandle(Connection connection, Queue<Task> jobQueue, Map<UUID, ClientHandle> requestMap) throws IOException
		{
			super("ClientHandle");
			this.connection = connection;
			this.jobQueue = jobQueue;
			this.requestMap = requestMap;
		}
		
		@Override
		public void run()
		{
			System.out.println(getName() + " started");
			
			try
			{
				// Start accepting images
				while (true)
				{
					// Get request
					Request request = connection.receive();
	    			System.out.println(getName() + " received request " + request.getImage().length + "b");
	
	    			// TODO Image backup
	    			
					// Queue task
	    			Task task = new Task(request.getId(), request.getImage());
	    			requestMap.put(request.getId(), this);
	    			
	    			jobQueue.add(task);
	    			task.queued();	
				}
			}
			catch (Exception e)
			{
				 e.printStackTrace();
			}
		}
		
		
		/**
		 * Sends response to the client.
		 * Blocks on the calling thread.
		 * 
		 * @param response The response object
		 * @throws IOException
		 */
		public synchronized void sendResponse(Request response) throws IOException
		{
			if (response == null)
				throw new NullPointerException("Response cannot be null");
			
			System.out.println(getName() + " sending response to " + connection.socket.getInetAddress().getHostAddress());
			requestMap.remove(response.getId());
			
			this.connection.send(response);
		}
		

		@Override
		public void close() throws Exception 
		{
			this.connection.close();
			
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
		private final ConcurrentHashMap<String, WorkerHandle> workerPool;
		private final Queue<Task> processed;
		private final Deque<CloseableThread> threads;
		private final Map<String, NodeDetails> workerDetails;
		
		public WorkerReceptionThread(
				int port,
				ConcurrentHashMap<String, WorkerHandle> workerPool,
				Queue<Task> processed, Deque<CloseableThread> threads,
				Map<String, NodeDetails> workerDetails
				) throws IOException
		{
			super("WorkerReception");
			this.socket = new ServerSocket(port);
			this.workerPool = workerPool;
			this.processed = processed;
			this.threads = threads;
			this.workerDetails = workerDetails;
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
					System.out.println("Accepted worker connection: " + clientSocket.getInetAddress().getHostAddress());
					
					// Create handle
					NodeDetails nodeDetails = workerDetails.get(clientSocket.getInetAddress().getHostAddress());
					WorkerHandle handle = new WorkerHandle(nodeDetails, clientSocket, processed, workerPool);
					threads.add(handle);
					handle.start();
					
					// Add to worker pool
					workerPool.put(workerAddress.getHostAddress(), handle);
					workerDetails.remove(clientSocket.getInetAddress().getHostAddress());
					
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
		private final Scheduler scheduler = new RandomScheduler(new Random());
		
		private final BlockingQueue<Task> jobQueue;
		private final Map<String, WorkerHandle> workerPool;
		
		
		public SchedulerThread(BlockingQueue<Task> jobQueue, Map<String, WorkerHandle> workerPool)
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
					ArrayList<Task> tasks = new ArrayList<>();
					tasks.add(jobQueue.take());
					Task nextTask = null;
					while ((nextTask = jobQueue.poll()) != null)
						tasks.add(nextTask);
					
					// Schedule
					Map<Task, WorkerHandle> mapping = scheduler.schedule(tasks, workerPool.values());
					
					// Send to worker
					for (Entry<Task, WorkerHandle> entry : mapping.entrySet())
					{
						Task task = entry.getKey();
						WorkerHandle handle = entry.getValue();
						handle.sendJob(task);
					}
				}
				catch (InterruptedException | SchedulerException | IOException e)
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
		private final NodeDetails nodeDetails;
		private final Socket workerSocket;
		private final ObjectInputStream in;
		private final ObjectOutputStream out;
		private final Queue<Task> processedQueue;
		private final Map<String, WorkerHandle> workerPool;
		
		private boolean closing = false;
		
		public WorkerHandle(NodeDetails nodeDetails, Socket workerSocket, Queue<Task> processedQueue, Map<String, WorkerHandle> workerPool) throws IOException
		{
			super("WorkerHandle");
			
			if (nodeDetails == null)
				throw new NullPointerException(getName() + " requires the NodeDetails");
			
			this.nodeDetails = nodeDetails;
			this.workerSocket = workerSocket;
			this.in = new ObjectInputStream(workerSocket.getInputStream());
			this.out = new ObjectOutputStream(workerSocket.getOutputStream());
			this.processedQueue = processedQueue;
			this.workerPool = workerPool;
		}
		
		@Override
		public void run()
		{
			System.out.println(getName() + " started");
			
			try
			{
				while (true)
				{
					// Read processed jobs from worker
					Task job = (Task) in.readObject();
					System.out.println(getName() + ": received from worker: " + job.getImage().length + "b");
					
					// Queue response to client
					processedQueue.add(job);
				}
			}
			catch (Exception e)
			{
				if (!closing)
					e.printStackTrace();
			}
		}
		
		
		public synchronized void sendJob(Task job) throws IOException
		{
			if (job == null)
				throw new NullPointerException("Job cannot be null");
			
			System.out.println("Sending job to " + workerSocket.getInetAddress().getHostAddress());
			
			// Send job
			out.writeObject(job);
		}
		
		
		public void release() 
		{
			System.out.println("Releasing node " + nodeDetails.getNodeAddress().getHostAddress());
			getService().releaseNode(nodeDetails);
		}
		

		@Override
		public void close() throws Exception 
		{
			this.closing = true;
			this.interrupt();
			
			workerPool.remove(this.nodeDetails.getNodePrivateIP().getHostAddress());
			
			this.in.close();
			this.out.close();
			this.workerSocket.close();
			
			release();
			
			System.out.println(getName() + " closed.");
		}
	}
	
	
	public static class Responder
		extends CloseableThread
	{
		private final BlockingQueue<Task> processed;
		private final Map<UUID, ClientHandle> requestMap;
		
		
		public Responder(BlockingQueue<Task> processed, Map<UUID, ClientHandle> requestMap)
		{
			super("Responder");
			this.processed = processed;
			this.requestMap = requestMap;
		}
		

		@Override
		public void run() 
		{
			try 
			{
				while (true)
				{
					try
					{
						Task task = processed.take();
						
						UUID requestUuid = task.getRequestUuid();
						Request response = new Request(requestUuid, task.getImage());
						
						ClientHandle handle = requestMap.get(requestUuid);
						handle.sendResponse(response);
					}
					catch (IOException e) 
					{
						e.printStackTrace();
					}
				}
			} 
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		@Override
		public void close() throws Exception 
		{
		}
		
	}
	
	
	public static void beHead(boolean noChild)
	{
		try (HeadNode head = new HeadNode();)
		{
//			if (!noChild)
//			{
//				NodeDetails workerDetails = head.startWorker();
//				System.out.println("Leased: " + workerDetails);
//			}
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
				Emulator.beEmulator(args[1], new File(args[2]));
				break;
			default:
				System.err.println("Unknown role: " + args[0]);
				return;
		}
	}
	
}
