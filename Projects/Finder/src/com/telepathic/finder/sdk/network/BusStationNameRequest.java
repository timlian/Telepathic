package com.telepathic.finder.sdk.network;

import org.ksoap2.serialization.SoapObject;

import android.util.Log;

public class BusStationNameRequest extends RPCRequest {
	private static final String REQUEST_NAME = "translateToStation";
	private static final String RESPONSE_NAME = REQUEST_NAME + "Result";
	
	private static final String KEY_BUS_NUMBER = "busNum";
	private static final String KEY_GPS_NUMBER = "gps";
	
	public BusStationNameRequest(String busNumber, String gpsNumber) {
		super(REQUEST_NAME);
		addParameter(KEY_BUS_NUMBER, busNumber);
		addParameter(KEY_GPS_NUMBER, gpsNumber);
	}
	
	@Override
	protected String getResponseName() {
		return RESPONSE_NAME;
	}

	@Override
	protected void handleError(String errorMessage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void handleResponse(SoapObject newDataSet) {
		Log.d("BusStationNameRequest", newDataSet.toString());
		
	}

}
