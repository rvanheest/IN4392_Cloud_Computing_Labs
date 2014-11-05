package data;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class Request implements Serializable {

	private static final long serialVersionUID = -4917904487482835787L;

	private final UUID id;
	private final byte[] image;

	public Request(UUID id, byte[] image) {
		this.id = id;
		this.image = image;
	}

	public UUID getId() {
		return id;
	}

	public byte[] getImage() {
		return image;
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
		return Objects.hash(id, image);
	}

	@Override
	public String toString() {
		return "Request " + id + ": " + this.image.length + "b";
	}
}
