package scheduler;

import head.WorkerHandle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import data.Task;

/**
 * This implementation of {@code Scheduler} assigns each task to a randomly chosen worker.
 * 
 * @author Richard
 */
public class RandomScheduler implements Scheduler {

	private final Random randomizer;

	public RandomScheduler(Random rand) {
		this.randomizer = rand;
	}

	@Override
	public SchedulerResponse schedule(List<Task> tasks, Collection<WorkerHandle> workers) {
		Map<Task, WorkerHandle> result = new HashMap<>();

		if (workers.isEmpty()) {
			return new SchedulerResponse(result, new ArrayList<>(tasks));
		}
		else {
			WorkerHandle[] array = workers.toArray(new WorkerHandle[0]);
			int size = workers.size();
			for (Task t : tasks) {
				int index = randomizer.nextInt(size);
				result.put(t, array[index]);
			}
		}

		return new SchedulerResponse(result);
	}
}
