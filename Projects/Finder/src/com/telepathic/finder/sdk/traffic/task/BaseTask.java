package com.telepathic.finder.sdk.traffic.task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class BaseTask<Result> {
    private boolean mIsDone = false;
    private Future<?> mTaskHandle;
    private ExecutorService mExecutorService;
    private TaskResult<Result> mTaskResult;
    
    public BaseTask() {
    	mExecutorService = Executors.newSingleThreadExecutor();
    }
    
    public synchronized void startTask() {
    	if (mIsDone == false) {
    		mTaskHandle = mExecutorService.submit(new Runnable() {
				@Override
				public void run() {
					doWork();
				}
			});
    	}
    }
    
    protected abstract void doWork();
    
    public synchronized void waitTaskDone() throws InterruptedException {
    	while(mIsDone == false) {
    		wait();
    	}
    }
    
    public synchronized boolean isDone() {
    	return mIsDone;
    }
    
    public synchronized void setTaskResult(TaskResult<Result> taskResult) {
    	mTaskResult = taskResult;
    	setTaskDone();
    }
    
    public synchronized void setTaskDone() {
    	mIsDone = true;
    	notifyAll();
    }
    
    public synchronized TaskResult<Result> getTaskResult() {
    	return mTaskResult;
    }
    
    public synchronized void cancelTask() {
    	if (mTaskHandle != null && !mTaskHandle.isDone()) {
    		mTaskHandle.cancel(true);
    	}
    }

}
