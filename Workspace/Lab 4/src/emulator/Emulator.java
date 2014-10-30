package emulator;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.imageio.ImageIO;

import tud.cc.HeadNode;
import tud.cc.Utils;
import data.Request;
import data.Timing;

public class Emulator implements AutoCloseable {

	private static class InputThread extends Thread {
		
		private final ObjectInputStream in;
		private final ConcurrentMap<UUID, Long> sendTimes;
		private final BlockingQueue<Timing> completionTimes;

		public InputThread(ObjectInputStream in, ConcurrentMap<UUID, Long> sendTimes, BlockingQueue<Timing> completionTimes) {
			this.in = in;
			this.sendTimes = sendTimes;
			this.completionTimes = completionTimes;
		}

		@Override
		public void run() {
			try {
				while (true) {
					Request request = (Request) this.in.readObject();
					long t2 = System.currentTimeMillis();
					System.out.println("EMULATOR_INPUT - received image: " + request);
					
					Long t1 = this.sendTimes.remove(request.getId());
					Timing timing = new Timing(request.getId(), t2 - t1);
					this.completionTimes.put(timing);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static class RandomOutputThread extends Thread {
		
		private final File imageDirectory;
		private final ObjectOutputStream out;
		private final ConcurrentMap<UUID, Long> sendTimes;
		private final Random random = new Random();
		private int num;
		private long timeToSleep;

		public RandomOutputThread(File imageDirectory, ObjectOutputStream out,
				ConcurrentMap<UUID, Long> sendTimes, int num, long timeToSleep) {
			this.imageDirectory = imageDirectory;
			this.out = out;
			this.sendTimes = sendTimes;
			this.num = num;
			this.timeToSleep = timeToSleep;
		}
		
		@Override
		public void run() {
			try {
				while (this.num > 0) {
					File[] images = this.imageDirectory.listFiles();
					int index = this.random.nextInt(images.length); // bound: [0, images.length)
					File imageToBeSend = images[index];
					BufferedImage image = ImageIO.read(imageToBeSend);
					
					byte[] bytesToBeSend = Utils.toByteArray(image);
					UUID uuid = UUID.randomUUID();
					Request request = new Request(uuid, bytesToBeSend);
					
					System.out.println("EMULATOR_OUTPUT - sending image: " + request);
					this.out.writeObject(request);
					
					long t1 = System.currentTimeMillis();
					this.sendTimes.put(uuid, t1);
					
					this.num--;
					
					sleep(this.timeToSleep);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static class AllOutputThread extends Thread {
		
		private final File imageDirectory;
		private final ObjectOutputStream out;
		private final ConcurrentMap<UUID, Long> sendTimes;
		private long timeToSleep;

		public AllOutputThread(File imageDirectory, ObjectOutputStream out,
				ConcurrentMap<UUID, Long> sendTimes, long timeToSleep) {
			this.imageDirectory = imageDirectory;
			this.out = out;
			this.sendTimes = sendTimes;
			this.timeToSleep = timeToSleep;
		}
		
		@Override
		public void run() {
			try {
				for (File file : this.imageDirectory.listFiles()) {
					BufferedImage image = ImageIO.read(file);
					
					byte[] bytesToBeSend = Utils.toByteArray(image);
					UUID uuid = UUID.randomUUID();
					Request request = new Request(uuid, bytesToBeSend);
					
					System.out.println("EMULATOR_OUTPUT - sending image: " + request);
					this.out.writeObject(request);
					
					long t1 = System.currentTimeMillis();
					this.sendTimes.put(uuid, t1);
					
					sleep(this.timeToSleep);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static class LoggingThread extends Thread {
		
		private final BlockingQueue<Timing> completionTimes;
		private final Logger logger;
		
		public LoggingThread(BlockingQueue<Timing> completionTimes, Logger logger) {
			this.completionTimes = completionTimes;
			this.logger = logger;
		}

		@Override
		public void run() {
			try {
    			while (true) {
    				Timing timing = this.completionTimes.take();
    				this.logger.info(timing.getId() + "\t" + timing.getTime());
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
	
	private final ConcurrentMap<UUID, Long> sendTimes = new ConcurrentHashMap<>();
	private final BlockingQueue<Timing> completionTimes = new LinkedBlockingQueue<Timing>();
	
	private final InputThread input;
	private final LoggingThread logging;
	
	private final File directory;
	private final Logger logger = Logger.getLogger("emulator");

	public Emulator(String head, File dir) throws UnknownHostException, IOException {
		this.socket = new Socket(InetAddress.getByName(head), HeadNode.HeadClientPort);
		this.out = new ObjectOutputStream(this.socket.getOutputStream());
		this.in = new ObjectInputStream(this.socket.getInputStream());
		
		this.input = new InputThread(this.in, this.sendTimes, this.completionTimes);
		this.logging = new LoggingThread(this.completionTimes, this.logger);
		
		this.directory = dir;
		
		this.initLogger();
		this.startProcessing();
	}

	private void initLogger() {
		try {
			Handler consoleHandler = new ConsoleHandler();
			Handler fileHandler = new FileHandler("emulator-log.log");
			Formatter simpleFormatter = new SimpleFormatter();
			
			consoleHandler.setFormatter(simpleFormatter);
			fileHandler.setFormatter(simpleFormatter);
			this.logger.addHandler(consoleHandler);
			this.logger.addHandler(fileHandler);
			
		}
		catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
	}

	private void startProcessing() {
		this.input.start();
		this.logging.start();
	}

	@Override
	public void close() throws IOException {
		this.in.close();
		this.out.close();
		this.socket.close();
	}

	public void runCommandLine() {
		try (BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in))) {
			System.out.println("Emulator command-line started");
			boolean isRunning = true;
			while (isRunning) {
				System.out.print("> ");
				String command = consoleInput.readLine();
				switch (command) {
					case "send":
						try {
    						System.out.println("How many images do you want to send?");
    						int num = Integer.parseInt(consoleInput.readLine());
    						
    						System.out.println("How long should the interval be between sending two images (in milliseconds)?");
    						int timeToSleep = Integer.parseInt(consoleInput.readLine());
    						
    						new RandomOutputThread(this.directory, this.out, this.sendTimes, num, timeToSleep).start();
						}
						catch (NumberFormatException e) {
							System.err.println("a number was not formatted correctly");
						}
						break;
					case "send-all":
						try {
    						System.out.println("How long should the interval be between sending two images (in milliseconds)?");
    						int sleepTime = Integer.parseInt(consoleInput.readLine());
    						
    						new AllOutputThread(this.directory, this.out, this.sendTimes, sleepTime).start();
						}
						catch (NumberFormatException e) {
							System.err.println("a number was not formatted correctly");
						}
						break;
					case "ping":
						System.out.println("pong");
						break;
					case "break":
						isRunning = false;
						break;
					default: System.out.println("Unknown command " + command);
				}
			}
			System.out.println("Emulator command-line ended");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	// connect to the head via socket
	// send a path every second
}
