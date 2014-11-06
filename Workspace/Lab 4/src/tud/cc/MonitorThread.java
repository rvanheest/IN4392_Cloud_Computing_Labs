package tud.cc;

import java.io.FileNotFoundException;
import java.io.FilterReader;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.BlockingQueue;

import data.Sample;
import data.Task;

public class MonitorThread 
	extends CloseableThread 
{
	public static final long decisionInterval = 10_000; // In milliseconds
	
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
	private Boolean[] leaseConditions()
	{
		Collection<WorkerHandle> workers = getWorkers();
		
		// Condition 1: there are no workers
		boolean cond1 = false;
		if (workers.size() == 0)
			cond1 = true;
		
		// Condition 2: job queue exceeds 10 jobs for 5 consecutive samples
//		boolean cond2 = false;
//		int over = 0;
//		for (Sample sample : this.samples.subList(Math.max(samples.size()-5,0), samples.size()))
//			if (sample.queueSize > 10)
//				over++;
//		if (over > 10)
//			cond2 = true;
		
		// Condition 3: each worker has more than 4 jobs
//		boolean cond3 = true;
//		for (WorkerHandle worker : workers)
//			if (worker.getJobsInProcess().size() < 4)
//				cond3 = false;
		
		// Condition 4: workload over 80%
		boolean cond4 = true;
		cond4 = samples.get(samples.size()-1).getWorkload() > 0.8;
		
		
		return new Boolean[] {
				cond1,
//				cond2,
//				cond3,
				cond4
		};
	}
	
	
	/**
	 * Evaluate releasing condition
	 * @return true if releasing one worker is recommended
	 */
	private Boolean[] releaseConditions()
	{
		Collection<WorkerHandle> workers = getWorkers();
		
		// Condition 1: if three workers have no work
//		boolean cond1 = false;
//		int idle = 0;
//		for (WorkerHandle worker : workers)
//			if (worker.getJobsInProcess().size() == 0)
//				idle++;
//		if (idle >= 3)
//			cond1 = true;
		
		// Condition 2: Workload below 50% (more than 2 workers)
		boolean cond2 = false;
		cond2 = samples.get(samples.size()-1).getWorkload() < 0.5
				&& workers.size() > 2;
		
		
		return new Boolean[] {
//				cond1,
				cond2
		};
	}
	
	
	private <T> String arrayToString(T[] array)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (T item : array)
			sb.append(item).append(", ");
		sb.append("]");
		return sb.toString();
	}
	
	
	private boolean any(Boolean[] bools)
	{
		for (boolean b : bools)
			if (b)
				return true;
		return false;
	}
	
	
	/**
	 * Get all the active workers. Excludes starving workers
	 * @return
	 */
	private Collection<WorkerHandle> getWorkers()
	{
		Collection<WorkerHandle> allWorkers = this.headNode.getWorkers();
		Collection<WorkerHandle> filteredWorkers = new ArrayList<WorkerHandle>(allWorkers.size());
		for (WorkerHandle worker : allWorkers)
			if (!worker.isStarve())
				filteredWorkers.add(worker);
		return allWorkers;
	}
	
	
	@Override
	public void run() 
	{
		System.out.println(getName() + " started");
		
		try 
		{
			long lastDecision = 0;
			while (!closing)
			{
				// Sample the state of the system
				Sample nextSample = headNode.takeSample();
				this.samples.add(nextSample);
				CSVWriter.getSamples().writeLine(nextSample.toParts());
				
				// Make decision every decisionInterval milliseconds
				if (lastDecision + decisionInterval < System.currentTimeMillis())
				{
					lastDecision = System.currentTimeMillis();
					
					if (!headNode.isLeasing())
					{
						// Decide on leasing nodes
						Boolean[] leaseConds = leaseConditions(); 
						if (any(leaseConds))
						{
							System.out.println(getName() + " recommended leasing: " + arrayToString(leaseConds));
							headNode.leaseWorker();
						}
						
						// Decide on releasing nodes
						Boolean[] releaseConds = releaseConditions(); 
						if (any(releaseConds))
						{
							System.out.println(getName() + " recommended releasing: " + arrayToString(releaseConds));
							headNode.decommissionRandom();
						}
					}
				}
				
				sleep(1000);
			}
		}
		catch (InterruptedException e) 
		{
			if (!closing)
				e.printStackTrace();
		} 
		catch (FileNotFoundException | UnsupportedEncodingException e)
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
