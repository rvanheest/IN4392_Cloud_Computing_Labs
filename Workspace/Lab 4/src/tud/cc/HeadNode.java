package tud.cc;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import amazonTests.Configurations;
import amazonTests.EC2CloudService;
import amazonTests.NodeDetails;


public class HeadNode 
	implements AutoCloseable
{
	
	public static final int HeadServerPort = 6048;
	//public static final String HeadServerAddress = "ec2-54-171-121-60.eu-west-1.compute.amazonaws.com";
	

	ServerSocket serverSocket = null;    
	
	public HeadNode() throws IOException
	{
		System.out.println("Starting head chat");
		System.out.println("Listening at " + HeadServerPort);
		this.serverSocket = new ServerSocket(HeadServerPort);  
	}
	
	
	/**
	 * Accept one incoming chat connection and exchange messages
	 */
	public void chat()
	{
		try (Socket clientSocket = serverSocket.accept();
			 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
		     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));)
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
	 * AutoCloseable implementation
	 */
	@Override
	public void close() throws IOException
	{
		if (this.serverSocket != null)
			this.serverSocket.close();
		this.serverSocket = null;
		
		System.out.println("Head node closed");
	}
	
	
	/**
	 * Deploys a new node and returns the IP of the node
	 * @return The details of the node just deployed
	 */
	public NodeDetails startWorker()
	{
		System.out.println("Leasing new worker...");
		
		ExecutorService service = Executors.newFixedThreadPool(8);
		EC2CloudService cloudService = new EC2CloudService("AwsCredentials.properties", "CC", "ec2.eu-west-1.amazonaws.com", service);
		
		NodeDetails details = cloudService.leaseNode(new Configurations("random", null));
		
		return details;
	}
	
	
	public void acceptAndFeed()
	{
		try (Socket clientSocket = serverSocket.accept();
			 ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
			 ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) 
		{
			System.out.println("Accepted connection: " + clientSocket.getInetAddress().getCanonicalHostName());
			
			BufferedImage[] images = new BufferedImage[]
			{
				ImageIO.read(new File("images/Eiger.JPG")),
				//ImageIO.read(new File("images/Apen.JPG"))
			};
			
			for (BufferedImage image : images) 
			{
    			byte[] imageBytes = toByteArray(image);
    			System.out.println("SERVER - bytes: " + imageBytes.length);
    			
    			out.writeObject(imageBytes);
    			System.out.println("SERVER - send image");
			}
			
			for (int i = 0; i < images.length; i++)
			{
    			byte[] resultBytes = (byte[]) in.readObject();
    			System.out.println("SERVER - received bytes: " + resultBytes.length);
    			
    			BufferedImage result = toBufferedImage(resultBytes);
    			System.out.println("SERVER - received image: " + result);
    			
    			File output = new File("testing/Result" + i + ".JPG");
    			ImageIO.write(result, "JPG", output);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	public static byte[] toByteArray(BufferedImage image) throws IOException {
		try (ByteArrayOutputStream outbytes = new ByteArrayOutputStream()) {
			ImageIO.write(image, "JPG", outbytes);
			return outbytes.toByteArray();
		}
	}

	public static BufferedImage toBufferedImage(byte[] bytes) throws IOException {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
			return ImageIO.read(bais);
		}
	}
	
	
	public static void beHead()
	{
		try (HeadNode head = new HeadNode();)
		{
			NodeDetails workerDetails = head.startWorker();
			System.out.println("Leased: " + workerDetails);
			//head.chat();
			head.acceptAndFeed();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
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
	
}
