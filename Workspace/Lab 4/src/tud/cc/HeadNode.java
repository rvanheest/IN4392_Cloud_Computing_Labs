package tud.cc;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import amazonTests.Configurations;
import amazonTests.EC2CloudService;
import amazonTests.NodeDetails;


public class HeadNode 
	implements AutoCloseable
{
	
	public static final int HeadWorkerPort = 6048;
	public static final int HeadClientPort = 6049;	
	

	private final ServerSocket workerSocket;
	private final ServerSocket clientSocket;
	private final BlockingQueue<Task> jobQueue = new java.util.concurrent.LinkedBlockingDeque<Task>();
	private final ConcurrentHashMap<InetAddress, WorkerHandle> workerPool = new ConcurrentHashMap<>();
	
	
	public HeadNode() throws IOException
	{
		System.out.println("Initialising head node...");
		this.workerSocket = new ServerSocket(HeadWorkerPort);
		System.out.println("Listening for workers at " + HeadWorkerPort);
		this.clientSocket = new ServerSocket(HeadClientPort);
		System.out.println("Listening for clients at " + HeadClientPort);
	}
	
	
	/**
	 * AutoCloseable implementation
	 */
	@Override
	public void close() throws IOException
	{
		if (this.workerSocket != null)
			this.workerSocket.close();
		if (this.clientSocket != null)
			this.clientSocket.close();
		
		System.out.println("Head node closed");
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
	
	
	public void acceptAndFeed()
	{
		try (Socket clientSocket = workerSocket.accept();
			 ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
			 ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) 
		{
			System.out.println("Accepted connection: " + clientSocket.getInetAddress().getCanonicalHostName());
			
			BufferedImage[] images = new BufferedImage[]
			{
				ImageIO.read(new File("images/Eiger.JPG")),
				//ImageIO.read(new File("images/Apen.JPG"))
			};
			
			for (BufferedImage image : images) 
			{
    			byte[] imageBytes = toByteArray(image);
    			System.out.println("SERVER - bytes: " + imageBytes.length);
    			
    			out.writeObject(imageBytes);
    			System.out.println("SERVER - send image");
			}
			
			for (int i = 0; i < images.length; i++)
			{
    			byte[] resultBytes = (byte[]) in.readObject();
    			System.out.println("SERVER - received bytes: " + resultBytes.length);
    			
    			BufferedImage result = toBufferedImage(resultBytes);
    			System.out.println("SERVER - received image: " + result);
    			
    			File output = new File("testing/Result" + i + ".JPG");
    			ImageIO.write(result, "JPG", output);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * This receives jobs asynchronously from the client.
	 * Incoming jobs are queued in the job queue.
	 * 
	 * @author Chris
	 *
	 */
	public static class ClientReceptionThread
		extends Thread
		implements AutoCloseable
	{
		private final ServerSocket socket;
		private final BlockingQueue<Task> jobQueue;
		
		public ClientReceptionThread(int port, BlockingQueue<Task> jobQueue) throws IOException
		{
			this.socket = new ServerSocket(port);
			this.jobQueue = jobQueue;
		}
		
		@Override
		public void run()
		{
			throw new UnsupportedOperationException("ReceptionThread.run");
			
				// TODO Accept connection
				
				// TODO Read image
				
				// TODO Queue task + connection
		}

		@Override
		public void close() throws Exception
		{
			this.socket.close();
			
		}
	}
	
	
	/**
	 * Accepts connections from new workers
	 * 
	 * @author Chris
	 *
	 */
	public static class WorkerReceptionThread
		extends Thread
		implements AutoCloseable
	{
		private final ServerSocket socket;
		private final ConcurrentHashMap<InetAddress, WorkerHandle> workerPool;
		
		public WorkerReceptionThread(int port, ConcurrentHashMap<InetAddress, WorkerHandle> workerPool) throws IOException
		{
			this.socket = new ServerSocket(port);
			this.workerPool = workerPool;
		}
		
		
		@Override
		public void run()
		{
			//throw new UnsupportedOperationException();
			
			// Loop: accept connections from workers
			while (true)
			{
				try (Socket clientSocket = socket.accept();
					 ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
					 ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) 
				{
					InetAddress workerAddress = clientSocket.getInetAddress();
					System.out.println("Accepted worker connection: " + clientSocket.getInetAddress().getCanonicalHostName());
					
					// TODO Create handle
					WorkerHandle handle = new WorkerHandle(clientSocket);
					handle.start();
					
					// TODO Add to worker pool
					workerPool.put(workerAddress, handle);
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
		}
	}
	
	
	public static class SchedulerThread
		extends Thread
	{
		private final BlockingQueue<Task> jobQueue;
		// TODO worker pool
		
		
		public SchedulerThread(BlockingQueue<Task> jobQueue/*TODO worker pool*/)
		{
			this.jobQueue = jobQueue;
		}
		
		
		@Override
		public void run()
		{
			throw new UnsupportedOperationException("SchedulerThread.run");
			
			// TODO Take job from queue
			
			// TODO Send to worker
		}
	}
	
	
	/**
	 * Handles input from one worker
	 * @author Chris
	 */
	public static class WorkerHandle
		extends Thread
		implements AutoCloseable
	{
		
		private final Socket workerSocket;
		private final ObjectInputStream in;
		private final ObjectOutputStream out;
		
		public WorkerHandle(Socket workerSocket) throws IOException
		{
			this.workerSocket = workerSocket;
			this.in = new ObjectInputStream(workerSocket.getInputStream());
			this.out = new ObjectOutputStream(workerSocket.getOutputStream());
		}
		
		@Override
		public void run()
		{
			throw new UnsupportedOperationException("WorkerHandle.run");
			
			// TODO read processed jobs from worker
			
			// TODO queue response to client --> asynchronous because clients are unreliable
		}

		@Override
		public void close() throws Exception 
		{
			this.in.close();
			this.out.close();
			this.workerSocket.close();
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
	
	
	public static void beHead()
	{
		try (HeadNode head = new HeadNode();)
		{
			NodeDetails workerDetails = head.startWorker();
			System.out.println("Leased: " + workerDetails);
			head.acceptAndFeed();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args)
	{
		switch (args[0])
		{
			case "head":
				HeadNode.beHead();
				break;
			case "worker":
				//beWorker(args[1]); //TODO remove hardcoding
				WorkerNode.beWorker(args[1]);
				break;
			default:
				System.err.println("Unknown role: " + args[0]);
				return;
		}
	}
	
}
