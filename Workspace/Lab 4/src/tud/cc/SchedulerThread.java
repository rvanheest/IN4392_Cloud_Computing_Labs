package tud.cc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
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
					task.scheduled();
					handle.sendJob(task);
				}
			}
			catch (InterruptedException e)
			{
				if (!this.closing)
					e.printStackTrace();
			}
			catch (SchedulerException | IOException e)
			{
				e.printStackTrace();
			}
		}
	}


	@Override
	public void close() throws Exception 
	{
		this.closing = false;
		this.interrupt();
		
		// TODO Auto-generated method stub
		System.out.println(getName() + " closed.");	
	}
}