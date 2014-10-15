package imageProcessing;

import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;

public class Gaussian implements Callable<BufferedImage> {

	private final BufferedImage input;

	public Gaussian(BufferedImage input) {
		this.input = input;
	}

	@Override
	public BufferedImage call() {
		int cuttoff = 2000;
		double magic = 1.442695;

		int width = this.input.getWidth();
		int height = this.input.getHeight();
		int xcenter = width / 2 - 1;
		int ycenter = height / 2 - 1;

		BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int px = this.input.getRGB(x, y);

				double distance = Math.sqrt(Math.pow(x - xcenter, 2) + Math.pow(y - ycenter, 2));
				double value = px * 255
						* Math.exp((-1 * distance * distance) / (magic * cuttoff * cuttoff));
				dest.setRGB(x, y, (int) value);
			}
		}

		return dest;
	}
}
