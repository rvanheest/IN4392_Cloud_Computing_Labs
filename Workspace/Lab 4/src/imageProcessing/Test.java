package imageProcessing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;

public class Test {

	public static void main(String[] args) throws Exception {
		BufferedImage image = ImageIO.read(new File("testimg/Eiger.JPG"));

		Callable<BufferedImage> gray = new GrayScaling(image);
		Callable<BufferedImage> noise = new Noise(image, 10, 200);
		Callable<BufferedImage> invert = new Invert(image);
		Callable<BufferedImage> burn = new Burn(image);
		Callable<BufferedImage> gaus = new Gaussian(image);
		Callable<BufferedImage> flipV = new FlipVertical(image);
		Callable<BufferedImage> flipH = new FlipHorizontal(image);

		Combine c1 = new Combine(gray, noise);
		Combine c2 = new Combine(invert, burn);
		Combine c3 = new Combine(c1, flipV);
		Combine c4 = new Combine(c2, flipH);
		Combine c5 = new Combine(c3, c4);

		long t1 = System.currentTimeMillis();
		BufferedImage comb = new Combine(c5, gaus, 0.8).call();
		
		System.out.println(System.currentTimeMillis() - t1);
		
		File output = new File("testimg/EigerCombVH.JPG");
		ImageIO.write(comb, "JPG", output);
	}
}
