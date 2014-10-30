package scheduler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import tud.cc.HeadNode.WorkerHandle;
import data.Task;

/**
 * This implementation of {@code Scheduler} assigns each task to a different {@code WorkerHandle} by
 * iterating over {@code workers}. In the case there are more tasks than workers, the iteration of
 * {@code workers} starts all over again.
 * 
 * @author Richard van Heest
 */
public class RoundRobinScheduler implements Scheduler {

	@Override
	public Map<Task, WorkerHandle> schedule(List<Task> tasks, Collection<WorkerHandle> workers)
			throws SchedulerException {
		Map<Task, WorkerHandle> result = new HashMap<>();

		if (workers.isEmpty()) {
			throw new SchedulerException("The set of WorkerHandles was empty");
		}
		else {
			Iterator<WorkerHandle> iterator = workers.iterator();
			for (Task t : tasks) {
				if (iterator.hasNext()) {
					result.put(t, iterator.next());
				}
				else {
					// start iterating the workers again
					iterator = workers.iterator();
					result.put(t, iterator.next());
				}
			}
		}

		return result;
	}
}
