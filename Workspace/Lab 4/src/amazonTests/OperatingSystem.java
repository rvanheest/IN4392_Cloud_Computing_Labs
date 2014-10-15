package amazonTests;

import java.io.Serializable;
import java.util.Objects;

public final class OperatingSystem implements Serializable {

	/**
	 * Generated serial version UID.
	 */
	private static final long serialVersionUID = 8125657020705911501L;

	private final String opSys;
	private final String version;

	public OperatingSystem(String opSys, String version) {
		this.opSys = opSys;
		this.version = version;
	}

	public String getOperatingSystem() {
		return this.opSys;
	}

	public String getVersion() {
		return this.version;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof OperatingSystem) {
			OperatingSystem that = (OperatingSystem) other;
			return Objects.equals(this.getOperatingSystem(), that.getOperatingSystem())
					&& Objects.equals(this.getVersion(), that.getVersion());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.getOperatingSystem(), this.getVersion());
	}

	@Override
	public String toString() {
		return "<OperatingSystem[" + String.valueOf(this.getOperatingSystem()) + ", "
				+ String.valueOf(this.getVersion()) + "]>";
	}
}
