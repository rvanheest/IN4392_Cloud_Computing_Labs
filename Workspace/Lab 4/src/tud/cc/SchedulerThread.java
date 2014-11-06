package tud.cc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingDeque;

import scheduler.QueueLengthScheduler;
import scheduler.Scheduler;
import scheduler.SchedulerResponse;
import data.Task;

public class SchedulerThread
	extends CloseableThread
{
	private boolean closing;
	
	private final Scheduler scheduler = new QueueLengthScheduler(2);
	
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
			boolean delayedScheduling = false;
			
			while (!closing)
			{
				try
				{
					// Wait for first worker
					if ( workerPool.values().size() < 1 )
					{
						sleep(5000);
					}
					
					Collection<WorkerHandle> eligibleWorkers = getEligibleWorkers(workerPool.values());
					if (eligibleWorkers.size() == 0 && workerPool.size() > 0 )
					{	// If every worker is full, wait and try again
						if (!delayedScheduling)
							System.out.println(getName() + " system overwhelmed. Delaying scheduling");
						delayedScheduling = true;
						sleep(1000);
						continue;
					}
					delayedScheduling = false;
					
					// Take a job from the queue
					ArrayList<Task> tasks = new ArrayList<>();
					tasks.add(jobQueue.take());
					
					// Re-determine eligible workers
					// We might have more,, but not fewer than before dequeuing the job
					eligibleWorkers = getEligibleWorkers(workerPool.values());
					
					// Take more jobs to schedule at once
					// Don't take more than one per worker
					while (tasks.size() < eligibleWorkers.size())
					{
						Task nextTask = jobQueue.poll();
						if (nextTask == null)
							break;
						tasks.add(nextTask);
					}
					
					// Schedule
					SchedulerResponse response = scheduler.schedule(tasks, eligibleWorkers);
					
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
				if (handle.getJobsInProcess().size() <= handle.handshake.cores*2) // If not full
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