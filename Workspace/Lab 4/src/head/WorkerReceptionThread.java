package head;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Deque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import amazonTests.NodeDetails;
import data.Task;

/**
 * Accepts connections from new workers
 * 
 * @author Chris
 *
 */
public class WorkerReceptionThread
	extends CloseableThread
{
	private boolean closing = false;
	
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
		try
		{
			while (!closing)
			{
				try
				{
					Socket clientSocket = socket.accept(); // The worker handle will close this
							
					InetAddress workerAddress = clientSocket.getInetAddress();
					System.out.println("Accepted worker connection: " + clientSocket.getInetAddress().getHostAddress());
					
					// Create handle
					NodeDetails nodeDetails = workerDetails.get(clientSocket.getInetAddress().getHostAddress());
					if (nodeDetails == null)
						throw new Exception("Unknown host tried to connect on worker port");
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
					if (e instanceof InterruptedException)
						throw (InterruptedException)e;
					
					if (!closing)
						e.printStackTrace();
				}
			}
		}
		catch (InterruptedException e)
		{
			if (!this.closing)
				e.printStackTrace();
		}
	}


	@Override
	public void close() throws Exception 
	{
		this.closing = true;
		this.interrupt();
		
		this.socket.close();
		
		System.out.println(getName() + " closed.");
	}
}