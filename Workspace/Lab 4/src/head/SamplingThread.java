package head;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import data.Sample;

public class SamplingThread
	extends CloseableThread
{
	private boolean closing = false;
	
	private final HeadNode headNode;
	
	private final LinkedList<Sample> samples = new LinkedList<>();
	
	
	public SamplingThread(HeadNode headnode)
	{
		super("SamplingThread");
		
		this.headNode = headnode;
	}
	
	public synchronized Sample getMostRecent()
	{
		return samples.getLast();
	}
	
	public synchronized List<Sample> getHistory()
	{
		return Collections.unmodifiableList(samples);
	}
	
	public synchronized List<Sample> getHistory(int window)
	{
		List<Sample> history = getHistory();
		window = Math.min(window, history.size());
		return history.subList(history.size()-window, history.size());
	}
	
	public synchronized List<Sample> getHistory(int window, long millisago)
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
	
	private void sampleState() throws FileNotFoundException, UnsupportedEncodingException 
	{
		Sample nextSample = headNode.takeSample();
		
		synchronized (this)
		{
			if (samples.size() > 0)
				nextSample.setSmoothing(samples.getLast());
			this.samples.addLast(nextSample);
		}
		
		CSVWriter.getSamples().writeLine(nextSample.toParts());
	}
	
	public void run()
	{
		System.out.println(getName() + " started");
		
		try 
		{
			while (!closing)
			{
				sampleState();
				
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
