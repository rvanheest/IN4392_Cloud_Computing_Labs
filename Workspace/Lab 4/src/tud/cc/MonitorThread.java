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
	
	private final LinkedList<Sample> samples = new LinkedList<>();
	

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
	
	public List<Sample> getHistory(int window, long millisago)
	{
		long after = System.currentTimeMillis() - millisago;
		
		window = Math.min(window, samples.size());
		List<Sample> history = samples.subList(samples.size()-window, samples.size());
		
		ArrayList<Sample> filter = new ArrayList<>();
		for (Sample sample : history)
			if (sample.timestamp > after )
				filter.add(sample);
		
		return Collections.unmodifiableList(filter);
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
	private boolean leaseConditions()
	{
		Collection<WorkerHandle> workers = getWorkers();
		
		// Condition 1: there are no workers
		boolean cond1 = false;
		if (workers.size() == 0 && !headNode.isLeasing())
			cond1 = true;
		
		// Condition 2: workload over 80%
		boolean cond2 = true;
		cond2 = samples.getLast().getSmoothPromisedWorkload() > 0.8;
		
		
		return any(new Boolean[] {
				cond1,
				cond2
		});
	}
	
	
	/**
	 * Evaluate releasing condition
	 * @return true if releasing one worker is recommended
	 */
	private boolean releaseConditions()
	{
		Collection<WorkerHandle> workers = getWorkers();
		
		// Set minimum to 2 workers
		if (workers.size() <= 2)
			return false;
		
		// Condition 1: Workload consistently below 50% for 30s
		boolean cond1 = true;
		Collection<Sample> history = getHistory(10, 30_000);
		if (history.size()>3) // Monitoring thread not being starved
			for (Sample sample : history)
				if (sample.getSmoothPromisedWorkload() > 0.5)
					cond1 = false;
				
				
		return any(new Boolean[] {
				cond1
		});
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
	
	private boolean any(Boolean bool)
	{
		return bool;
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
				sampleState();
				
				// Make decision every decisionInterval milliseconds
				if (lastDecision + decisionInterval < System.currentTimeMillis())
				{
					lastDecision = System.currentTimeMillis();
					
					// Decide on leasing nodes
					Boolean leaseConds = leaseConditions(); 
					// Decide on releasing nodes
					Boolean releaseConds = releaseConditions(); 
					
					if (any(leaseConds))
					{
						// Increase workforce by 25% every time it is insufficient
						int leaseCount = samples.getLast().workersLeased / 4;
						leaseCount = Math.max(leaseCount, 1);
						System.out.println(getName() + " recommended leasing " + leaseCount);
						headNode.leaseWorker();
					}
					else if (any(releaseConds))
					{
						System.out.println(getName() + " recommended releasing");
						headNode.decommissionRandom();
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


	private void sampleState() throws FileNotFoundException, UnsupportedEncodingException 
	{
		Sample nextSample = headNode.takeSample();
		if (samples.size() > 0)
			nextSample.setSmoothing(samples.getLast());
		this.samples.addLast(nextSample);
		
		CSVWriter.getSamples().writeLine(nextSample.toParts());
	}
	
	
	@Override
	public void close() throws Exception 
	{
		this.closing = true;
		this.interrupt();
		
		System.out.println(getName() + " closed");
	}

}
