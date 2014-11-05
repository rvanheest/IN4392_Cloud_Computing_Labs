package data;

import java.util.Objects;
import java.util.UUID;

public class Timing {

	private final UUID id;
	private final long time;

	public Timing(UUID id, long time) {
		this.id = id;
		this.time = time;
	}

	public UUID getId() {
		return this.id;
	}

	public long getTime() {
		return this.time;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Timing) {
			Timing that = (Timing) other;
			return Objects.equals(this.id, that.id)
					&& Objects.equals(this.time, that.time);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.time);
	}

	@Override
	public String toString() {
		return "<Timing[" + this.id + ", " + this.time + "]>";
	}
}
