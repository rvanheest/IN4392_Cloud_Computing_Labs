package tud.cc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.Queue;

import tud.cc.WorkerHandle;
import amazonTests.NodeDetails;
import data.Task;

/**
 * Handles input from one worker
 * @author Chris
 */
public class WorkerHandle
	extends CloseableThread
{
	private boolean closing = false;
	
	private final NodeDetails nodeDetails;
	private final Socket workerSocket;
	private final ObjectInputStream in;
	private final ObjectOutputStream out;
	private final Queue<Task> processedQueue;
	private final Map<String, WorkerHandle> workerPool;
	
	
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
			while (!closing)
			{
				// Read processed jobs from worker
				Task job = (Task) in.readObject();
				System.out.println(getName() + ": received from worker: " + job.getImage().length + "b");
				job.processed();
				
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
		HeadNode.getService().releaseNode(nodeDetails);
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