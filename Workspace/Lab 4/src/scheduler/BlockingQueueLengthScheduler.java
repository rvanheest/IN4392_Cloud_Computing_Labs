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

public class BlockingQueueLengthScheduler implements Scheduler {

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
    				if (worker.queueLength <= 2 * worker.getWorker().handshake.cores) {
    					result.put(task, worker.getWorker());
        				workersQueue.add(worker.incrementQueueLength());
    				}
    				else {
    					bestFull = true;
    					reject.add(task);
    					workersQueue.add(worker);
    				}
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
		private final int queueLength;

		public WorkerHandleWrapper(WorkerHandle worker) {
			this.worker = worker;
			this.queueLength = worker.getJobsInProcess().size();
		}

		private WorkerHandleWrapper(WorkerHandle worker, int queueLength) {
			this.worker = worker;
			this.queueLength = queueLength;
		}

		public WorkerHandle getWorker() {
			return worker;
		}

		public int getQueueLength() {
			return queueLength;
		}

		public WorkerHandleWrapper incrementQueueLength() {
			return new WorkerHandleWrapper(this.worker, this.queueLength + 1);
		}
	}

	private class QueueLengthComparator implements Comparator<WorkerHandleWrapper> {

		@Override
		public int compare(WorkerHandleWrapper worker1, WorkerHandleWrapper worker2) {
			// worker1 < worker2 => 1
			// worker1 = worker2 => 0
			// worker1 > worker2 => -1

			int size1 = worker1.getQueueLength();
			int size2 = worker2.getQueueLength();
			
			int threshold1 = worker1.getWorker().handshake.cores;
			int threshold2 = worker2.getWorker().handshake.cores;

			if (size1 == 0) {
				if (size2 == 0) {
					return 0;
				}
				else if (size2 < threshold2) {
					return 1;
				}
				else {
					// size2 > threshold2
					return -1;
				}
			}
			else if (size1 < threshold1) {
				if (size2 == 0) {
					return -1;
				}
				else if (size2 < threshold2) {
					return size1 > size2 ? 1 : size1 == size2 ? 0 : -1;
				}
				else {
					// size2 > threshold2
					return -1;
				}
			}
			else {
				// size1 > threshold1 
				if (size2 == 0) {
					return 1;
				}
				else if (size2 < threshold2) {
					return 1;
				}
				else {
					// size2 > threshold2
					return size1 < size2 ? 1 : size1 == size2 ? 0 : -1;
				}
			}
		}
	}
}
