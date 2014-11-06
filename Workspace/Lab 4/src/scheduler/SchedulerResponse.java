package scheduler;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import tud.cc.WorkerHandle;
import data.Task;

public class SchedulerResponse {

	private final Map<Task, WorkerHandle> accept;
	private final List<Task> reject;

	public SchedulerResponse(Map<Task, WorkerHandle> accept) {
		this.accept = accept;
		this.reject = Collections.emptyList();
	}

	public SchedulerResponse(Map<Task, WorkerHandle> accept, List<Task> reject) {
		this.accept = accept;
		this.reject = reject;
	}

	public Map<Task, WorkerHandle> getAccept() {
		return accept;
	}

	public List<Task> getReject() {
		return reject;
	}
}
