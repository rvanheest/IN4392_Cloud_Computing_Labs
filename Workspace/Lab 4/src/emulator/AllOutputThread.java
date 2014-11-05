package emulator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import javax.imageio.ImageIO;

import tud.cc.Utils;
import data.Request;

class AllOutputThread extends Thread {

	private final ObjectOutputStream out;
	private final ConcurrentMap<UUID, Long> sendTimes;
	private final ArrayList<BufferedImage> images;
	private long timeToSleep;

	public AllOutputThread(ObjectOutputStream out,
			ConcurrentMap<UUID, Long> sendTimes, long timeToSleep,
			ArrayList<BufferedImage> images) {
		this.out = out;
		this.sendTimes = sendTimes;
		this.timeToSleep = timeToSleep;
		this.images = images;
	}

	@Override
	public void run() {
		try {
			for (BufferedImage image : images) {
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
