package tud.cc;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import amazonTests.Configurations;
import amazonTests.EC2CloudService;
import amazonTests.NodeDetails;


public class HeadNode 
{
	
	public static final int HeadServerPort = 6048;
	public static final String HeadServerAddress = "ec2-54-171-121-60.eu-west-1.compute.amazonaws.com";
	

	public HeadNode()
	{
	}
	
	
	public void chat()
	{
		System.out.println("Starting head chat");
		System.out.println("Listening at " + HeadServerPort);
		
		try ( 
			    ServerSocket serverSocket = new ServerSocket(HeadServerPort);
			    Socket clientSocket = serverSocket.accept();
			    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
			    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			)
		{
			String write = "Is this the client?\n";
			System.out.print("Writing: " + write);
			out.write(write);
			out.flush();
			
			System.out.print("Reading: ");
			String read = in.readLine();
			System.out.print(read);
			
			System.out.println();			
		}
		catch(IOException e)
		{
		}
	}
	
	
	/**
	 * Deploys a new node and returns the IP of the node
	 * @return The details of the node just deployed
	 */
	public NodeDetails startWorker()
	{
		ExecutorService service = Executors.newFixedThreadPool(8);
		EC2CloudService cloudService = new EC2CloudService("AwsCredentials.properties", "CC", "ec2.eu-west-1.amazonaws.com", service);
		
		NodeDetails details = cloudService.leaseNode(new Configurations("random", null));
		
		return details;
	}
	
	
	public static void main(String[] args)
	{
		switch (args[0])
		{
			case "head":
				HeadNode.beHead();
				break;
			case "worker":
				//beWorker(args[1]); //TODO remove hardcoding
				WorkerNode.beWorker(args[1]);
				break;
			default:
				System.err.println("Unknown role: " + args[0]);
				return;
		}
	}
	
	
	public static void beHead()
	{
		HeadNode head = new HeadNode();
		
		head.chat();
	}
	
	
	
}
