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
	private long timeWorkerProcessStarted = -1;
	private long timeWorkerProcessEnded = -1;
	private long timeWorkerDelivered = -1;
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
		this.timeWorkerProcessStarted = t.timeWorkerProcessStarted;
		this.timeWorkerProcessEnded = t.timeWorkerProcessEnded;
		this.timeWorkerDelivered = t.timeWorkerDelivered;
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
	
	public long getTimeWorkerProcessStarted() {
		return timeWorkerProcessStarted;
	}
	
	public long getTimeWorkerProcessEnded() {
		return timeWorkerProcessEnded;
	}

	public long getTimeWorkerDelivered() {
		return timeWorkerDelivered;
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
	public void workerProcessStarted()
	{
		this.timeWorkerProcessStarted = System.currentTimeMillis();
	}
	public void workerProcessEnded()
	{
		this.timeWorkerProcessEnded = System.currentTimeMillis();
	}
	public void workerDelivered()
	{
		this.timeWorkerDelivered = System.currentTimeMillis();
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
	 * 
	 * @return
	 */
	public long[] stepsInWorker()
	{
		long[] steps =  new long[] 
		{ 
			this.timeWorkerProcessStarted - this.timeWorkerReceived,
			this.timeWorkerProcessEnded - this.timeWorkerReceived,
			this.timeWorkerDelivered - this.timeWorkerReceived,
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
		return this.timeWorkerDelivered - this.timeWorkerReceived;
	}
	
	
	@Override
	public String toString() 
	{
		long[] stepsHead = this.cumulativeStepsInHead();
		return "[Job: " + this.uuid + ": " + this.image.length + "b: " 
				+ stepsHead[0] + ", "
				+ stepsHead[1] + ", "
				+ stepsHead[2] + ", "
				+ stepsHead[3] + ", "
				+ "W" + (this.timeWorkerDelivered - this.timeWorkerReceived)
				+ "(" + (this.timeWorkerProcessEnded - this.timeWorkerProcessStarted) +")"
		+ "]";
	}
	
	public Object[] toParts()
	{
		long[] stepsHead = this.stepsInHead();
		long[] stepsWorker = this.stepsInWorker();
		return new Object[] {
			this.uuid,
			this.px,
			this.timeQueued,
			stepsHead[0],
			stepsHead[1],
			stepsHead[2],
			stepsWorker[0],
			stepsWorker[1],
			stepsWorker[2]
		};
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
