package com.telepathic.finder.sdk.traffic.task;


import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusLineDirection;
import com.telepathic.finder.sdk.traffic.request.GetBusLineDirectionRequest;
import com.telepathic.finder.sdk.traffic.request.RequestCallback;
import com.telepathic.finder.sdk.traffic.request.RequestExecutor;

public class GetBusLineDirectionTask extends BaseTask<KXBusLineDirection> {
	private String mLineNumber;
	private String mGpsNumber;
	
	public GetBusLineDirectionTask(String lineNumber, String gpsNumber) {
		super("GetLineDirectionTask");
		mLineNumber = lineNumber;
		mGpsNumber = gpsNumber;
	}
	
	@Override
	protected void doWork() {
		GetBusLineDirectionRequest request = new GetBusLineDirectionRequest(mLineNumber, mGpsNumber);
		RequestExecutor.execute(request, new RequestCallback() {
			final TaskResult<KXBusLineDirection> taskResult = new TaskResult<KXBusLineDirection>();
			@Override
			public void onSuccess(Object result) {
				taskResult.setResult((KXBusLineDirection)result);
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
