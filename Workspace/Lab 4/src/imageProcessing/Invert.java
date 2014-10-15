package imageProcessing;

import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;

public class Invert implements Callable<BufferedImage> {

	private final BufferedImage input;

	public Invert(BufferedImage input) {
		this.input = input;
	}

	@Override
	public BufferedImage call() {
		int width = this.input.getWidth();
		int height = this.input.getHeight();

		BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int px = this.input.getRGB(x, y);

				// Subtracting the channels value from 0xFF effectively inverts it
				int red = 0xFF - ((px >> 16) & 0xFF);
				int green = 0xFF - ((px >> 8) & 0xFF);
				int blue = 0xFF - (px & 0xFF);

				int inverted = (0xFF << 24) + (red << 16) + (green << 8) + blue;
				dest.setRGB(x, y, inverted);
			}
		}

		return dest;
	}
}
