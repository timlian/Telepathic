package com.telepathic.finder.sdk.traffic.task;

import java.util.concurrent.Callable;

import org.ksoap2.serialization.SoapObject;


public class GetBusStationLinesTask  implements Callable<String[]> {
	private final String mStationName;
    private final String mGpsNumber;
    private String[] mResult;
    
    public GetBusStationLinesTask(String stationName, String gpsNumber) {
    	mStationName = stationName;
        mGpsNumber = gpsNumber;
    }


	@Override
	public String[] call() throws Exception {
		NetworkManager.execute(new GetBusStationLinesRequest());
		return mResult;
	}
	
	private class GetBusStationLinesRequest extends RPCBaseRequest {
		private static final String METHOD_NAME = "getBusStationLines";
	    // parameter keys
	    private static final String KEY_BUS_STATION = "busStation";
	    private static final String KEY_PAGE_INDEX = "pageIndex";
	    private static final String KEY_PAGE_SIZE = "pageSize";
	    //response keys
	    private static final String KEY_STATION_GPS = "stationGPS";
	    private static final String KEY_LINE_NAME = "lineName";
	    
	    GetBusStationLinesRequest() {
	    	 super(METHOD_NAME);
	         addParameter(KEY_BUS_STATION, mStationName);
	         addParameter(KEY_PAGE_INDEX, "1");
	         addParameter(KEY_PAGE_SIZE, "30");
	    }
	    
	    @Override
	    void handleError(String errorMessage) {

	    }

	    /*
	     * Table1=anyType{station=新会展中心公交站; stationAlias=anyType{}; stationGPS=50022; lineName=298,115,84,118,102; totalNum=2; code=200; msg=成功; };
	     * Table1=anyType{station=新会展中心公交站; stationAlias=anyType{}; stationGPS=50023; lineName=84,102,115,298,118; };
	     */
	    @Override
		void handleResponse(SoapObject newDataSet) {
			SoapObject dataEntry = null;
			final int count = newDataSet.getPropertyCount();
			for (int idx = 0; idx < count; idx++) {
				dataEntry = (SoapObject) newDataSet.getProperty(idx);
				String gpsNumber = dataEntry.getPrimitivePropertyAsString(KEY_STATION_GPS);
				if (gpsNumber.equals(mGpsNumber)) {
					mResult = dataEntry.getPrimitivePropertyAsString(KEY_LINE_NAME).split(",");
				}
			}
		}
	}
	
}
