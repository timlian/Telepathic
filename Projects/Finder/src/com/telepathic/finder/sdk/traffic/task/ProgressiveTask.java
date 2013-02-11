package com.telepathic.finder.sdk.traffic.task;

import java.util.concurrent.BlockingQueue;

public abstract class ProgressiveTask<Progress> extends BaseTask<Progress> {
	private BlockingQueue<TaskResult<Progress>> mProgresseQueue;
	
	ProgressiveTask(BlockingQueue<TaskResult<Progress>> progresseQueue) {
		mProgresseQueue = progresseQueue;
	}
	
	
	public void setProgress(TaskResult<Progress> progress) {
		mProgresseQueue.add(progress);
	}

}
