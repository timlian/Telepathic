package com.telepathic.finder.sdk.traffic.task;

import java.util.List;

import com.telepathic.finder.sdk.traffic.request.GetBusStationLinesRequest;
import com.telepathic.finder.sdk.traffic.request.RequestCallback;
import com.telepathic.finder.sdk.traffic.request.RequestExecutor;

public class GetBusStationLinesTask extends BaseTask<List<String>> {
	private final String mStationName;
    private final String mGpsNumber;
    
    public GetBusStationLinesTask(String stationName, String gpsNumber) {
    	super("GetBusStationLinesTask");
    	mStationName = stationName;
        mGpsNumber = gpsNumber;
    }

    @Override
    protected void doWork() {
    	GetBusStationLinesRequest request = new GetBusStationLinesRequest(mStationName, mGpsNumber);
		final TaskResult<List<String>> taskResult = new TaskResult<List<String>>();
		RequestExecutor.execute(request, new RequestCallback() {
			@Override
			public void onSuccess(Object result) {
				taskResult.setResult((List<String>)result);
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
