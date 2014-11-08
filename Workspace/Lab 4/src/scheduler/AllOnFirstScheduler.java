package scheduler;

import head.WorkerHandle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import data.Task;

/**
 * This implementation of {@code Scheduler} assigns all tasks to the first {@code WorkerHandle} in
 * {@code workers}.
 * 
 * @author Richard van Heest
 */
public class AllOnFirstScheduler implements Scheduler {

	@Override
	public SchedulerResponse schedule(List<Task> tasks, Collection<WorkerHandle> workers) {
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
			return new SchedulerResponse(result, new ArrayList<>(tasks));
		}

		return new SchedulerResponse(result);
	}
}
