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

public class QueueBytesScheduler implements Scheduler {

	@Override
	public SchedulerResponse schedule(List<Task> tasks, Collection<WorkerHandle> workers) {
		Map<Task, WorkerHandle> result = new HashMap<>();

		if (workers.isEmpty()) {
			return new SchedulerResponse(result, new ArrayList<>(tasks));
		}
		else {
			PriorityQueue<WorkerHandleWrapper> workersQueue = new PriorityQueue<>(workers.size(),
					new QueueLengthComparator());

			for (WorkerHandle worker : workers) {
				workersQueue.add(new WorkerHandleWrapper(worker));
			}

			for (Task task : tasks) {
				WorkerHandleWrapper worker = workersQueue.poll();
				result.put(task, worker.getWorker());
				workersQueue.add(worker.incrementQueueLength(task.getImageSize()));
			}
		}

		return new SchedulerResponse(result);
	}

	private class WorkerHandleWrapper {

		private final WorkerHandle worker;
		private final long bytes;

		public WorkerHandleWrapper(WorkerHandle worker) {
			this.worker = worker;
			this.bytes = worker.getJobsInProcess().size();
		}

		private WorkerHandleWrapper(WorkerHandle worker, long bytes) {
			this.worker = worker;
			this.bytes = bytes;
		}

		public WorkerHandle getWorker() {
			return worker;
		}

		public long getBytes() {
			return bytes;
		}

		public WorkerHandleWrapper incrementQueueLength(int b) {
			return new WorkerHandleWrapper(this.worker, this.bytes + b);
		}
	}

	private class QueueLengthComparator implements Comparator<WorkerHandleWrapper> {

		@Override
		public int compare(WorkerHandleWrapper worker1, WorkerHandleWrapper worker2) {
			// worker1 < worker2 => 1
			// worker1 = worker2 => 0
			// worker1 > worker2 => -1

			long size1 = worker1.getBytes();
			long size2 = worker2.getBytes();

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
