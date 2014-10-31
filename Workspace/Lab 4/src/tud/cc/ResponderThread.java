package tud.cc;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import data.Request;
import data.Task;

public class ResponderThread
	extends CloseableThread
{
	private boolean closing = false;
	
	private final BlockingQueue<Task> processed;
	private final Map<UUID, ClientHandle> requestMap;
	
	
	public ResponderThread(BlockingQueue<Task> processed, Map<UUID, ClientHandle> requestMap)
	{
		super("Responder");
		this.processed = processed;
		this.requestMap = requestMap;
	}
	

	@Override
	public void run() 
	{
		try 
		{
			while (true)
			{
				try
				{
					Task task = processed.take();
					
					UUID requestUuid = task.getRequestUuid();
					Request response = new Request(requestUuid, task.getImage());
					
					ClientHandle handle = requestMap.get(requestUuid);
					handle.sendResponse(response);
				}
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
		} 
		catch (InterruptedException e)
		{
			if (!this.closing)
				e.printStackTrace();
		}
	}
	
	
	@Override
	public void close() throws Exception 
	{
		this.closing = true;
		this.interrupt();
		
		System.out.println(getName() + " closed.");
	}
	
}