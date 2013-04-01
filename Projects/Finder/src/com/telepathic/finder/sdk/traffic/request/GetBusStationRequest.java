package com.telepathic.finder.sdk.traffic.request;

import org.ksoap2.serialization.SoapObject;

import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusStation;

public class GetBusStationRequest extends RPCBaseRequest {
    private static final String REQUEST_NAME = "translateToStation";
    private static final String KEY_GPS_NUMBER = "gps";
    private static final String KEY_STATION_NAME = "stationName";

    private String mGpsNumber;

    /** Request example
     *
     * translateToStation{gps=50022; } or translateToStation{busNum=102; gps=50022; }
     */
    public GetBusStationRequest(String gpsNumber) {
        super(REQUEST_NAME);
        mGpsNumber = gpsNumber;
        addParameter(KEY_GPS_NUMBER, gpsNumber);
    }

    @Override
    protected void handleError(int errorCode, String errorMessage) {
    	if (mCallback != null) {
    		mCallback.onError(errorCode, errorMessage);
    	}
    }

    /**
     * Response example:
     *
     * {stationName=新会展中心公交站; alias=奇诺咖啡餐厅}
     */
    @Override
    protected void handleResponse(SoapObject newDataSet) {
    	KXBusStation station = new KXBusStation();
    	station.setGpsNumber(mGpsNumber);
        if(newDataSet.getPropertyCount() > 0) {
            SoapObject firstDataEntry = (SoapObject) newDataSet.getProperty(0);
            String name = firstDataEntry.getPrimitivePropertyAsString(KEY_STATION_NAME);
            station.setName(name);
        }
        if (mCallback != null) {
        	mCallback.onSuccess(station);
        }
    }
}
