package imageProcessing;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;

public class Combine implements Callable<BufferedImage> {

	private final Callable<BufferedImage> input1;
	private final Callable<BufferedImage> input2;
	private final double part;

	public Combine(Callable<BufferedImage> input1, Callable<BufferedImage> input2, double part) {
		this.input1 = input1;
		this.input2 = input2;
		this.part = part;
	}

	public Combine(Callable<BufferedImage> input1, Callable<BufferedImage> input2) {
		this(input1, input2, 0.5);
	}

	@Override
	public BufferedImage call() throws Exception {
		BufferedImage im1 = this.input1.call();
		BufferedImage im2 = this.input2.call();
		
		int width = im1.getWidth();
		int height = im1.getHeight();

		BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				Color c1 = new Color(im1.getRGB(x, y));
				Color c2 = new Color(im2.getRGB(x, y));
				
				int red = (int) (c1.getRed() * this.part + c2.getRed() * (1 - this.part));
				int green = (int) (c1.getGreen() * this.part + c2.getGreen() * (1 - this.part));
				int blue = (int) (c1.getBlue() * this.part + c2.getBlue() * (1 - this.part));
				
				Color combined = new Color(red, green, blue);
				dest.setRGB(x, y, combined.getRGB());
			}
		}

		return dest;
	}
}
