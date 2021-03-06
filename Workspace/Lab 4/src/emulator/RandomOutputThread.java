package emulator;

import head.Utils;

import java.awt.image.BufferedImage;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import data.Request;

public class RandomOutputThread extends Thread {

	private final ObjectOutputStream out;
	private final ConcurrentMap<UUID, Long> sendTimes;
	private final List<BufferedImage> images;
	private final Random random = new Random();
	private int num;
	private final long timeToSleep;

	public RandomOutputThread(ObjectOutputStream out,
			ConcurrentMap<UUID, Long> sendTimes, int num, long timeToSleep,
			List<BufferedImage> images) {
		this.out = out;
		this.sendTimes = sendTimes;
		this.num = num;
		this.timeToSleep = timeToSleep;
		this.images = images;
	}

	@Override
	public void run() {
		try {
			while (this.num > 0) {
				int index = this.random.nextInt(images.size()); // bound: [0, images.length)
				BufferedImage image = images.get(index);

				byte[] bytesToBeSend = Utils.toByteArray(image);
				UUID uuid = UUID.randomUUID();
				Request request = new Request(uuid, bytesToBeSend, image.getHeight() * image.getWidth());

				//System.out.println("EMULATOR_OUTPUT - sending image: " + request);
				System.out.println("OUT: remaining " + this.num);
				synchronized (this.out) {
					this.out.writeObject(request);
				}

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
