package emulator.experiments;

public class EmulatedUser {

	private final long startSleep;
	private final int imageCount;
	private final long intermediateSleep;

	public EmulatedUser(long startSleep, int imageCount, long intermediateSleep) {
		this.startSleep = startSleep;
		this.imageCount = imageCount;
		this.intermediateSleep = intermediateSleep;
	}

	public long getStartSleep() {
		return startSleep;
	}

	public int getImageCount() {
		return imageCount;
	}

	public long getIntermediateSleep() {
		return intermediateSleep;
	}
}
