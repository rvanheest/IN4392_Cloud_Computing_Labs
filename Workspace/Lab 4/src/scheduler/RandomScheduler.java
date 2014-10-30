package scheduler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import tud.cc.HeadNode.WorkerHandle;
import data.Task;

public class RandomScheduler implements Scheduler {

	private final Random randomizer;

	public RandomScheduler(Random rand) {
		this.randomizer = rand;
	}

	@Override
	public Map<Task, WorkerHandle> schedule(List<Task> tasks, Set<WorkerHandle> workers) {
		Map<Task, WorkerHandle> result = new HashMap<>();

		if (!workers.isEmpty()) {
			WorkerHandle[] array = workers.toArray(new WorkerHandle[0]);
			int size = workers.size();
			for (Task t : tasks) {
				int index = randomizer.nextInt(size);
				result.put(t, array[index]);
			}
		}

		return result;
	}
}
