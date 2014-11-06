package tud.cc;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import tud.cc.ClientHandle;
import data.Request;
import data.Task;

/**
 * Handles input from one client
 * @author Chris
 */
public class ClientHandle
	extends CloseableThread
{
	private boolean closing = false;
	
	private final HeadNode headnode;
	private final Connection connection;
	private final Queue<Task> jobQueue;
	private final Map<UUID, ClientHandle> requestMap;
	
	
	public ClientHandle(HeadNode headnode, Connection connection, Queue<Task> jobQueue, Map<UUID, ClientHandle> requestMap) throws IOException
	{
		super("ClientHandle");
		this.headnode = headnode;
		this.connection = connection;
		this.jobQueue = jobQueue;
		this.requestMap = requestMap;
	}
	
	@Override
	public void run()
	{
		System.out.println(getName() + " started");
		
		try
		{
			// Start accepting images
			while (!closing)
			{
				// Get request
				Request request = connection.receive();
    			//System.out.println(getName() + " received request " + request.getImage().length + "b");

    			// TODO Image backup
    			
				// Queue task
    			Task task = new Task(request.getId(), request.getImage(), request.getPixelCount());
    			requestMap.put(request.getId(), this);
    			
    			task.queued();
    			jobQueue.add(task);
    			headnode.justIn(task);
			}
		}
		catch (IOException e)
		{
			System.out.println(getName() + " connection to " + connection.socket.getInetAddress().getHostAddress() + " lost");
		}
		catch (Exception e)
		{
			if (!closing)
				e.printStackTrace();
		}
		finally
		{
			try {
				// TODO remove client?
				connection.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Sends response to the client.
	 * Blocks on the calling thread.
	 * 
	 * @param response The response object
	 * @throws IOException
	 */
	public synchronized void sendResponse(Request response) throws IOException
	{
		if (response == null)
			throw new NullPointerException("Response cannot be null");
		
		//System.out.println(getName() + " sending response to " + connection.socket.getInetAddress().getHostAddress());
		requestMap.remove(response.getId());
		
		this.connection.send(response);
	}
	

	@Override
	public void close() throws Exception 
	{
		this.closing = true;
		this.interrupt();
		
		this.connection.close();
		
		System.out.println(getName() + " closed.");
	}
}