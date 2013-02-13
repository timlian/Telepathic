package com.telepathic.finder.sdk.traffic.task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.telepathic.finder.util.Utils;

public abstract class BaseTask<Result> {
	private static final String TAG = "BaseTask";
	private String mTaskName;
    private boolean mIsDone = false;
    private Future<?> mTaskHandle;
    private ExecutorService mExecutorService;
    private TaskResult<Result> mTaskResult;
    
    public BaseTask(String taskName) {
    	mTaskName = taskName;
    	mExecutorService = Executors.newSingleThreadExecutor();
    }
    
	public synchronized void startTask() {
		if (mIsDone == false) {
			mTaskHandle = mExecutorService.submit(new Runnable() {
				@Override
				public void run() {
					try {
						doWork();
					} catch (Exception e) {
						Utils.debug(TAG, mTaskName + " catch exception: " + e.getMessage());
					} finally {
						notifyTaskDone();
					}
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
    
    public synchronized void notifyTaskDone() {
    	mIsDone = true;
    	notifyAll();
    }
    
    public synchronized boolean isDone() {
    	return mIsDone;
    }
    
    public synchronized void setTaskResult(TaskResult<Result> taskResult) {
    	mTaskResult = taskResult;
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
