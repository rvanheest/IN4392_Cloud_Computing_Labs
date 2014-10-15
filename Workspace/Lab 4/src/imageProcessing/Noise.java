package imageProcessing;

import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.concurrent.Callable;

public class Noise implements Callable<BufferedImage> {

	private final BufferedImage input;
	private final int quality;
	private final int threshold;

	public Noise(BufferedImage input, int quality, int threshold) {
		this.input = input;
		this.quality = quality;
		this.threshold = threshold;
	}

	@Override
	public BufferedImage call() {
		int width = this.input.getWidth();
		int height = this.input.getHeight();

		BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		Random r = new Random();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int px = this.input.getRGB(x, y);
				int ran = r.nextInt(this.quality);
				if (ran <= 1) {
					int amount = r.nextInt(this.threshold);
					int red = ((px >> 16) & 0xFF) + amount;

					amount = r.nextInt(this.threshold);
					int green = ((px >> 8) & 0xFF) + amount;

					amount = r.nextInt(this.threshold);
					int blue = (px & 0xFF) + amount;

					// Overflow fix
					if (red > 255) {
						red = 255;
					}
					if (green > 255) {
						green = 255;
					}
					if (blue > 255) {
						blue = 255;
					}

					px = (0xFF << 24) + (red << 16) + (green << 8) + blue;
				}
				dest.setRGB(x, y, px);
			}
		}

		return dest;
	}
}
