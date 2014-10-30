package scheduler;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import tud.cc.HeadNode.WorkerHandle;
import data.Task;

/**
 * This implementation of {@code Scheduler} assigns each task to a randomly chosen worker.
 * @author Richard
 */
public class RandomScheduler implements Scheduler {

	private final Random randomizer;

	public RandomScheduler(Random rand) {
		this.randomizer = rand;
	}

	@Override
	public Map<Task, WorkerHandle> schedule(List<Task> tasks, Collection<WorkerHandle> workers) throws SchedulerException {
		Map<Task, WorkerHandle> result = new HashMap<>();

		if (workers.isEmpty()) {
			throw new SchedulerException("The set of WorkerHandles was empty");
		}
		else {
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
