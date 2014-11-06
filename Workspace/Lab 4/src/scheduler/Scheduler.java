package scheduler;

import java.util.Collection;
import java.util.List;

import tud.cc.WorkerHandle;
import data.Task;

/**
 * Implementations of this interface assign {@code Task} objects to instances of
 * {@code WorkerHandle}. If there are no workers in the set, an exception is thrown.
 * 
 * @author Richard van Heest
 */
public interface Scheduler {

	SchedulerResponse schedule(List<Task> tasks, Collection<WorkerHandle> workers);
}
