package com.telepathic.finder.sdk.traffic.request;

import java.util.Arrays;

import org.ksoap2.serialization.SoapObject;

public class GetBusStationLinesRequest extends RPCBaseRequest {
	private static final String METHOD_NAME = "getBusStationLines";
    // parameter keys
    private static final String KEY_BUS_STATION = "busStation";
    private static final String KEY_PAGE_INDEX = "pageIndex";
    private static final String KEY_PAGE_SIZE = "pageSize";
    //response keys
    private static final String KEY_STATION_GPS = "stationGPS";
    private static final String KEY_LINE_NAME = "lineName";
    
    private String mGpsNumber;
    
    public GetBusStationLinesRequest(String statinName, String gpsNumber) {
    	 super(METHOD_NAME);
         addParameter(KEY_BUS_STATION, statinName);
         addParameter(KEY_PAGE_INDEX, "1");
         addParameter(KEY_PAGE_SIZE, "30");
         mGpsNumber = gpsNumber;
    }
    
    @Override
    void handleError(int errorCode, String errorMessage) {
    	mCallback.onError(errorCode, errorMessage);
    }

    /*
     * Table1=anyType{station=新会展中心公交站; stationAlias=anyType{}; stationGPS=50022; lineName=298,115,84,118,102; totalNum=2; code=200; msg=成功; };
     * Table1=anyType{station=新会展中心公交站; stationAlias=anyType{}; stationGPS=50023; lineName=84,102,115,298,118; };
     */
    @Override
	void handleResponse(SoapObject newDataSet) {
		SoapObject dataEntry = null;
		final int count = newDataSet.getPropertyCount();
		String lineNames = null;
		for (int idx = 0; idx < count; idx++) {
			dataEntry = (SoapObject) newDataSet.getProperty(idx);
			String gpsNumber = dataEntry.getPrimitivePropertyAsString(KEY_STATION_GPS);
			if (gpsNumber.equals(mGpsNumber)) {
				lineNames = dataEntry.getPrimitivePropertyAsString(KEY_LINE_NAME);
			}
		}
		if (lineNames != null && !lineNames.equals("")) {
			mCallback.onSuccess(Arrays.asList(lineNames.split(",")));
		}
	}
}