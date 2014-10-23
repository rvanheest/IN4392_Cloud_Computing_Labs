package imageProcessing;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.imageio.ImageIO;

public class Server {

	public static void main(String[] args) {
		try (ServerSocket serverSocket = new ServerSocket(4444);
				Socket clientSocket = serverSocket.accept();
				ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {
			BufferedImage[] images = new BufferedImage[] {
					ImageIO.read(new File("testimg/Eiger.JPG")),
					ImageIO.read(new File("testimg/Apen.JPG"))
			};
			
			for (BufferedImage image : images) {
    			byte[] imageBytes = toByteArray(image);
    			System.out.println("SERVER - bytes: " + imageBytes.length);
    			
    			out.writeObject(imageBytes);
    			System.out.println("SERVER - send image");
			}
			
			for (int i = 0; i < images.length; i++) {
    			byte[] resultBytes = (byte[]) in.readObject();
    			System.out.println("SERVER - received bytes: " + resultBytes.length);
    			
    			BufferedImage result = toBufferedImage(resultBytes);
    			System.out.println("SERVER - received image: " + result);
    			
    			File output = new File("testimg/Result" + i + ".JPG");
    			ImageIO.write(result, "JPG", output);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

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
