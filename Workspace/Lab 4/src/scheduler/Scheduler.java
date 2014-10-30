package scheduler;

import java.util.List;
import java.util.Map;
import java.util.Set;

import tud.cc.HeadNode.WorkerHandle;
import data.Task;

/**
 * Implementations of this interface assign {@code Task} objects to instances of
 * {@code WorkerHandle}. If there are no workers in the set, an exception is thrown.
 * @author Richard van Heest
 */
public interface Scheduler {

	Map<Task, WorkerHandle> schedule(List<Task> tasks, Set<WorkerHandle> workers) throws SchedulerException;
}
