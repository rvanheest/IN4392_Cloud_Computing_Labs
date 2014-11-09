package emulator;

import head.HeadNode;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
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

import data.Timing;
import emulator.experiments.ExperimentSetups;

public class Emulator implements AutoCloseable {

	private final Socket socket;
	private final ObjectOutputStream out;
	private final ObjectInputStream in;

	private final ConcurrentMap<UUID, Long> sendTimes = new ConcurrentHashMap<>();
	private final BlockingQueue<Timing> completionTimes = new LinkedBlockingQueue<Timing>();

	private final List<BufferedImage> images = new ArrayList<>();

	private final InputThread input;
	private final LoggingThread logging;

	private final Logger logger = Logger.getLogger("emulator");

	public Emulator(String head, File dir) throws UnknownHostException, IOException {
		this.socket = new Socket(InetAddress.getByName(head), HeadNode.HeadClientPort);
		this.out = new ObjectOutputStream(this.socket.getOutputStream());
		this.in = new ObjectInputStream(this.socket.getInputStream());

		this.input = new InputThread(this.in, this.sendTimes, this.completionTimes);
		this.logging = new LoggingThread(this.completionTimes, this.logger);

		this.initLogger();
		this.bufferImages(dir);
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

	private void bufferImages(File imageDirectory) throws IOException {
		System.out.println("Preloading images");
		File[] imageFiles = imageDirectory.listFiles();
		for (File file : imageFiles)
			images.add(ImageIO.read(file));
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
				String[] tokens = consoleInput.readLine().split("\\s");
				String command = tokens[0];
				switch (command) {
					case "send":
						try {
							int num = Integer.parseInt(tokens[1]);

							int timeToSleep = Integer.parseInt(tokens[2]);

							new RandomOutputThread(this.out, this.sendTimes, num, timeToSleep,
									this.images).start();
						}
						catch (NumberFormatException e) {
							System.err.println("a number was not formatted correctly");
						}
						catch (ArrayIndexOutOfBoundsException e) {
							System.err.println("send should have the following format: \"send [number_of_images] [time_to_sleep]\"");
						}
						break;
					case "send-all":
						try {
							int sleepTime = Integer.parseInt(tokens[1]);

							new AllOutputThread(this.out, this.sendTimes, sleepTime, this.images)
									.start();
						}
						catch (NumberFormatException e) {
							System.err.println("a number was not formatted correctly");
						}
						catch (ArrayIndexOutOfBoundsException e) {
							System.err.println("send-all should have the following format: \"send-all [time_to_sleep]\"");
						}
						break;
					case "experiment":
						try {
							int experiment = Integer.parseInt(tokens[1]);

							switch (experiment) {
								case 1:
									new ExperimentOutputThread(ExperimentSetups.experiment1(
											this.out, this.sendTimes, this.images)).start();
									break;
								case 2:
									new ExperimentOutputThread(ExperimentSetups.experiment2(
											this.out, this.sendTimes, this.images)).start();
								default:
									System.out.println("experiment is not found");
							}
						}
						catch (NumberFormatException e) {
							System.err.println("a number was not formatted correctly");
						}
						break;
					case "pending_num":
						System.out.println(this.sendTimes.size());
						break;
					case "pending":
						for (Entry<UUID, Long> entry : this.sendTimes.entrySet()) {
							System.out.println(entry.getKey() + "\t" + entry.getValue());
						}
						break;
					case "pending_keys":
						for (UUID uuid : this.sendTimes.keySet()) {
							System.out.println(uuid);
						}
						break;
					case "ping":
						System.out.println("pong");
						break;
					case "break":
						isRunning = false;
						break;
					default:
						System.out.println("Unknown command " + command);
				}
			}
			System.out.println("Emulator command-line ended");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void beEmulator(String arg1, File arg2) {
		try (Emulator emu = new Emulator(arg1, arg2)) {
			emu.runCommandLine();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
