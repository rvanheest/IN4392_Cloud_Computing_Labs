package tud.cc;

import imageProcessing.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class WorkerNode 
{	

	
	public static void beWorker(String server)
	{
		try (Client client = new Client(server)) {
			while (true) {
				Thread.sleep(10000);
			}
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args)
	{
		String server = (args.length > 0) ? args[0] : "localhost";
		
		beWorker(server);
	}	

}
