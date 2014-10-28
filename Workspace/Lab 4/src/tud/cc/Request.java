package tud.cc;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class Request
	implements Serializable
{
	private static final long serialVersionUID = -4917904487482835787L;
	
	public Request(UUID id, byte[] image)
	{
		this.id = id;
		this.image = image;
	}
	
	
	public final UUID id; 
	public final byte[] image;
	
	
	public UUID getId() {
		return id;
	}
	public byte[] getImage() {
		return image;
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( obj instanceof Request )
		{
			Request r = (Request) obj;
			return id.equals(r.id);
		}
		return false;			
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(id, image);
	}
	
	@Override
	public String toString() {
		return "Request " + id + ": " + this.image.length + "b"; 
	}
}
