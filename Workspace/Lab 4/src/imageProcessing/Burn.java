package imageProcessing;

import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;

public class Burn implements Callable<BufferedImage> {

	private final BufferedImage input;

	public Burn(BufferedImage input) {
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

				int burn = px << 8;

				dest.setRGB(x, y, burn);
			}
		}

		return dest;
	}
}
