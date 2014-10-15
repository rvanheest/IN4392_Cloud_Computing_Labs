package imageProcessing;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;

public class GrayScaling implements Callable<BufferedImage> {

	private final BufferedImage input;

	public GrayScaling(BufferedImage input) {
		this.input = input;
	}

	@Override
	public BufferedImage call() {
		int width = this.input.getWidth();
		int height = this.input.getHeight();

		BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				Color c = new Color(this.input.getRGB(j, i));
				int red = (int) (c.getRed() * 0.299);
				int green = (int) (c.getGreen() * 0.587);
				int blue = (int) (c.getBlue() * 0.114);
				Color newColor = new Color(red + green + blue, red + green + blue, red + green
						+ blue);
				dest.setRGB(j, i, newColor.getRGB());
			}
		}

		return dest;
	}
}
