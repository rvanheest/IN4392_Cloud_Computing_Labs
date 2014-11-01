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
	
	
//	private boolean leaseCondition()
//	{
//		
//	}
	
	
	@Override
	public void run() 
	{
		System.out.println(getName() + " started");
		
		try 
		{
			while (!closing)
			{
				this.samples.add(headNode.takeSample());
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
