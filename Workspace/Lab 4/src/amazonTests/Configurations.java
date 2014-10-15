package amazonTests;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public final class Configurations implements Serializable {

	/**
	 * Generated serial version UID.
	 */
	private static final long serialVersionUID = 883245041650450341L;

	private final String nodeType;
	private final Map<String, String> configuration;

	public Configurations(String nodeType, Map<String, String> configuration) {
		this.nodeType = nodeType;
		this.configuration = configuration;
	}

	public String getNodeType() {
		return this.nodeType;
	}

	public Map<String, String> getConfiguration() {
		return this.configuration;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Configurations) {
			Configurations that = (Configurations) other;
			return Objects.equals(this.getNodeType(), that.getNodeType())
					&& Objects.equals(this.getConfiguration(), that.getConfiguration());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.getNodeType(), this.getConfiguration());
	}

	@Override
	public String toString() {
		return "<Configurations[" + String.valueOf(this.getNodeType()) + ", "
				+ String.valueOf(this.getConfiguration()) + "]>";
	}
}
