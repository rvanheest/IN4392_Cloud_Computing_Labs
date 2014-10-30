package tud.cc;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Utils {

	public static byte[] toByteArray(BufferedImage image) throws IOException {
		try (ByteArrayOutputStream outbytes = new ByteArrayOutputStream()) {
			ImageIO.write(image, "JPG", outbytes);
			return outbytes.toByteArray();
		}
	}

	public static BufferedImage toBufferedImage(byte[] bytes) throws IOException {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
			return ImageIO.read(bais);
		}
	}
}
