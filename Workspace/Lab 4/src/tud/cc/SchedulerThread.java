package tud.cc;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;

import scheduler.RandomScheduler;
import scheduler.Scheduler;
import scheduler.SchedulerException;
import data.Task;

public class SchedulerThread
	extends CloseableThread
{
	private boolean closing;
	
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
		
		try
		{
			while (!closing)
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
					Map<Task, WorkerHandle> mapping = scheduler.schedule(tasks, getElligibleWorkers(workerPool.values()));
					
					// Send to worker
					for (Entry<Task, WorkerHandle> entry : mapping.entrySet())
					{
						Task task = entry.getKey();
						WorkerHandle handle = entry.getValue();
						task.scheduled();
						try {
							handle.sendJob(task);
						} catch (IllegalAccessException e) {
							// Job was rejected. Re-queue job
							jobQueue.add(task);
							// Print error; This error should not occur.
							e.printStackTrace();
						}
					}
				}
				catch (SchedulerException | IOException e)
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
	private Collection<WorkerHandle> getElligibleWorkers(Collection<WorkerHandle> allWorkers)
	{
		ArrayList<WorkerHandle> filtered = new ArrayList<WorkerHandle>(allWorkers.size());
		for (WorkerHandle handle : allWorkers)
			if (!handle.isStarve())
				filtered.add(handle);
		return filtered;
	}


	@Override
	public void close() throws Exception 
	{
		this.closing = true;
		this.interrupt();
		
		// TODO Auto-generated method stub
		System.out.println(getName() + " closed.");	
	}
}