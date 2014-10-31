package tud.cc;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import data.Task;

/**
 * This receives jobs asynchronously from the client.
 * Incoming jobs are queued in the job queue.
 * 
 * @author Chris
 *
 */
public class ClientReceptionThread
	extends CloseableThread
{
	private boolean closing = false;
	
	private final ServerSocket socket;
	private final BlockingQueue<Task> jobQueue;
	private final Deque<CloseableThread> threads;
	private final Map<UUID, ClientHandle> requestMap;
	
	public ClientReceptionThread(int port, BlockingQueue<Task> jobQueue, Deque<CloseableThread> threads, Map<UUID, ClientHandle> requestMap) throws IOException
	{
		super("ClientReception");
		this.socket = new ServerSocket(port);
		this.jobQueue = jobQueue;
		this.threads = threads;
		this.requestMap = requestMap;
	}
	
	@Override
	public void run()
	{
		System.out.println(getName() + " started");
		
		while (true)
		{
			// Accept connection
			try 
			{
				Connection connection = new Connection(socket.accept());
				
				System.out.println("Accepted client connection: " + connection.socket.getInetAddress().getHostAddress());
				
				ClientHandle handle = new ClientHandle(connection, jobQueue, requestMap);
				threads.add(handle);
				handle.start();
			}
			catch (Exception e)
			{
				if (this.closing)
					break;
				e.printStackTrace();
			}
		}
	}

	@Override
	public void close() throws Exception
	{
		this.closing = true;
		this.interrupt();
		
		this.socket.close();
		
		System.out.println(getName() + " closed.");
	}
}