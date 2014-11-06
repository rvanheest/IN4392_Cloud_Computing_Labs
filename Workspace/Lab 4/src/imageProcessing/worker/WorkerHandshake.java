package imageProcessing.worker;

import java.io.Serializable;


/**
 * Upon connecting, a worker sends an instance of this object to the head
 * 
 * @author Chris
 *
 */
public class WorkerHandshake
	implements Serializable
{
	private static final long serialVersionUID = -8896882167017437484L;
	
	/**
	 * The number of cores available to this worker
	 */
	public final int cores;
	
	public WorkerHandshake(int cores)
	{
		this.cores = cores;
	}	
}
