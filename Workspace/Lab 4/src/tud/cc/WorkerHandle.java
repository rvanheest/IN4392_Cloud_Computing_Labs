package tud.cc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;

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
	
	/**
	 * Contains the jobs currently being handled by this worker
	 * as a set of (jobID -> imageSize) tuples
	 */
	private final Map<UUID, Integer> jobsInProcess = Collections.synchronizedMap(new HashMap<UUID, Integer>());
	
	
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
				this.jobsInProcess.remove(job.getUuid());
				
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
	
	
	/**
	 * Get a snapshot of the workers current workload.
	 * The data stop being up-to-date as soon as the function returns.
	 * @return The snapshot
	 */
	public Map<UUID, Integer> getWorkloadSnapshot()
	{
		return new HashMap<UUID, Integer>(this.jobsInProcess);
	}
	
	
	/**
	 * Send a job to be executed by this worker.
	 * This function blocks on the calling thread.
	 * @param job The job to sent.
	 * @throws IOException The connection to the worker has failed.
	 */
	public synchronized void sendJob(Task job) throws IOException
	{
		if (job == null)
			throw new NullPointerException("Job cannot be null");
		
		System.out.println(Thread.currentThread().getName() +  " sending job to " + workerSocket.getInetAddress().getHostAddress());

		this.jobsInProcess.put(job.getUuid(), job.getImage().length);
		
		// Send job
		out.writeObject(job);
	}
	
	
	/**
	 * Release the previously leased EC2 instance
	 */
	public void release() 
	{
		System.out.println("Releasing node " + nodeDetails.getNodeAddress().getHostAddress());
		HeadNode.getService().releaseNode(nodeDetails);
	}
	

	/**
	 * Close socket, remove worker from local collections and release the machine.
	 */
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
	
	
	@Override
	public String toString() 
	{
		int bs = 0;
		for (Integer i : this.jobsInProcess.values())
			bs += i;
		
		return this.workerSocket.getInetAddress().getHostAddress() + ": " + this.jobsInProcess.size() + " jobs, " + bs + " bytes";
	}
}

