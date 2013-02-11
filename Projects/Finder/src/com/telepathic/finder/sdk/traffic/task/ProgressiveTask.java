package com.telepathic.finder.sdk.traffic.task;

import java.util.concurrent.BlockingQueue;

public abstract class ProgressiveTask<Progress> extends BaseTask<Progress> {
	private BlockingQueue<Progress> mProgresseQueue;
	
	ProgressiveTask(BlockingQueue<Progress> progresseQueue) {
		mProgresseQueue = progresseQueue;
	}
	
	
	public void setProgress(Progress progress) {
		mProgresseQueue.add(progress);
	}

}
