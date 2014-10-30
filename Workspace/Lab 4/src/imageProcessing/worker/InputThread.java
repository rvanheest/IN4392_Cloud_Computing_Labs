package imageProcessing.worker;

import java.io.ObjectInputStream;
import java.util.concurrent.BlockingQueue;

import data.Task;

class InputThread extends Thread {

	private final ObjectInputStream in;
	private final BlockingQueue<Task> inputQueue;

	public InputThread(ObjectInputStream in, BlockingQueue<Task> inputQueue) {
		super("InputThread");
		this.in = in;
		this.inputQueue = inputQueue;
	}

	@Override
	public void run() {
		System.out.println(getName() + " started.");
		try {
			while (true) {
				Task task = (Task) this.in.readObject();
				task.received();
				System.out.println("CLIENT_INPUT - received image: " + task.getImage().length);
				this.inputQueue.put(task);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}