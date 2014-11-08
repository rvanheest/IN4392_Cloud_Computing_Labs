package head;

import imageProcessing.worker.WorkerHandshake;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import amazonTests.NodeDetails;
import data.Task;

/**
 * Handles input from one worker
 * @author Chris
 */
public class WorkerHandle
	extends CloseableThread
{
	/**
	 * The handle is in the process of releasing resources
	 */
	private boolean closing = false;
	/**
	 * The handle is not accepting any more work
	 */
	private boolean starve = false;
	/**
	 * The handle is to decommission itself as soon as current responsibilities expire.
	 */
	private boolean decommision = false;
	
	public  final WorkerHandshake handshake;
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
	private final Map<UUID, Long> jobsInProcess = Collections.synchronizedMap(new HashMap<UUID, Long>());
	public Map<UUID, Long> getJobsInProcess()
	{
		return Collections.unmodifiableMap(jobsInProcess);
	}
	
	public boolean isFull() {
		return this.getJobsInProcess().size() <= 2 * this.handshake.cores;
	}
	
	public long getPixelsInProcess() {
		long sum = 0;
		for (Long l : this.getJobsInProcess().values()) {
			sum += l;
		}
		return sum;
	}
	
	/**
	 * 
	 * @param nodeDetails
	 * @param workerSocket
	 * @param processedQueue
	 * @param workerPool
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	public WorkerHandle(NodeDetails nodeDetails, Socket workerSocket, Queue<Task> processedQueue, Map<String, WorkerHandle> workerPool) throws IOException, ClassNotFoundException
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
		
		this.handshake = (WorkerHandshake) in.readObject();
	}
	
	
	/*
	 * Set this worker to starve.
	 * A starving worker will not accept more jobs;
	 */
	public synchronized void setStarve(boolean starve)
	{
		this.starve = starve;
	}
	
	
	/*
	 * Get if this worker is starving
	 * A starving worker will not accept more jobs;
	 */
	public boolean isStarve()
	{
		return this.starve;
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
				//System.out.println(getName() + ": received from worker: " + job.getImage().length + "b");
				job.processed();
				this.jobsInProcess.remove(job.getUuid());
				
				// Queue response to client
				processedQueue.add(job);
				
				if (this.decommision)
					if (this.freeToDecommission())
						this.close();
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
	public Map<UUID, Long> getWorkloadSnapshot()
	{
		return new HashMap<UUID, Long>(this.jobsInProcess);
	}
	
	
	/**
	 * Send a job to be executed by this worker.
	 * This function blocks on the calling thread.
	 * @param job The job to sent.
	 * @throws IOException The connection to the worker has failed.
	 * @throws IllegalAccessException 
	 */
	public synchronized void sendJob(Task job) throws IOException, IllegalAccessException
	{
		if (job == null)
			throw new NullPointerException("Job cannot be null");
		
		if (this.isStarve())
			throw new IllegalAccessException("Worker cannot accept jobs while starving");
		
		//System.out.println(Thread.currentThread().getName() +  " sending job to " + workerSocket.getInetAddress().getHostAddress());

		this.jobsInProcess.put(job.getUuid(), job.getPixelCount());
		
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
	 * This worker will starve itself and be commissioned when the last job is completed.
	 * If no work is in progress, the worker will be commissioned immediately.
	 * @throws Exception 
	 */
	public synchronized void setForDecommision()
	{
		this.decommision = true;
		this.setStarve(true);
		
		if (this.freeToDecommission())
			try {
				this.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	
	/**
	 * Returns true if the worker can be decommissioned safely.
	 * @return
	 */
	public synchronized boolean freeToDecommission()
	{
		return this.isStarve()
				&& this.jobsInProcess.isEmpty();
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
		// TODO remove thread from thread pool too
		
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
		for (Long l : this.jobsInProcess.values())
			bs += l;
		
		return this.workerSocket.getInetAddress().getHostAddress() + "x" + this.handshake.cores + ": " + this.jobsInProcess.size() + " jobs, " + bs + " px";
	}
}


