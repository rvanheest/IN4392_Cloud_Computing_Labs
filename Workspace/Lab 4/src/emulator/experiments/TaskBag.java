package emulator.experiments;

import java.awt.image.BufferedImage;
import java.util.Collection;

public class TaskBag {
	
	private final Collection<BufferedImage> images;
	private final long timeToSleep;
	
	public TaskBag(Collection<BufferedImage> images, long timeToSleep) {
		this.images = images;
		this.timeToSleep = timeToSleep;
	}
	
	public Collection<BufferedImage> getImages() {
		return images;
	}
	
	public long getTimeToSleep() {
		return timeToSleep;
	}
}