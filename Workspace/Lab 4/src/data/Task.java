package data;

import java.io.Serializable;
import java.util.UUID;

public class Task 
	implements Serializable
{
	private static final long serialVersionUID = 5186635774216529654L;
	
	private final UUID uuid;
	private final UUID requestUuid;
	
	private long timeQueued = -1;
	private long timeScheduled = -1;
	private long timeWorkerReceived = -1;
	private long timeWorkerProcessed = -1;
	private long timeProcessed = -1;
	private long timeServed = -1;
	
	private byte[] image;
	private long px;
	
	
	public Task(UUID requestUuid, byte[] image, long px) 
	{
		this.uuid = UUID.randomUUID();
		this.requestUuid = requestUuid;
		
		this.image = image;
		this.px = px;
	}

	public Task(Task t, byte[] newImage) 
	{
		this.uuid = t.uuid;
		this.requestUuid = t.requestUuid;
		
		this.timeQueued = t.timeQueued;
		this.timeScheduled = t.timeScheduled;
		this.timeWorkerProcessed = t.timeWorkerProcessed;
		this.timeWorkerReceived = t.timeWorkerReceived;
		this.timeServed = t.timeServed;
		
		this.image = newImage;
	}
	
	
	public UUID getUuid() {
		return uuid;
	}
	
	public UUID getRequestUuid() {
		return requestUuid;
	}

	public long getTimeQueued() {
		return timeQueued;
	}

	public long getTimeScheduled() {
		return timeScheduled;
	}

	public long getTimeWorkerProcessed() {
		return timeWorkerProcessed;
	}

	public long getTimeWorkerReceived() {
		return this.timeWorkerReceived;
	}
	
	public long getTimeProcessed() {
		return this.timeProcessed;
	}

	public long getTimeServed() {
		return timeServed;
	}

	public byte[] getImage() {
		return image;
	}
	
	public int getImageSize() {
		return this.getImage().length;
	}
	
	public long getPixelCount() {
		return this.px;
	}

	public void queued()
	{
		this.timeQueued = System.currentTimeMillis();
	}
	public void scheduled()
	{
		this.timeScheduled = System.currentTimeMillis();
	}
	public void workerReceived() 
	{
		this.timeWorkerReceived = System.currentTimeMillis();
	}
	public void workerProcessed()
	{
		this.timeWorkerProcessed = System.currentTimeMillis();
	}
	public void processed()
	{
		this.timeProcessed = System.currentTimeMillis();
	}
	public void served()
	{
		this.timeServed = System.currentTimeMillis();
	}
	
	
	/**
	 * The cumulative span of the processing steps
	 * @return
	 */
	public long[] cumulativeStepsInHead()
	{
		long[] steps =  new long[] 
		{ 
			this.timeQueued - this.timeQueued,
			this.timeScheduled - this.timeQueued,
			this.timeProcessed - this.timeQueued,
			this.timeServed - this.timeQueued,
		};
		for (int i=0 ; i<steps.length ; i++)
			if (steps[i] < 0)
				steps[i] = -1;
		return steps;
	}
	
	/**
	 * The span of the processing steps
	 * @return
	 */
	public long[] stepsInHead()
	{
		long[] steps =  new long[] 
		{ 
			this.timeScheduled - this.timeQueued,
			this.timeProcessed - this.timeScheduled,
			this.timeServed - this.timeProcessed,
		};
		for (int i=0 ; i<steps.length ; i++)
			if (steps[i] < 0)
				steps[i] = -1;
		return steps;
	}
	
	/**
	 * Get the time that this image spent being processed
	 * @return
	 */
	public long getWorkerProcessingTime()
	{
		return this.timeWorkerProcessed - this.timeWorkerReceived;
	}
	
	
	@Override
	public String toString() 
	{
		long[] steps = this.cumulativeStepsInHead();
		return "[Job: " + this.uuid + ": " + this.image.length + "b: " 
				+ steps[0] + ", "
				+ steps[1] + ", "
				+ steps[2] + ", "
				+ steps[3] + ", "
		+ "]";
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		if (obj instanceof Task)
		{
			Task other = (Task) obj;
			return other.uuid.equals(this.uuid);
		}
		return false;
	}
	
	@Override
	public int hashCode() 
	{
		return this.uuid.hashCode();
	}
}
