package scheduler;

import head.WorkerHandle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import data.Task;

public class BlockingQueueBytesScheduler implements Scheduler {

	@Override
	public SchedulerResponse schedule(List<Task> tasks, Collection<WorkerHandle> workers) {
		Map<Task, WorkerHandle> result = new HashMap<>();
		List<Task> reject = new ArrayList<>();

		if (workers.isEmpty()) {
			return new SchedulerResponse(result, new ArrayList<>(tasks));
		}
		else if (tasks.isEmpty()) {
			return new SchedulerResponse(result);
		}
		else {
			int size = workers.size();
			
			PriorityQueue<WorkerHandleWrapper> workersQueue = new PriorityQueue<>(size,
					new QueueLengthComparator());

			for (WorkerHandle worker : workers) {
				workersQueue.add(new WorkerHandleWrapper(worker));
			}
			
			boolean bestFull = false;

			for (Task task : tasks) {
				if (!bestFull) {
    				WorkerHandleWrapper worker = workersQueue.poll();
    				if (worker.px <= 630 * 10_000) {
    					result.put(task, worker.getWorker());
        				workersQueue.add(worker.incrementQueueLength(task.getPixelCount()));
    				}
    				else {
    					bestFull = true;
    					reject.add(task);
    					workersQueue.add(worker);
    				}
    				result.put(task, worker.getWorker());
    				workersQueue.add(worker.incrementQueueLength(task.getImageSize()));
				}
				else {
					reject.add(task);
				}
			}
			
			return new SchedulerResponse(result, reject);
		}
	}

	private class WorkerHandleWrapper {

		private final WorkerHandle worker;
		private final long px;

		public WorkerHandleWrapper(WorkerHandle worker) {
			this.worker = worker;
			this.px = worker.getPixelsInProcess();
		}

		private WorkerHandleWrapper(WorkerHandle worker, long px) {
			this.worker = worker;
			this.px = px;
		}

		public WorkerHandle getWorker() {
			return worker;
		}

		public long getPixelCount() {
			return px;
		}

		public WorkerHandleWrapper incrementQueueLength(long pxs) {
			return new WorkerHandleWrapper(this.worker, this.px + pxs);
		}
	}

	private class QueueLengthComparator implements Comparator<WorkerHandleWrapper> {

		@Override
		public int compare(WorkerHandleWrapper worker1, WorkerHandleWrapper worker2) {
			// worker1 < worker2 => 1
			// worker1 = worker2 => 0
			// worker1 > worker2 => -1

			long size1 = worker1.getPixelCount();
			long size2 = worker2.getPixelCount();

			if (size1 < size2) {
				return 1;
			}
			else if (size1 == size2) {
				return 0;
			}
			else {
				return -1;
			}
		}
	}
}
