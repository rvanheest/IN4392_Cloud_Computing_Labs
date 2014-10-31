package emulator;

import java.io.ObjectInputStream;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

import data.Request;
import data.Timing;

class InputThread extends Thread {

	private final ObjectInputStream in;
	private final ConcurrentMap<UUID, Long> sendTimes;
	private final BlockingQueue<Timing> completionTimes;

	public InputThread(ObjectInputStream in, ConcurrentMap<UUID, Long> sendTimes,
			BlockingQueue<Timing> completionTimes) {
		this.in = in;
		this.sendTimes = sendTimes;
		this.completionTimes = completionTimes;
	}

	@Override
	public void run() {
		try {
			while (true) {
				Request request = (Request) this.in.readObject();
				long t2 = System.currentTimeMillis();
				System.out.println("EMULATOR_INPUT - received image: " + request);

				Long t1 = this.sendTimes.remove(request.getId());
				Timing timing = new Timing(request.getId(), t2 - t1);
				this.completionTimes.put(timing);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
