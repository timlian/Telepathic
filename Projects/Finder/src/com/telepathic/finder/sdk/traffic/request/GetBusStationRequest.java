package com.telepathic.finder.sdk.traffic.request;

import org.ksoap2.serialization.SoapObject;

import android.text.TextUtils;

public class GetBusStationRequest extends RPCBaseRequest {
	private static final String REQUEST_NAME = "translateToStation";
	private static final String KEY_BUS_NUMBER = "busNum";
	private static final String KEY_GPS_NUMBER = "gps";
	private static final String KEY_STATION_NAME = "stationName";
	private static final String KEY_DIRECTION = "direction";
	
	private String mLineNumber;
	
	/** Request example
	 * 
	 * translateToStation{gps=50022; } or translateToStation{busNum=102; gps=50022; }
	 */
	public GetBusStationRequest(String lineNumber, String gpsNumber) {
		super(REQUEST_NAME);
		mLineNumber = lineNumber;
		if (!TextUtils.isEmpty(lineNumber)) {
			addParameter(KEY_BUS_NUMBER, lineNumber);
		}
		addParameter(KEY_GPS_NUMBER, gpsNumber);
	}
	
	@Override
	protected void handleError(int errorCode, String errorMessage) {
		mCallback.onError(errorCode, errorMessage);
	}

	/**
	 * Response example:
	 * 
	 * {stationName=新会展中心公交站; alias=奇诺咖啡餐厅; code=200; msg=成功; } or
	 * {stationName=新会展中心公交站; alias=奇诺咖啡餐厅; direction=下行; code=200; msg=成功; }
	 */
	//{stationName=新会展中心公交站; alias=奇诺咖啡餐厅; direction=下行; code=200; msg=成功; }
	@Override
	protected void handleResponse(SoapObject newDataSet) {
		if(newDataSet.getPropertyCount() > 0) {
			StringBuilder result = new StringBuilder();
			SoapObject firstDataEntry = (SoapObject) newDataSet.getProperty(0);
			if (!TextUtils.isEmpty(mLineNumber)) {
				result.append(mLineNumber);
				result.append(",");
			}
			result.append(firstDataEntry.getPrimitivePropertyAsString(KEY_STATION_NAME));
			try {
				String direction = firstDataEntry.getPrimitivePropertyAsString(KEY_DIRECTION);
				result.append(",");
				result.append(direction);
			} catch (RuntimeException e) {
				// ignore
			}
			mCallback.onSuccess(result.toString());
		}
	}
}