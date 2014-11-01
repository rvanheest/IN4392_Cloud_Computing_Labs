package data;

import java.util.Date;

public class Sample
{
	public final long timestamp = System.currentTimeMillis();
	public final int queueSize;
	public final long schedulingDelay;
	
	public Sample(int queueSize, long schedulingDelay)
	{
		this.queueSize = queueSize;
		this.schedulingDelay = schedulingDelay;
	}
	
	@Override
	public String toString() 
	{
		return "Sample " + new Date(timestamp) + ":" + "\n"
				+ "\tQueue: " + this.queueSize + "\n"
				+ "\tScheduling delay: " + this.schedulingDelay + "";
	}
}