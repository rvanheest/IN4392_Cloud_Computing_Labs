package tud.cc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import amazonTests.Configurations;
import amazonTests.EC2CloudService;
import amazonTests.NodeDetails;
import data.Sample;
import data.Task;
import emulator.Emulator;


public class HeadNode 
	implements AutoCloseable
{
	
	public static final int HeadWorkerPort = 6048;
	public static final int HeadClientPort = 6049;	
	
	private static ExecutorService _service = null;
	private static EC2CloudService _cloudService = null;
	static EC2CloudService getService()
	{
		if (_cloudService == null)
		{
			 _service = Executors.newFixedThreadPool(8);
			 _cloudService = new EC2CloudService("AwsCredentials.properties", "CC", "ec2.eu-west-1.amazonaws.com", _service);
		}
		return _cloudService;
	}

	private final BlockingDeque<Task> jobQueue = new java.util.concurrent.LinkedBlockingDeque<Task>();
	private final BlockingQueue<Task> processed = new java.util.concurrent.LinkedBlockingDeque<Task>();
	private final ConcurrentHashMap<String, WorkerHandle> workerPool = new ConcurrentHashMap<>();
	private final Map<String, NodeDetails> expectedWorkerDetails = Collections.synchronizedMap(new HashMap<String, NodeDetails>());
	private final Map<UUID, ClientHandle> requestMap = new ConcurrentHashMap<>();	
	
	
	private final Deque<CloseableThread> threads = new LinkedBlockingDeque<>();
	private final WorkerReceptionThread workerReceptionThread;
	private final ClientReceptionThread clientReceptionThread;
	private final SchedulerThread schedulerThread;
	private final ResponderThread responderThread;
	private final MonitorThread monitorThread;
	
	
	private final List<Task> sampling_TasksJustIn = Collections.synchronizedList(new ArrayList<Task>());
	private final List<Task> sampling_TasksJustOut = Collections.synchronizedList(new ArrayList<Task>());
	public void justIn(Task task) { synchronized (sampling_TasksJustIn) { sampling_TasksJustIn.add(task); }}
	public void justOut(Task task) { synchronized (sampling_TasksJustOut) { sampling_TasksJustOut.add(task); }}
	
	
	public boolean isLeasing()
	{ 
		return expectedWorkerDetails.size() != 0;
	}
	
	
	public HeadNode() throws IOException
	{
		System.out.println("Initialising head node...");
		
		// Start all the threads
		threads.add(this.workerReceptionThread = new WorkerReceptionThread(HeadWorkerPort, workerPool, processed, threads, expectedWorkerDetails));
		threads.add(this.clientReceptionThread = new ClientReceptionThread(this, HeadClientPort, jobQueue, threads, requestMap));
		threads.add(this.schedulerThread = new SchedulerThread(jobQueue, workerPool));
		threads.add(this.responderThread = new ResponderThread(this, processed, requestMap));
		threads.add(this.monitorThread = new MonitorThread(this));
		
		for (Thread t : threads)
			t.start();
	}
	
	
	/**
	 * Get all the live workers
	 * @return
	 */
	public Collection<WorkerHandle> getWorkers()
	{
		return Collections.unmodifiableCollection(this.workerPool.values());
	}
	
	
	/**
	 * AutoCloseable implementation
	 * @throws InterruptedException 
	 */
	@Override
	public void close() throws IOException, InterruptedException
	{
		// Close every thread
		for (CloseableThread t : threads)
		{
			try 
			{
				t.close();
			} catch (Exception e) {	}
		}
		
		// Join threads
		System.out.println("Waiting for terminating threads...");
		for (Thread t : threads)
				t.join();
		
		System.out.println("Head node closed");
	}


	/**
	 * A blocking function that starts a command-line loop on this thread.
	 */
	public void runCommandLine() 
	{
		try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in)))
		{
			System.out.println("Command-line started");
			boolean isCl = true; 
			while (isCl)
			{
				try
				{
					System.out.print("> ");
					String[] tokens = in.readLine().split("\\s");
					String command = tokens[0];
					switch (command)
					{
						case "workers":
						case "w":
							for (Entry<String, WorkerHandle> entry : workerPool.entrySet())
								System.out.println(entry.getValue());
							break;
						case "worker-details":
							for (String details : expectedWorkerDetails.keySet())
								System.out.println(details);
							break;
						case "queue":
						case "q":
							for (Task job : jobQueue)
								System.out.println(job);
							break;
						case "processed":
							for (Task job : processed)
								System.out.println(job);
							break;
						case "workload":
							System.out.println(this.takeSample().getWorkload());
							break;
						case "lease":
							System.out.println("Leasing a new worker...");
							NodeDetails workerDetails = startWorker();
							System.out.println("Leased: " + workerDetails);
							break;
						case "release":
							WorkerHandle handle = workerPool.get(tokens[1]);
							handle.close();
							break;
						case "sample":
							System.out.println(this.takeSample());
							break;
						case "history":
							int window = Integer.parseInt(tokens[1]);
							for (Sample sample : this.monitorThread.getHistory(window))
								System.out.println(sample);
							break;
						case "ping":
							System.out.println("pong");
							break;
						case "break":
						case "exit":
							isCl = false;
							break;
						case "threads":
							Thread[] allThreads = new Thread[Thread.activeCount()];
			                Thread.enumerate(allThreads);
			                for (Thread thread : allThreads)
			                	System.out.println(thread);
							break;
						default:
							System.out.println("Unknown command " + command);
							break;
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			System.out.println("Command-line ended");
		}
		catch (Exception e)	{ }
	}
	
	
	/**
	 * Deploys a new node and returns the IP of the node
	 * @return The details of the node just deployed
	 */
	public synchronized NodeDetails startWorker()
	{
		this.expectedWorkerDetails.put("Pending", null);
		
		NodeDetails details = getService().leaseNode(new Configurations("random", null));
		this.expectedWorkerDetails.put(details.getNodePrivateIP().getHostAddress(), details);
		this.expectedWorkerDetails.remove("Pending");
		
		return details;
	}
	
	
	/**
	 * Decommission a worker at random
	 * @throws Exception 
	 */
	public void decommissionRandom()
	{
		if (this.workerPool.size() < 1)
			throw new IllegalStateException("Cannot decommission worker because there are no workers");
		
		String worker = this.workerPool.keys().nextElement();
		WorkerHandle handle = this.workerPool.get(worker);
		this.workerPool.remove(worker);
		handle.setForDecommision();
	}
	
	
	public Sample takeSample()
	{
		Task queueHead = jobQueue.peek();
		
		int cores = 0;
		int jobsInWorkers = 0;
		for (WorkerHandle worker : this.workerPool.values())
		{
			cores += worker.handshake.cores;
			jobsInWorkers += worker.getJobsInProcess().size();
		}
		int jobsIn, jobsOut = -1;
		synchronized (this.sampling_TasksJustIn)
		{
			jobsIn = this.sampling_TasksJustIn.size();
			this.sampling_TasksJustIn.clear();
		}
		synchronized (this.sampling_TasksJustOut)
		{
			jobsOut = this.sampling_TasksJustOut.size();
			this.sampling_TasksJustOut.clear();
		}
		
		return new Sample
		(
				jobQueue.size(),
				(queueHead != null) ? System.currentTimeMillis() - queueHead.getTimeQueued() : 0,
				cores,
				workerPool.size(),
				jobsInWorkers,
				jobsIn,
				jobsOut
		);
	}
	
	
	public static void beHead()
	{
		try (HeadNode head = new HeadNode();)
		{
			cleanup.add(head);
			head.runCommandLine();
		}
		catch (IOException | InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Add here everything that needs to cleaned up on sudden termination.
	 */
	private static final Collection<AutoCloseable> cleanup = Collections.synchronizedList(new ArrayList<AutoCloseable>());
	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException
	{
		// Add kill hook
		Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                System.out.println("Kill received! Cleaning up...");
                for (AutoCloseable mess : cleanup)
                {
					try {
						mess.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
                }
            }
        });
		
		
		switch (args[0])
		{
			case "head":
				HeadNode.beHead();
				break;
			case "worker":
				WorkerNode.beWorker(args[1]);
				break;
			case "emulator":
				Emulator.beEmulator(args[1], new File(args[2]));
				break;
			default:
				System.err.println("Unknown role: " + args[0]);
				return;
		}
		
		
		// Discover leaked threads
		Thread[] allThreads = new Thread[Thread.activeCount()];
        Thread.enumerate(allThreads);
        if (allThreads.length > 1)
        {
        	System.out.println("\nDEBUG: thread leak:");
	        for (Thread thread : allThreads)
	        	if (!Thread.currentThread().equals(thread))
		        	System.out.println(thread);
	        System.out.println("DEBUG: forcing exit\n");
	        System.exit(1);
        }
	}
	
}
