package data;

import java.util.Date;

public class Sample
{
	public final long timestamp = System.currentTimeMillis();
	public final int queueSize;
	public final long schedulingDelay;
	public final int coresLeased;
	public final int workersLeased;
	public final int jobsInWorkers;
	
	public Sample(int queueSize, long schedulingDelay, int coresLeased, int workersLeased, int jobsInWorkers)
	{
		this.queueSize = queueSize;
		this.schedulingDelay = schedulingDelay;
		this.coresLeased = coresLeased;
		this.workersLeased = workersLeased;
		this.jobsInWorkers = jobsInWorkers;
	}
	
	public double getWorkload()
	{
		return ((double)(jobsInWorkers + queueSize)) / coresLeased;
	}
	
	@Override
	public String toString() 
	{
		return "Sample " + new Date(timestamp) + ":" + "\n"
				+ "\tQueue: " + this.queueSize + "\n"
				+ "\tScheduling delay: " + this.schedulingDelay + "\n"
				+ "\tCores total: " + this.coresLeased + "\n"
				+ "\tWorkers total: " + this.workersLeased + "\n"
				+ "\tJobs in workers: " + this.jobsInWorkers + "";
	}
}