package com.telepathic.finder.sdk.traffic.task;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

import org.ksoap2.serialization.SoapObject;

public class GetBusLocationTask extends ProgressiveTask<Integer> {
	private static final String TAG = GetBusLocationTask.class.getSimpleName();
	private final String mLineNumber;
	private final ArrayList<String> mRoute;
	private final String mLastStation;
	private int mErrorCode;
	private String mErrorMessage;
	private int mStationIndex;

	public GetBusLocationTask(String lineNumber, ArrayList<String> route, BlockingQueue<Integer> queue) {
		super(queue);
		mLineNumber = lineNumber;
		mRoute = route;
		mStationIndex = route.size() - 1;
		mLastStation = route.get(route.size() - 1);
	}

	@Override
	protected void doWork() {
		while (isDone()) {
			NetworkManager.execute(new GetBusLocationRequest());
		}
	}

	public String getStationName() {
		String result = null;
		if (mStationIndex >= 0 && mStationIndex < mRoute.size()) {
			result = mRoute.get(mStationIndex);
		}
		return result;
	}

	private void updateStationIndex(int distance) {
		if (distance > 0) {
			mStationIndex -= distance;
		} else {
			mStationIndex--;
		}
	}

	private class GetBusLocationRequest extends RPCBaseRequest {
		private static final String METHOD_NAME = "getBusLocation";
		private static final String KEY_LINE_NUMBER = "lineNumber";
		private static final String KEY_GPS_NUMBER = "GPSNumber";
		private static final String KEY_LAST_STATION = "lastStation";
		private static final String KEY_DISTANCE = "distance";

		GetBusLocationRequest() {
			super(METHOD_NAME);
			addParameter(KEY_LINE_NUMBER, mLineNumber);
			addParameter(KEY_GPS_NUMBER, getStationName());
			addParameter(KEY_LAST_STATION, mLastStation);
		}

		@Override
		protected void handleError(int errorCode, String errorMessage) {
			mErrorCode = errorCode;
			mErrorMessage = errorMessage;
		}

		/*
		 * Location response data example:
		 * 
		 * {lineNumber=102; distance=1; code=200; msg=�ɹ�; }
		 */
		@Override
		protected void handleResponse(SoapObject newDataSet) {
			final SoapObject firstDataEntry = (SoapObject) newDataSet.getProperty(0);
			final String lineNumber = firstDataEntry.getPrimitivePropertyAsString(KEY_LINE_NUMBER);
			final int distance = Integer.parseInt(firstDataEntry.getPrimitivePropertyAsString(KEY_DISTANCE));
			updateStationIndex(distance);
			if (distance >= 0) {
				TaskResult<Integer> taskResult = new TaskResult<Integer>();
				taskResult.setResult(mStationIndex);
				if (mStationIndex >= 0) {
					setProgress(mStationIndex);
				} else {
					setTaskDone();
				}
			}
		}
	}

}
