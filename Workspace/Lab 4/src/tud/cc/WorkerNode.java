package tud.cc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class WorkerNode 
{	
	public static void main(String[] args)
	{
		String server = (args.length > 0) ? args[0] : "localhost";		
		chat(server);
	}
	
	public static void chat(String head)
	{		
		System.out.println("Starting worker chat");
		System.out.println("Reaching to " + head + ":" + HeadNode.HeadServerPort );
		
		try (
			    Socket kkSocket = new Socket(head, HeadNode.HeadServerPort);
			    PrintWriter out = new PrintWriter(kkSocket.getOutputStream(), true);
			    BufferedReader in = new BufferedReader(
			        new InputStreamReader(kkSocket.getInputStream()));
			)
		{		
			System.out.print("Reading: ");
			String read = in.readLine();
			System.out.println(read);
			
			String write = "No, this is Partick!\n";
			System.out.print("Writing: " + write);
			out.write(write);
			out.flush();
			
			System.out.println();
		}
		catch (IOException e)
		{
		}
	}
	
	
	public static void beWorker(String server)
	{
		chat(server);
	}

}
