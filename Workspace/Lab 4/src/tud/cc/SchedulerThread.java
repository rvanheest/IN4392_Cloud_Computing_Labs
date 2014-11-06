package tud.cc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingDeque;

import scheduler.BlockingQueueLengthScheduler;
import scheduler.Scheduler;
import scheduler.SchedulerResponse;
import data.Task;

public class SchedulerThread
	extends CloseableThread
{
	private boolean closing;
	
	private final Scheduler scheduler = new BlockingQueueLengthScheduler();
	
	private final BlockingDeque<Task> jobQueue;
	private final Map<String, WorkerHandle> workerPool;
	
	
	public SchedulerThread(BlockingDeque<Task> jobQueue, Map<String, WorkerHandle> workerPool)
	{
		super("Scheduler");
		this.jobQueue = jobQueue;
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
				sleep(1000);
				try
				{
					// Wait for first worker
					if ( workerPool.values().size() < 1 )
					{
						sleep(5000);
					}
					
					Collection<WorkerHandle> eligibleWorkers = this.getEligibleWorkers(workerPool.values());
					List<Task> tasks = new ArrayList<>();
					tasks.add(jobQueue.take());
					this.jobQueue.drainTo(tasks);
					
					SchedulerResponse response = this.scheduler.schedule(tasks, eligibleWorkers);
					
					List<Task> reject = response.getReject();
					int size = reject.size();
					for (int i = size - 1; i >= 0; i--) {
						this.jobQueue.putFirst(reject.get(i));
					}
					
					// Send to worker
					int scheduled = 0;
					for (Entry<Task, WorkerHandle> entry : response.getAccept().entrySet())
					{
						Task task = entry.getKey();
						WorkerHandle handle = entry.getValue();
						task.scheduled();
						try 
						{
							handle.sendJob(task);
							scheduled++;
						} 
						catch (IllegalAccessException e) 
						{
							// Job was rejected. Re-queue job
							jobQueue.putLast(task);
							// Print error; This error should not occur.
							e.printStackTrace();
						}
					}
					System.out.println(getName() + " scheduled " + scheduled + " jobs.");
				}
				catch (IOException e)
				{
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
	
	
	/**
	 * Filter a collection of workers to those that are accepting work.
	 * @param allWorkers The original collection of workers.
	 * @return The filtered collection.
	 */
	private Collection<WorkerHandle> getEligibleWorkers(Collection<WorkerHandle> allWorkers)
	{
		ArrayList<WorkerHandle> filtered = new ArrayList<WorkerHandle>(allWorkers.size());
		for (WorkerHandle handle : allWorkers)
			if (!handle.isStarve()) // If not marked for decommission
					filtered.add(handle);
		return filtered;
	}


	@Override
	public void close() throws Exception 
	{
		this.closing = true;
		this.interrupt();
		
		System.out.println(getName() + " closed.");	
	}
}