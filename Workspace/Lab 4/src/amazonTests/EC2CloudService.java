package amazonTests;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.auth.PropertiesCredentials;

public class EC2CloudService implements CloudService {

	private final String credentialProperties;
	private final String securityGroup;
	private final String endPoints;
	private final ExecutorService executorService;
	private AmazonEC2Client amazonEC2Client;

	public EC2CloudService(String credentialProperties, String securityGroup, String endPoints, ExecutorService executorService) {
		this.credentialProperties = credentialProperties;
		this.securityGroup = securityGroup;
		this.endPoints = endPoints;
		this.executorService = executorService;
		this.createEC2Client();
	}

	private void createEC2Client() {
		try {
			this.amazonEC2Client = new AmazonEC2Client(new PropertiesCredentials(new File(this.credentialProperties)));
			this.amazonEC2Client.setEndpoint(this.endPoints);
		}
		catch (IllegalArgumentException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public NodeDetails leaseNode(Configurations config) {
		// TODO: config definition
		String imageName = "ami-748e2903";
		String instanceType = "t2.micro";
		EC2CreateInstanceWorker worker = new EC2CreateInstanceWorker(this.securityGroup, imageName, instanceType, this.amazonEC2Client);
		Future<NodeDetails> nodeDetails = this.executorService.submit(worker);
		try {
			return nodeDetails.get();
		}
		catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void releaseNode(NodeDetails node) {
		TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest();
		terminateInstancesRequest.withInstanceIds(node.getNodeID());
		this.amazonEC2Client.terminateInstances(terminateInstancesRequest);
	}
}
