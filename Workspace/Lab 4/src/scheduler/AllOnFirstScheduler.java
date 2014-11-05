package scheduler;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import tud.cc.WorkerHandle;
import data.Task;

/**
 * This implementation of {@code Scheduler} assigns all tasks to the first {@code WorkerHandle} in
 * {@code workers}.
 * 
 * @author Richard van Heest
 */
public class AllOnFirstScheduler implements Scheduler {

	@Override
	public Map<Task, WorkerHandle> schedule(List<Task> tasks, Collection<WorkerHandle> workers)
			throws SchedulerException {
		Map<Task, WorkerHandle> result = new HashMap<>();

		Iterator<WorkerHandle> iterator = workers.iterator();
		if (iterator.hasNext()) {
			WorkerHandle worker = iterator.next();
			for (Task t : tasks) {
				result.put(t, worker);
			}
		}
		else {
			assert workers.isEmpty() : "The workers set should be empty, but contained: "
					+ workers.toString();
			throw new SchedulerException("The set of WorkerHandles was empty");
		}

		return result;
	}
}
