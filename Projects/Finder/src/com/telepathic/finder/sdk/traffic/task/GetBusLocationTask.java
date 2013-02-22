package com.telepathic.finder.sdk.traffic.task;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

import com.telepathic.finder.sdk.traffic.request.GetBusLocationRequest;
import com.telepathic.finder.sdk.traffic.request.RequestCallback;
import com.telepathic.finder.sdk.traffic.request.RequestExecutor;
import com.telepathic.finder.util.Utils;

public class GetBusLocationTask extends ProgressiveTask<Integer> {
	private static final String TAG = GetBusLocationTask.class.getSimpleName();
	private final String mLineNumber;
	private final ArrayList<String> mRoute;
	
	private final String mLastStation;
	
	private int mLocation;

	public GetBusLocationTask(String lineNumber, ArrayList<String> route, BlockingQueue<Integer> queue) {
		super(queue);
		mLineNumber = lineNumber;
		mRoute = route;
		mLocation = route.size() - 1;
		mLastStation = route.get(route.size() - 1);
	}

	@Override
	protected void doWork() {
		while (mLocation >= 0) {
			GetBusLocationRequest request = new GetBusLocationRequest(mLineNumber, getStationName(), mLastStation);
			RequestExecutor.execute(request, new RequestCallback() {
				@Override
				public void onSuccess(Object result) {
					Integer distance = (Integer) result;
					if (distance >= 0) {
						mLocation -= distance;
						if (mLocation >= 0) {
							setProgress(mLocation);
							Utils.debug(TAG, "location: " + mLocation);
						} else {
							Utils.debug(TAG, "end: " + mLocation);
							setTaskResult(null);
						}
					} else {
						mLocation -= 1;
						if (mLocation < 0) {
							Utils.debug(TAG, "end: " + mLocation);
							setTaskResult(null);
						}
					}
				}
				
				@Override
				public void onError(int errorCode, String errorMessage) {
					TaskResult<Integer> taskResult = new TaskResult<Integer>();
					taskResult.setResult(-1);
					taskResult.setErrorCode(errorCode);
					taskResult.setErrorMessage(errorMessage);
					setTaskResult(taskResult);
				}
			});
		}
	}

	public void setLastLocation(Integer lastLocation) {
		setTaskEndFlag(lastLocation);
	}
	
	public String getStationName() {
		String result = null;
		if (mLocation >= 0 && mLocation < mRoute.size()) {
			result = mRoute.get(mLocation);
		}
		return result;
	}

}
