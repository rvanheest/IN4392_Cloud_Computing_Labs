package emulator;

import emulator.experiments.EmulatedUserThread;

public class ExperimentOutputThread extends Thread {

	private final Iterable<EmulatedUserThread> users;

	public ExperimentOutputThread(Iterable<EmulatedUserThread> users) {
		this.users = users;
	}

	@Override
	public void run() {
		try {
			for (EmulatedUserThread user : this.users) {
				user.start();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
