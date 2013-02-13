package com.telepathic.finder.sdk.traffic.task;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

import org.ksoap2.serialization.SoapObject;

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
			NetworkManager.execute(new GetBusLocationRequest());
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
			TaskResult<Integer> taskResult = new TaskResult<Integer>();
			taskResult.setResult(-1);
			taskResult.setErrorCode(errorCode);
			taskResult.setErrorMessage(errorMessage);
			setTaskResult(taskResult);
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
	}

}
