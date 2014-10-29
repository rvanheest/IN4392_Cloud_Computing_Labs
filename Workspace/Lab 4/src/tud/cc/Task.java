package tud.cc;

import java.util.Objects;
import java.util.UUID;

public class Task 
{
	private final UUID uuid;
	private final UUID requestUuid;
	
	private long timeQueued;
	private long timeScheduled;
	private long timeProcessed;
	private long timeServed;
	
	private byte[] image;
	
	
	public Task(UUID requestUuid, byte[] image) 
	{
		this.uuid = UUID.randomUUID();
		this.requestUuid = requestUuid;
		
		this.image = image;
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

	public long getTimeProcessed() {
		return timeProcessed;
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
		this.timeProcessed = System.currentTimeMillis();
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
