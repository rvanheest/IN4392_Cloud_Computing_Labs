package scheduler;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tud.cc.HeadNode.WorkerHandle;
import data.Task;

/**
 * This implementation of {@code Scheduler} assigns all tasks to the first {@code WorkerHandle}
 * in {@code workers}. If {@code workers} is empty, the scheduler returns an empty
 * {@code Map<Task, WorkerHandle>}.
 * @author Richard van Heest
 */
public class AllOnFirstScheduler implements Scheduler {

	@Override
	public Map<Task, WorkerHandle> schedule(List<Task> tasks, Set<WorkerHandle> workers) {
		Map<Task, WorkerHandle> result = new HashMap<>();
		
		Iterator<WorkerHandle> iterator = workers.iterator();
		if (iterator.hasNext()) {
			WorkerHandle worker = iterator.next();
			for (Task t : tasks) {
				result.put(t, worker);
			}
		}
		
		return result;
	}
}
