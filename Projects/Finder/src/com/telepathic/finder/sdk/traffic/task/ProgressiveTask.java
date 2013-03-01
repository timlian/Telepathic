package com.telepathic.finder.sdk.traffic.task;

import java.util.concurrent.BlockingQueue;

public abstract class ProgressiveTask<Progress> extends BaseTask<Progress> {
    private BlockingQueue<Progress> mProgresseQueue;
    private Progress mEndFlag;

    ProgressiveTask(BlockingQueue<Progress> progresseQueue) {
        super("ProgressiveTask");
        mProgresseQueue = progresseQueue;
    }

    public void setProgress(Progress progress) {
        mProgresseQueue.add(progress);
    }

    public void setTaskEndFlag(Progress endFlag) {
        mEndFlag = endFlag;
    }

    @Override
    public synchronized void setTaskResult(TaskResult<Progress> result) {
        if (mEndFlag != null) {
            mProgresseQueue.add(mEndFlag);
        }
        super.setTaskResult(result);
    }

}
