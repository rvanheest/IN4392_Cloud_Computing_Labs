package emulator;

import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

import data.Timing;

class LoggingThread extends Thread {

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
