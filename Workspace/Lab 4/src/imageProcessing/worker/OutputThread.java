package imageProcessing.worker;

import java.io.ObjectOutputStream;
import java.util.concurrent.BlockingQueue;

import data.Task;

class OutputThread extends Thread {

	private final ObjectOutputStream out;
	private final BlockingQueue<Task> outputQueue;

	public OutputThread(ObjectOutputStream out, BlockingQueue<Task> outputQueue) {
		super("OutputThread");
		this.out = out;
		this.outputQueue = outputQueue;
	}

	@Override
	public void run() {
		System.out.println(getName() + " started.");
		try {
			while (true) {
				Task task = this.outputQueue.take();
				task.processed();
				System.out.println("CLIENT_OUTPUT - received image: " + task.getImage().length);
				this.out.writeObject(task);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
