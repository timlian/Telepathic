package com.telepathic.finder.sdk.traffic.request;

import org.ksoap2.serialization.SoapObject;

public class GetBusLocationRequest extends RPCBaseRequest {
	private static final String METHOD_NAME = "getBusLocation";
	private static final String KEY_LINE_NUMBER = "lineNumber";
	private static final String KEY_GPS_NUMBER = "GPSNumber";
	private static final String KEY_LAST_STATION = "lastStation";
	private static final String KEY_DISTANCE = "distance";

	public GetBusLocationRequest(String lineNumber, String stationName, String lastStationName) {
		super(METHOD_NAME);
		addParameter(KEY_LINE_NUMBER, lineNumber);
		addParameter(KEY_GPS_NUMBER, stationName);
		addParameter(KEY_LAST_STATION, lastStationName);
	}

	@Override
	protected void handleError(int errorCode, String errorMessage) {
		mCallback.onError(errorCode, errorMessage);
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
		mCallback.onSuccess(distance);
		
	}
}