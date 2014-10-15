package imageProcessing;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Test {

	public static void main(String[] args) throws IOException {
		BufferedImage image = ImageIO.read(new File("testimg/Apen.JPG"));
		
		long t1 = System.currentTimeMillis();
		BufferedImage gray = new GrayScaling(image).call();
//		BufferedImage noise = new Noise(image, 10, 200).call();
//		BufferedImage invert = new Invert(image).call();
//		BufferedImage burn = new Burn(image).call();
//		BufferedImage gaus = new Gaussian(image).call();
//		BufferedImage comb = new Combine(gray, gaus, 0.9).call();
		BufferedImage flipV = new FlipVertical(gray).call();
		BufferedImage flipH = new FlipHorizontal(gray).call();
		BufferedImage comb = new Combine(flipV, flipH, 0.5).call();
		
		System.out.println(System.currentTimeMillis() - t1);
		
		File output = new File("testimg/EigerCombVH.JPG");
		ImageIO.write(comb, "JPG", output);
	}
}
