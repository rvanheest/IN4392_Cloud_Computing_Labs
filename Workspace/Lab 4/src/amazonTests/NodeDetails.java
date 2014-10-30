package amazonTests;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Objects;

public final class NodeDetails implements Serializable {

	/**
	 * Generated serial version UID.
	 */
	private static final long serialVersionUID = -1681705464223433561L;

	private final String nodeID;
	private final String nodeType;
	private final InetAddress nodeIP;
	private final InetAddress privateIP;
	private final Integer sshPort;
	private final Integer httpsPort;
	private final Integer hornetQPort;
	private final Integer jmxPort;
	private final int processors;
	private final OperatingSystem operatingSystem;

	public NodeDetails(String nodeID, String nodeType, InetAddress nodeIP, 
			InetAddress privateIP, Integer sshPort,
			Integer httpsPort, Integer hornetQPort, Integer jmxPort, int processors,
			OperatingSystem operatingSytem) {
		this.nodeID = nodeID;
		this.nodeType = nodeType;
		this.nodeIP = nodeIP;
		this.privateIP = privateIP;
		this.sshPort = sshPort;
		this.httpsPort = httpsPort;
		this.hornetQPort = hornetQPort;
		this.jmxPort = jmxPort;
		this.processors = processors;
		this.operatingSystem = operatingSytem;
	}

	public String getNodeID() {
		return this.nodeID;
	}

	public String getNodeType() {
		return this.nodeType;
	}

	public InetAddress getNodeAddress() {
		return this.nodeIP;
	}
	
	public InetAddress getNodePrivateIP() {
		return this.privateIP;
	}

	public Integer getSSHPort() {
		return this.sshPort;
	}

	public Integer getHTTPSPort() {
		return this.httpsPort;
	}

	public Integer getHornetQPort() {
		return this.hornetQPort;
	}

	public Integer getJMXPort() {
		return this.jmxPort;
	}

	public int getProcessors() {
		return this.processors;
	}

	public OperatingSystem getOperatingSystem() {
		return this.operatingSystem;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof NodeDetails) {
			NodeDetails that = (NodeDetails) other;
			return Objects.equals(this.getNodeID(), that.getNodeID())
					&& Objects.equals(this.getNodeType(), that.getNodeType())
					&& Objects.equals(this.getNodeAddress(), that.getNodeAddress())
					&& Objects.equals(this.getSSHPort(), that.getSSHPort())
					&& Objects.equals(this.getHTTPSPort(), that.getHTTPSPort())
					&& Objects.equals(this.getHornetQPort(), that.getHornetQPort())
					&& Objects.equals(this.getJMXPort(), that.getJMXPort())
					&& Objects.equals(this.getProcessors(), that.getProcessors())
					&& Objects.equals(this.getOperatingSystem(), that.getOperatingSystem());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.getNodeID(), this.getNodeType(),
				this.getNodeAddress(), this.getSSHPort(), this.getHTTPSPort(),
				this.getHornetQPort(), this.getJMXPort(), this.getProcessors(),
				this.getOperatingSystem());
	}

	@Override
	public String toString() {
		return "<NodeDetails[" + String.valueOf(this.getNodeID()) + ", "
				+ String.valueOf(this.getNodeType()) + ", "
				+ String.valueOf(this.getNodeAddress()) + ", "
				+ String.valueOf(this.getSSHPort()) + ", "
				+ String.valueOf(this.getHTTPSPort()) + ", "
				+ String.valueOf(this.getHornetQPort()) + ", "
				+ String.valueOf(this.getJMXPort()) + ", "
				+ String.valueOf(this.getProcessors()) + ", "
				+ String.valueOf(this.getOperatingSystem()) + "]>";
	}
}
