package com.telepathic.finder.sdk.traffic.task;

import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusLine;
import com.telepathic.finder.sdk.traffic.request.GetBusLineRequest;
import com.telepathic.finder.sdk.traffic.request.RequestCallback;
import com.telepathic.finder.sdk.traffic.request.RequestExecutor;

public class GetBusLineTask extends BaseTask<KXBusLine> {
	private static final String TAG = GetBusLineTask.class.getSimpleName();
	
    private String mLineNumber;
    
    public GetBusLineTask(String lineNumber) {
    	super("GetBusLineTask");
        mLineNumber = lineNumber;
    }
    
    @Override
    protected void doWork() {
    	GetBusLineRequest request = new GetBusLineRequest(mLineNumber);
    	final TaskResult<KXBusLine> taskResult = new TaskResult<KXBusLine>();
    	RequestExecutor.execute(request, new RequestCallback() {
			@Override
			public void onSuccess(Object result) {
				taskResult.setResult((KXBusLine)result);
				setTaskResult(taskResult);
			}
			
			@Override
			public void onError(int errorCode, String errorMessage) {
				taskResult.setErrorCode(errorCode);
				taskResult.setErrorMessage(errorMessage);
				setTaskResult(taskResult);
			}
		});
    	
    }
    
}
