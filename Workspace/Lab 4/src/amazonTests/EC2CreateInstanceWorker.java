package amazonTests;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Callable;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;

public class EC2CreateInstanceWorker implements Callable<NodeDetails> {

	private final String securityGroup;
	private final String imageName;
	private final String instanceType;
	private final AmazonEC2Client amazonEC2Client;

	public EC2CreateInstanceWorker(String securityGroup, String imageName, String instanceType,
			AmazonEC2Client amazonEC2Client) {
		this.securityGroup = securityGroup;
		this.imageName = imageName;
		this.instanceType = instanceType;
		this.amazonEC2Client = amazonEC2Client;
	}

	@Override
	public NodeDetails call() throws Exception {
		return this.createInstance();
	}

	private NodeDetails createInstance() throws UnknownHostException {
		// TODO: do something with config
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest();

		runInstancesRequest.withImageId(this.imageName).withInstanceType(this.instanceType)
				.withMinCount(1).withMaxCount(1).withKeyName("Cloud Computing")
				.withSecurityGroups(this.securityGroup);
		RunInstancesResult runInstancesResult = this.amazonEC2Client
				.runInstances(runInstancesRequest);
		Reservation reservation = runInstancesResult.getReservation();
		List<Instance> instances = reservation.getInstances();
		Instance instance = instances.get(0);

		String ipAddress = this.getInstancePublicIpAddress(instance.getInstanceId());
		return new NodeDetails(instance.getInstanceId(), instance.getInstanceType(),
				InetAddress.getByName(ipAddress), 22, 443, 5456, 6060, 1, null);
	}

	private String getInstancePublicIpAddress(String instanceId) {
		String ipaddress = null;
		while (ipaddress == null) {
			DescribeInstancesResult describeInstancesRequest = this.amazonEC2Client
					.describeInstances();
			List<Reservation> reservations = describeInstancesRequest.getReservations();
			for (Reservation reservation : reservations) {
				for (Instance instance : reservation.getInstances()) {
					if (instance.getInstanceId().equals(instanceId)) {
						ipaddress = instance.getPublicIpAddress();
					}
				}
			}
		}
		return ipaddress;
	}
}
