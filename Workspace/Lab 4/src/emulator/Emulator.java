package emulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
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

import tud.cc.HeadNode;
import data.Timing;

public class Emulator implements AutoCloseable {

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
				String command = consoleInput.readLine().trim();
				switch (command) {
					case "send":
						try {
							System.out.println("How many images do you want to send?");
							int num = Integer.parseInt(consoleInput.readLine().trim());

							System.out.println("How long should the interval be between sending "
									+ "two images (in milliseconds)?");
							int timeToSleep = Integer.parseInt(consoleInput.readLine().trim());

							new RandomOutputThread(this.directory, this.out, this.sendTimes,
									num, timeToSleep).start();
						}
						catch (NumberFormatException e) {
							System.err.println("a number was not formatted correctly");
						}
						break;
					case "send-all":
						try {
							System.out.println("How long should the interval be between sending "
									+ "two images (in milliseconds)?");
							int sleepTime = Integer.parseInt(consoleInput.readLine().trim());

							new AllOutputThread(this.directory, this.out, this.sendTimes, sleepTime)
									.start();
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
