package data;

import java.io.Serializable;
import java.util.UUID;

public class Task 
	implements Serializable
{
	private static final long serialVersionUID = 5186635774216529654L;
	
	private final UUID uuid;
	private final UUID requestUuid;
	
	private long timeQueued;
	private long timeScheduled;
	private long timeWorkerReceived;
	private long timeWorkerProcessed;
	private long timeServed;
	
	private byte[] image;
	
	
	public Task(UUID requestUuid, byte[] image) 
	{
		this.uuid = UUID.randomUUID();
		this.requestUuid = requestUuid;
		
		this.image = image;
	}

	public Task(Task t, byte[] newImage) {
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

	public long getTimeServed() {
		return timeServed;
	}

	public byte[] getImage() {
		return image;
	}

	public void queued()
	{
		this.timeQueued = System.currentTimeMillis();
	}
	public void scheduled()
	{
		this.timeScheduled = System.currentTimeMillis();
	}
	public void processed()
	{
		this.timeWorkerProcessed = System.currentTimeMillis();
	}
	public void received() {
		this.timeWorkerReceived = System.currentTimeMillis();
	}
	public void served()
	{
		this.timeServed = System.currentTimeMillis();
	}
	
	@Override
	public String toString() {
		return "[Job: " + this.uuid + ": " + this.image.length + "b]";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Task)
		{
			Task other = (Task) obj;
			return other.uuid.equals(this.uuid);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.uuid.hashCode();
	}
}
