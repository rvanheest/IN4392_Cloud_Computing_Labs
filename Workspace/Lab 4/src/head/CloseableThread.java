package head;

abstract class CloseableThread
	extends Thread
	implements AutoCloseable
{
	public CloseableThread()
	{
	}
	
	public CloseableThread(String name)
	{
		super("[T-" + name + "]");
	}
}