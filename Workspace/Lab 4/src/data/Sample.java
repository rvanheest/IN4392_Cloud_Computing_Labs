package data;

import java.util.Date;

public class Sample
{
	public final long timestamp = System.currentTimeMillis();
	public final int queueSize;
	public final long schedulingDelay;
	public final int coresLeased;
	public final int workersLeased;
	public final int workerUnderWay;
	public final int jobsInWorkers;
	public final int jobsIn;
	public final int jobsOut;
	
	private double processedWorkload = -1;
	
	/**
	 * 
	 * @param queueSize
	 * @param schedulingDelay
	 * @param coresLeased
	 * @param workersLeased
	 * @param workersUnderWay
	 * @param jobsInWorkers
	 * @param jobsIn
	 * @param jobsOut
	 */
	public Sample(int queueSize, long schedulingDelay, int coresLeased, int workersLeased,
			int workersUnderWay, int jobsInWorkers, int jobsIn, int jobsOut)
	{
		this.queueSize = queueSize;
		this.schedulingDelay = schedulingDelay;
		this.coresLeased = coresLeased;
		this.workersLeased = workersLeased;
		this.workerUnderWay = workersUnderWay;
		this.jobsInWorkers = jobsInWorkers;
		this.jobsIn = jobsIn;
		this.jobsOut = jobsOut;
	}
	
	
	public double getWorkload()
	{
		return ((double)(jobsInWorkers + queueSize)) / coresLeased;
	}
	
	public double getProcessedWorkload()
	{
		return this.processedWorkload;
	}
	
	public void setProcessedWorkload(double processedWorkload)
	{
		this.processedWorkload = processedWorkload;
	}
	
	public Object[] toParts()
	{
		return new Object[] {
			timestamp,
			queueSize,
			schedulingDelay,
			coresLeased,
			workersLeased,
			workerUnderWay,
			jobsInWorkers,
			jobsIn,
			jobsOut,
			getWorkload(),
			getProcessedWorkload()
		};
	}
	
	@Override
	public String toString() 
	{
		return "Sample " + new Date(timestamp) + ":" + "\n"
				+ "\tQueue: " + this.queueSize + "\n"
				+ "\tScheduling delay: " + this.schedulingDelay + "\n"
				+ "\tCores total: " + this.coresLeased + "\n"
				+ "\tWorkers total: " + this.workersLeased + "\n"
				+ "\tJobs in workers: " + this.jobsInWorkers + "'n"
				+ "\tJobs in: " + this.jobsIn + "\n"
				+ "\tJobs out: " + this.jobsOut + "";
	}
}