package emulator.experiments;

import java.awt.image.BufferedImage;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import emulator.RandomOutputThread;

public class EmulatedUserThread extends Thread {

	private final EmulatedUser user;
	private final RandomOutputThread output;

	public EmulatedUserThread(EmulatedUser user, ObjectOutputStream out,
			ConcurrentMap<UUID, Long> sendTimes, List<BufferedImage> images) {
		this.user = user;
		this.output = new RandomOutputThread(out, sendTimes, user.getImageCount(),
				user.getIntermediateSleep(), images);
	}

	@Override
	public void run() {
		try {
			sleep(this.user.getStartSleep());
			this.output.run();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
