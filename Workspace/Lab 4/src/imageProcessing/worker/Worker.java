package imageProcessing.worker;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import data.Task;
import tud.cc.HeadNode;

public class Worker implements AutoCloseable {

	private final Socket socket;
	private final ObjectOutputStream out;
	private final ObjectInputStream in;

	private final BlockingQueue<Task> inputQueue = new LinkedBlockingQueue<>();
	private final BlockingQueue<Task> outputQueue = new LinkedBlockingQueue<>();

	private final InputThread input;
	private final OutputThread output;
	private final ArrayList<ProcessingThread> processes = new ArrayList<ProcessingThread>();

	public Worker(String head) throws UnknownHostException, IOException {
		// Establish connection
		this.socket = new Socket(InetAddress.getByName(head), HeadNode.HeadWorkerPort);
		this.out = new ObjectOutputStream(this.socket.getOutputStream());
		this.in = new ObjectInputStream(this.socket.getInputStream());

		// Greet
		this.out.writeObject(new WorkerHandshake(
			Runtime.getRuntime().availableProcessors()
		));
		
		// Set up processing belt
		this.input = new InputThread(in, inputQueue);
		this.output = new OutputThread(out, outputQueue);

		// Start one processing thread for each processor
		for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
			processes.add(new ProcessingThread(inputQueue, outputQueue));
		}

		this.startProcessing();
	}

	private void startProcessing() {
		System.out.println("Starting processing threads...");

		input.start();
		output.start();
		for (ProcessingThread process : processes) {
			process.start();
		}

		System.out.println("All processing threads started.");
	}

	@Override
	public void close() throws IOException {
		this.input.interrupt();
		this.output.interrupt();

		for (ProcessingThread process : processes) {
			process.interrupt();
		}

		this.in.close();
		this.out.close();
		this.socket.close();
	}
}
