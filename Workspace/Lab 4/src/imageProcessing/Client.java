package imageProcessing;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;

public class Client {
	
	private static class InputThread extends Thread {

		private final ObjectInputStream in;
		private final BlockingQueue<byte[]> inputQueue;

		public InputThread(ObjectInputStream in, BlockingQueue<byte[]> inputQueue) {
			this.in = in;
			this.inputQueue = inputQueue;
		}
		
		@Override
		public void run() {
			try {
    			while (true) {
   					byte[] imageBytes = (byte[]) this.in.readObject();
   					System.out.println("CLIENT_INPUT - received image: " + imageBytes.length);
					this.inputQueue.put(imageBytes);
				}
			}
    		catch (Exception e) {
    			e.printStackTrace();
    		}
		}
	}

	private static class OutputThread extends Thread {

		private final ObjectOutputStream out;
		private final BlockingQueue<byte[]> outputQueue;

		public OutputThread(ObjectOutputStream out, BlockingQueue<byte[]> outputQueue) {
			this.out = out;
			this.outputQueue = outputQueue;
		}
		
		@Override
		public void run() {
			try {
    			while (true) {
					byte[] imageBytes = this.outputQueue.take();
					System.out.println("CLIENT_OUTPUT - received image: " + imageBytes.length);
					this.out.writeObject(imageBytes);
				}
			}
    		catch (Exception e) {
    			e.printStackTrace();
    		}
		}
	}

	private static class ProcessingThread extends Thread {

		private final BlockingQueue<byte[]> inputQueue;
		private final BlockingQueue<byte[]> outputQueue;

		public ProcessingThread(BlockingQueue<byte[]> inputQueue, BlockingQueue<byte[]> outputQueue) {
			this.inputQueue = inputQueue;
			this.outputQueue = outputQueue;
		}
		
		@Override
		public void run() {
			try {
    			while (true) {
    				byte[] imageBytes = this.inputQueue.take();
    				System.out.println("CLIENT_PROC - start: " + imageBytes.length);
    				BufferedImage image = this.toBufferedImage(imageBytes);
        			
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
        			Combine c6 = new Combine(c5, gaus, 0.8);
        
        			BufferedImage res = c6.call();	//executing all the filters recursively
        			System.out.println("CLIENT_PROC - finished: " + imageBytes.length);
        			
        			byte[] resBytes = toByteArray(res);
        			this.outputQueue.put(resBytes);
    			}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		private byte[] toByteArray(BufferedImage image) throws IOException {
			try (ByteArrayOutputStream outbytes = new ByteArrayOutputStream()) {
				ImageIO.write(image, "JPG", outbytes);
				return outbytes.toByteArray();
			}
		}

		private BufferedImage toBufferedImage(byte[] bytes) throws IOException {
			try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
				return ImageIO.read(bais);
			}
		}
	}

	
	public void startProcessing(String head)
	{
		try (Socket socket = new Socket(InetAddress.getByName(head), 6048);
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
			BlockingQueue<byte[]> inputQueue = new LinkedBlockingQueue<>();
			BlockingQueue<byte[]> outputQueue = new LinkedBlockingQueue<>();
			
			InputThread input = new InputThread(in, inputQueue);
			OutputThread output = new OutputThread(out, outputQueue);
			ProcessingThread process1 = new ProcessingThread(inputQueue, outputQueue);
			ProcessingThread process2 = new ProcessingThread(inputQueue, outputQueue);
			
			System.out.println("Starting processing threads...");
			
			input.start();
			output.start();
			process1.start();
			process2.start();
			
			System.out.println("All processing threads started.");
			
			System.in.read();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	
	public static void main(String[] args) {
		Client client = new Client();
		client.startProcessing(args[0]);
	}
}
