package data;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class Request implements Serializable {

	private static final long serialVersionUID = -4917904487482835787L;

	private final UUID id;
	private final byte[] image;
	private final long px;

	public Request(UUID id, byte[] image, long px) {
		this.id = id;
		this.image = image;
		this.px = px;
	}

	public UUID getId() {
		return id;
	}

	public byte[] getImage() {
		return image;
	}
	
	public long getPixelCount() {
		return this.px;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Request) {
			Request that = (Request) other;
			return Objects.equals(this.id, that.id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.image, this.px);
	}

	@Override
	public String toString() {
		return "Request " + this.id + ": " + this.image.length + " b - " + this.px + " px";
	}
}
