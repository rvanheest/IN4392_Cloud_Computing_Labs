package amazonTests;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Test {

	public static void main(String[] args) {
		ExecutorService service = Executors.newFixedThreadPool(8);
		EC2CloudService cloudService = new EC2CloudService("AwsCredentials.properties", "CC", "ec2.eu-west-1.amazonaws.com", service);
		
		NodeDetails details = cloudService.leaseNode(new Configurations("random", null));
		System.out.println(details);
		
//		cloudService.releaseNode(details);
	}
}
