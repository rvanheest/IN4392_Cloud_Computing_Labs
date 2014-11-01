package tud.cc;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import data.Task;

public class MonitorThread 
	extends CloseableThread 
{
	private boolean closing = false;
	
	private final Map<String, WorkerHandle> workerPool;
	private final BlockingQueue<Task> jobQueue;
	
	
	public MonitorThread(Map<String, WorkerHandle> workerPool, BlockingQueue<Task> jobQueue)
	{
		super("MonitorThread");
		
		this.workerPool = workerPool;
		this.jobQueue = jobQueue;
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
			}
		}
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void close() throws Exception 
	{
		this.closing = true;
		this.interrupt();
		
		System.out.println(getName() + " closed");
	}

}
