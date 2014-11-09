package imageProcessing.worker;

import head.Utils;
import imageProcessing.Burn;
import imageProcessing.Combine;
import imageProcessing.FlipHorizontal;
import imageProcessing.FlipVertical;
import imageProcessing.Gaussian;
import imageProcessing.GrayScaling;
import imageProcessing.Invert;
import imageProcessing.Noise;

import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

import data.Task;

class ProcessingThread extends Thread {

	private final BlockingQueue<Task> inputQueue;
	private final BlockingQueue<Task> outputQueue;

	public ProcessingThread(BlockingQueue<Task> inputQueue, BlockingQueue<Task> outputQueue) {
		super("ProcessingThread");
		this.inputQueue = inputQueue;
		this.outputQueue = outputQueue;
	}

	@Override
	public void run() {
		System.out.println(getName() + " started.");
		try {
			while (true) {
				Task inTask = this.inputQueue.take();
				inTask.workerProcessStarted();
				
				byte[] imageBytes = inTask.getImage();
				System.out.println("CLIENT_PROC - start: " + imageBytes.length);
				BufferedImage image = Utils.toBufferedImage(imageBytes);

				Callable<BufferedImage> gray = new GrayScaling(image);
				Callable<BufferedImage> noise = new Noise(image, 10, 200);
				Callable<BufferedImage> invert = new Invert(image);
				Callable<BufferedImage> burn = new Burn(image);
				Callable<BufferedImage> gaus = new Gaussian(image);
				Callable<BufferedImage> flipV = new FlipVertical(image);
				Callable<BufferedImage> flipH = new FlipHorizontal(image);

				Combine c1 = new Combine(gray, noise);
//				Combine c2 = new Combine(invert, burn);
				Combine c3 = new Combine(c1, flipV);
//				Combine c4 = new Combine(c2, flipH);
//				Combine c5 = new Combine(c3, c4);
//				Combine c6 = new Combine(c5, gaus, 0.8);
//
//				BufferedImage res = c6.call(); // executing all the filters recursively
				BufferedImage res = c3.call();
				System.out.println("CLIENT_PROC - finished: " + imageBytes.length);

				byte[] resBytes = Utils.toByteArray(res);
				Task outTask = new Task(inTask, resBytes);
				outTask.workerProcessEnded();
				this.outputQueue.put(outTask);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
