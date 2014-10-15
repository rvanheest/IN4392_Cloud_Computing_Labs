package imageProcessing;

import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;

public class Combine implements Callable<BufferedImage> {

	private final BufferedImage input1;
	private final BufferedImage input2;
	private final double part;

	public Combine(BufferedImage input1, BufferedImage input2, double part) {
		if (input1.getWidth() == input2.getWidth() && input1.getHeight() == input2.getHeight()) {
    		this.input1 = input1;
    		this.input2 = input2;
    		this.part = part;
		}
		else {
			throw new IllegalArgumentException("dimensions aren't the same!");
		}
	}

	@Override
	public BufferedImage call() {
		int width = this.input1.getWidth();
		int height = this.input1.getHeight();

		BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int px1 = this.input1.getRGB(x, y);
				int px2 = this.input2.getRGB(x, y);

				double combined = px1 * this.part + px2 * (1 - this.part);
				dest.setRGB(x, y, (int) combined);
			}
		}

		return dest;
	}
}
