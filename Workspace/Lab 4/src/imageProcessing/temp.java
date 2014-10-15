package imageProcessing;

import java.awt.image.BufferedImage;

public class temp {

	public static BufferedImage histogramThreshold(BufferedImage img, int threshold) {

		BufferedImage dest = new BufferedImage(img.getWidth(), img.getHeight(),
				BufferedImage.TYPE_INT_ARGB);

		int reds[] = new int[256];
		int greens[] = new int[256];
		int blues[] = new int[256];

		// Count the occurance of each pixel's red, green and blue
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				int px = img.getRGB(x, y);

				int red = ((px >> 16) & 0xFF);
				reds[red]++;

				int green = ((px >> 8) & 0xFF);
				greens[green]++;

				int blue = (px & 0xFF);
				blues[blue]++;

				dest.setRGB(x, y, px);
			}
		}

		// Analyse the results
		int mostCommonRed = 0;
		int mostCommonBlue = 0;
		int mostCommonGreen = 0;

		for (int i = 0; i < 256; i++) {
			if (reds[i] > mostCommonRed) {
				mostCommonRed = i;
			}

			if (blues[i] > mostCommonBlue) {
				mostCommonBlue = i;
			}

			if (greens[i] > mostCommonGreen) {
				mostCommonGreen = i;
			}
		}

		// Set the destination to pixels that are in a range +/- threshold from mostCommon value
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				int px = img.getRGB(x, y);

				int red = ((px >> 16) & 0xFF);
				int green = ((px >> 8) & 0xFF);
				int blue = (px & 0xFF);
				int val = 0;

				if (((red - 20 < mostCommonRed) && (red + threshold > mostCommonRed))
						|| ((blue - threshold < mostCommonBlue) && (blue + threshold > mostCommonBlue))
						|| ((green - threshold < mostCommonGreen) && (green + threshold > mostCommonGreen))) {
					val = (0xFF << 24) + (red << 16) + (green << 8) + blue;
				}
				else {
					val = (0xFF << 24) + (0xFF << 16) + (0xFF << 8) + 0xFF;
				}

				dest.setRGB(x, y, val);
			}
		}

		return dest;
	}

	public static BufferedImage greyScale(BufferedImage img) {
		BufferedImage dest = new BufferedImage(img.getWidth(), img.getHeight(),
				BufferedImage.TYPE_INT_ARGB);

		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {

				int px = img.getRGB(x, y);

				int alpha = (px >> 24) & 0xFF;
				int red = (px >> 16) & 0xFF;
				int green = (px >> 8) & 0xFF;
				int blue = px & 0xFF;

				// average of RGB
				int avg = (red + blue + green) / 3;

				// set R, G & B with avg color
				int grey = (alpha << 24) + (avg << 16) + (avg << 8) + avg;

				dest.setRGB(x, y, grey);
			}
		}

		return dest;
	}

	public static BufferedImage flipVerticalHorizontal(BufferedImage img) {
		BufferedImage dest = new BufferedImage(img.getWidth(), img.getHeight(),
				BufferedImage.TYPE_INT_ARGB);

		// Flip vertical and horizontal
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				int px = img.getRGB(x, y);
				int destX = img.getWidth() - x - 1;
				int destY = img.getHeight() - y - 1;
				dest.setRGB(destX, destY, px);
			}
		}

		return dest;
	}

	public static BufferedImage flipVertical(BufferedImage img) {
		BufferedImage dest = new BufferedImage(img.getWidth(), img.getHeight(),
				BufferedImage.TYPE_INT_ARGB);

		// Flip vertical and horizontal
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				int px = img.getRGB(x, y);
				dest.setRGB(x, img.getHeight() - y - 1, px);
			}
		}

		return dest;
	}

	public static BufferedImage flipHorizontal(BufferedImage img) {
		BufferedImage dest = new BufferedImage(img.getWidth(), img.getHeight(),
				BufferedImage.TYPE_INT_ARGB);

		// Flip horizontal
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				int px = img.getRGB(x, y);
				dest.setRGB(img.getWidth() - x - 1, y, px);
			}
		}

		return dest;
	}
}
