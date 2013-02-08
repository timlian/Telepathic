package com.telepathic.finder.sdk.traffic.task;

import java.util.concurrent.Callable;

import org.ksoap2.serialization.SoapObject;

import android.text.TextUtils;

import com.telepathic.finder.sdk.traffic.entity.BusStation;
import com.telepathic.finder.sdk.traffic.store.BusLineStation;

public class GetBusStationRequest extends RPCBaseRequest implements Callable<BusStation> {
	private static final String REQUEST_NAME = "translateToStation";
	
	private static final String KEY_BUS_NUMBER = "busNum";
	private static final String KEY_GPS_NUMBER = "gps";
	private static final String KEY_STATION_NAME = "stationName";
	private static final String KEY_DIRECTION = "direction";
	
	private final String mLineNumber;
	private final String mGpsNumber;
	private BusStation mStation;
	
	public GetBusStationRequest(String lineNumber, String gpsNumber) {
		super(REQUEST_NAME);
		mStation = new BusStation();
		mLineNumber = lineNumber;
		mGpsNumber = gpsNumber;
		if (!TextUtils.isEmpty(mLineNumber)) {
			addParameter(KEY_BUS_NUMBER, mLineNumber);
		}
		//addParameter(KEY_GPS_NUMBER, mGpsNumber);
	}
	
	@Override
	protected void handleError(String errorMessage) {
		// TODO Auto-generated method stub
	}

	//{stationName=新会展中心公交站; alias=奇诺咖啡餐厅; direction=下行; code=200; msg=成功; }
	@Override
	protected void handleResponse(SoapObject newDataSet) {
		SoapObject firstDataEntry = (SoapObject) newDataSet.getProperty(0);
		String stationName = firstDataEntry.getPrimitivePropertyAsString(KEY_STATION_NAME);
		mStation.setName(stationName);
		String direction = null;
		if (!TextUtils.isEmpty(mLineNumber)) {
			direction = firstDataEntry.getPrimitivePropertyAsString(KEY_DIRECTION);
		}
		if (direction != null) {
			BusLineStation station = new BusLineStation(stationName, mGpsNumber, mLineNumber, direction);
			//mTrafficeMonitor.
		} else {
			//BusStation station = new BusStation(stationName, mGpsNumber);
		}
	}

	@Override
	public BusStation call() throws Exception {
		NetworkManager.execute(this);
		return mStation;
	}
	
}
