package imageProcessing;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

import tud.cc.HeadNode;
import tud.cc.Task;
import tud.cc.Utils;

public class Worker implements AutoCloseable {

	private static class InputThread extends Thread {

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

	private static class OutputThread extends Thread {

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

	private static class ProcessingThread extends Thread {

		private final BlockingQueue<Task> inputQueue;
		private final BlockingQueue<Task> outputQueue;

		public ProcessingThread(BlockingQueue<Task> inputQueue, BlockingQueue<Task> outputQueue) {
			super("ProcessingThread");
			this.inputQueue = inputQueue;
			this.outputQueue = outputQueue;
		}

		@Override
		public void run() {
			System.out.println(getName() + " started.");
			try {
				while (true) {
					Task inTask = this.inputQueue.take();
					byte[] imageBytes = inTask.getImage();
					System.out.println("CLIENT_PROC - start: " + imageBytes.length);
					BufferedImage image = Utils.toBufferedImage(imageBytes);

					Callable<BufferedImage> gray = new GrayScaling(image);
					Callable<BufferedImage> noise = new Noise(image, 10, 200);
					Callable<BufferedImage> invert = new Invert(image);
					Callable<BufferedImage> burn = new Burn(image);
					Callable<BufferedImage> gaus = new Gaussian(image);
					Callable<BufferedImage> flipV = new FlipVertical(image);
					Callable<BufferedImage> flipH = new FlipHorizontal(image);

					Combine c1 = new Combine(gray, noise);
					Combine c2 = new Combine(invert, burn);
					Combine c3 = new Combine(c1, flipV);
					Combine c4 = new Combine(c2, flipH);
					Combine c5 = new Combine(c3, c4);
					Combine c6 = new Combine(c5, gaus, 0.8);

					BufferedImage res = c6.call(); // executing all the filters recursively
					System.out.println("CLIENT_PROC - finished: " + imageBytes.length);

					byte[] resBytes = Utils.toByteArray(res);
					this.outputQueue.put(new Task(inTask, resBytes));
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private final Socket socket;
	private final ObjectOutputStream out;
	private final ObjectInputStream in;

	private final BlockingQueue<Task> inputQueue = new LinkedBlockingQueue<>();
	private final BlockingQueue<Task> outputQueue = new LinkedBlockingQueue<>();

	private final InputThread input;
	private final OutputThread output;
	private final ProcessingThread process1;
	private final ProcessingThread process2;

	public Worker(String head) throws UnknownHostException, IOException {
		this.socket = new Socket(InetAddress.getByName(head), HeadNode.HeadWorkerPort);
		this.out = new ObjectOutputStream(this.socket.getOutputStream());
		this.in = new ObjectInputStream(this.socket.getInputStream());

		this.input = new InputThread(in, inputQueue);
		this.output = new OutputThread(out, outputQueue);
		this.process1 = new ProcessingThread(inputQueue, outputQueue);
		this.process2 = new ProcessingThread(inputQueue, outputQueue);
		
		this.startProcessing();
	}

	private void startProcessing() {
		System.out.println("Starting processing threads...");

		input.start();
		output.start();
		process1.start();
		process2.start();

		System.out.println("All processing threads started.");
	}

	@Override
	public void close() throws IOException {
		this.input.interrupt();
		this.output.interrupt();
		this.process1.interrupt();
		this.process2.interrupt();

		this.in.close();
		this.out.close();
		this.socket.close();
	}
}
