package data;

import java.util.Date;

public class Sample
{
	public final long timestamp = System.currentTimeMillis();
	public final int queueSize;
	public final long schedulingDelay;
	public final int coresLeased;
	public final int workersLeased;
	public final int workersUnderWay;
	public final int jobsInWorkers;
	public final int jobsIn;
	public final int jobsOut;
	
	private double smoothenedWorkload = -1;
	private double smoothenedPromisedWorkload = -1;
	
	public double getSmoothWorkload() { return this.smoothenedWorkload; }
	public double getSmoothPromisedWorkload() { return this.smoothenedPromisedWorkload; }
	
	
	
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
		this.workersUnderWay = workersUnderWay;
		this.jobsInWorkers = jobsInWorkers;
		this.jobsIn = jobsIn;
		this.jobsOut = jobsOut;
		
		this.smoothenedWorkload = getWorkload();
		this.smoothenedPromisedWorkload = getPromisedWorkload();
	}
	
	
	
	public double getWorkload()
	{
		if (coresLeased == 0)
			return 0;
		return ((double)(jobsInWorkers + queueSize)) / coresLeased;
	}
	
	
	
	public double getPromisedWorkload()
	{
		double workload = getSmoothWorkload();
		
		if (workersUnderWay == 0 || coresLeased == 0)
			return workload;
		
		double coresUnderWay = (workersUnderWay * (coresLeased / (double)workersLeased));
		double coresPromised = coresLeased + coresUnderWay;
		double promise = workload * (((double)coresLeased) / coresPromised);
		
		return promise;
	}
	
	
	
	public void setSmoothing(Sample previous)
	{
		double smooth;
		
		smooth = 0.00;
		this.smoothenedWorkload = (smooth * previous.smoothenedWorkload)
								  + ((1.0-smooth) * this.getWorkload());
		
		smooth = 0.95;
		this.smoothenedPromisedWorkload = (smooth * previous.smoothenedPromisedWorkload)
										  + ((1.0-smooth) * this.getPromisedWorkload());
	}
	
	
	
	public Object[] toParts()
	{
		return new Object[] {
			timestamp,
			queueSize,
			schedulingDelay,
			coresLeased,
			workersLeased,
			workersUnderWay,
			jobsInWorkers,
			jobsIn,
			jobsOut,
			String.format("%.4f", getWorkload()),
			String.format("%.4f", getSmoothWorkload()),
			String.format("%.4f", getPromisedWorkload()),
			String.format("%.4f", getSmoothPromisedWorkload()),
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
				+ "\tJobs in workers: " + this.jobsInWorkers + "\n"
				+ "\tJobs in: " + this.jobsIn + "\n"
				+ "\tJobs out: " + this.jobsOut + "";
	}
}