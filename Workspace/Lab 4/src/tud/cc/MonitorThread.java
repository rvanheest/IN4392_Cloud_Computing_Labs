package tud.cc;

import java.util.*;
import java.util.concurrent.BlockingQueue;

import data.Sample;
import data.Task;

public class MonitorThread 
	extends CloseableThread 
{
	private boolean closing = false;
	
	private final HeadNode headNode;
	
	private final List<Sample> samples = new ArrayList<>();
	
	
	public List<Sample> getHistory()
	{
		return Collections.unmodifiableList(samples);
	}
	
	
	public List<Sample> getHistory(int window)
	{
		List<Sample> history = getHistory();
		window = Math.min(window, history.size());
		return history.subList(history.size()-window, history.size());
	}
	
	
	public MonitorThread(HeadNode headNode)
	{
		super("MonitorThread");
		
		this.headNode = headNode;
	}
	
	
	/**
	 * Evaluate leasing condition
	 * @return true if leasing is recommended
	 */
	private boolean leaseCondition()
	{
		return false;
	}
	
	
	/**
	 * Evaluate releasing condition
	 * @return true if releasing is recommended
	 */
	private boolean releaseCondition()
	{
		return false;
	}
	
	
	@Override
	public void run() 
	{
		System.out.println(getName() + " started");
		
		try 
		{
			while (!closing)
			{
				// Sample the state of the system
				this.samples.add(headNode.takeSample());
				
				// Decide on leasing nodes
				if (leaseCondition())
					headNode.startWorker();
				
				// Decide on releasing nodes
				if (releaseCondition())
					headNode.decommissionRandom();
				
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
