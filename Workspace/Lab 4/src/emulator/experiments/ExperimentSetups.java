package emulator.experiments;

import java.awt.image.BufferedImage;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

public enum ExperimentSetups {;

	public static Iterable<EmulatedUserThread> experiment1(ObjectOutputStream out,
			ConcurrentMap<UUID, Long> sendTimes, List<BufferedImage> images) {
		return template(out, sendTimes, images, exp1Users());
	}

	private static Iterable<EmulatedUserThread> template(ObjectOutputStream out,
			ConcurrentMap<UUID, Long> sendTimes, List<BufferedImage> images,
			Collection<EmulatedUser> users) {
		Collection<EmulatedUserThread> threads = new ArrayList<>(users.size()); 
		
		for (EmulatedUser user : users) {
			threads.add(new EmulatedUserThread(user, out, sendTimes, images));
		}
		
		return threads;
	}
	
	private static Collection<EmulatedUser> exp1Users() {
		return Arrays.asList(new EmulatedUser(0, 100, 1000));
	}
}
