package com.telepathic.finder.sdk.traffic.task;

import java.util.concurrent.Callable;

import org.ksoap2.serialization.SoapObject;

import android.text.TextUtils;

public class TranslateToStationTask implements Callable<String> {
	private final String mLineNumber;
	private final String mGpsNumber;
	private final StringBuilder mResult;
	
	public TranslateToStationTask(String lineNumber, String gpsNumber) {
		mLineNumber = lineNumber;
		mGpsNumber = gpsNumber;
		mResult = new StringBuilder();
	}
	
	@Override
	public String call() throws Exception {
		NetworkManager.execute(new GetBusStationRequest());
		return mResult.toString();
	}
	
	private class GetBusStationRequest extends RPCBaseRequest {
		private static final String REQUEST_NAME = "translateToStation";
		private static final String KEY_BUS_NUMBER = "busNum";
		private static final String KEY_GPS_NUMBER = "gps";
		private static final String KEY_STATION_NAME = "stationName";
		private static final String KEY_DIRECTION = "direction";
		
		/** Request example
		 * 
		 * translateToStation{gps=50022; } or translateToStation{busNum=102; gps=50022; }
		 */
		GetBusStationRequest() {
			super(REQUEST_NAME);
			if (!TextUtils.isEmpty(mLineNumber)) {
				addParameter(KEY_BUS_NUMBER, mLineNumber);
			}
			addParameter(KEY_GPS_NUMBER, mGpsNumber);
		}
		
		@Override
		protected void handleError(int errorCode, String errorMessage) {
			// TODO Auto-generated method stub
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
				SoapObject firstDataEntry = (SoapObject) newDataSet.getProperty(0);
				if (!TextUtils.isEmpty(mLineNumber)) {
					mResult.append(mLineNumber);
					mResult.append(",");
				}
				mResult.append(firstDataEntry.getPrimitivePropertyAsString(KEY_STATION_NAME));
				try {
					String direction = firstDataEntry.getPrimitivePropertyAsString(KEY_DIRECTION);
					mResult.append(",");
					mResult.append(direction);
				} catch (RuntimeException e) {
					// ignore
				}
			}
		}
	}
	
}
