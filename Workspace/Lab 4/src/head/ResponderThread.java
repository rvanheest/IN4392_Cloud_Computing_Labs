package head;

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
	
	private final HeadNode headnode;
	private final BlockingQueue<Task> processed;
	private final Map<UUID, ClientHandle> requestMap;
	
	
	public ResponderThread(HeadNode headNode, BlockingQueue<Task> processed, Map<UUID, ClientHandle> requestMap)
	{
		super("ResponderThread");
		this.headnode = headNode;
		this.processed = processed;
		this.requestMap = requestMap;
	}
	

	@Override
	public void run() 
	{
		System.out.println(getName() + " started");
		
		try 
		{
			while (!closing)
			{
				try
				{
					Task task = processed.take();
					
					UUID requestUuid = task.getRequestUuid();
					Request response = new Request(requestUuid, task.getImage(), task.getPixelCount());
					
					ClientHandle handle = requestMap.get(requestUuid);
					handle.sendResponse(response);
					
					task.served();
					System.out.println(getName() + " job completed " + task);
					headnode.justOut(task);
					CSVWriter.getJobs().writeLine(
							task.getUuid(),
							task.getImage().length,
							task.getTimeQueued(),
							task.getTimeScheduled(),
							task.getTimeProcessed(),
							task.getTimeServed(),
							task.getTimeWorkerReceived(),
							task.getTimeWorkerProcessed()							
					);
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