package scheduler;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import tud.cc.WorkerHandle;
import data.Task;

public class QueueLengthScheduler implements Scheduler {

	private final int threshold;

	public QueueLengthScheduler(int threshold) {
		this.threshold = threshold;
	}

	@Override
	public Map<Task, WorkerHandle> schedule(List<Task> tasks, Collection<WorkerHandle> workers)
			throws SchedulerException {
		Map<Task, WorkerHandle> result = new HashMap<>();

		if (workers.isEmpty()) {
			throw new SchedulerException("The set of WorkerHandles was empty");
		}
		else {
			Comparator<WorkerHandleWrapper> comparator = new QueueLengthComparator();
			PriorityQueue<WorkerHandleWrapper> workersQueue = new PriorityQueue<>(workers.size(),
					comparator);

			for (WorkerHandle worker : workers) {
				workersQueue.add(new WorkerHandleWrapper(worker));
			}

			for (Task task : tasks) {
				WorkerHandleWrapper worker = workersQueue.poll();
				result.put(task, worker.getWorker());
				workersQueue.add(worker.incrementQueueLength());
			}
		}

		return result;
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

			if (size1 == 0) {
				if (size2 == 0) {
					return 0;
				}
				else if (size2 < threshold) {
					return 1;
				}
				else {
					// size2 > threshold
					return -1;
				}
			}
			else if (size1 < threshold) {
				if (size2 == 0) {
					return -1;
				}
				else if (size2 < threshold) {
					return size1 > size2 ? 1 : size1 == size2 ? 0 : -1;
				}
				else {
					// size2 > threshold
					return -1;
				}
			}
			else {
				// size1 > threshold 
				if (size2 == 0) {
					return 1;
				}
				else if (size2 < threshold) {
					return 1;
				}
				else {
					// size2 > threshold
					return size1 < size2 ? 1 : size1 == size2 ? 0 : -1;
				}
			}
		}
	}
}