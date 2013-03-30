package com.telepathic.finder.sdk.traffic.request;

import org.ksoap2.serialization.SoapObject;

import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusStation;

import android.text.TextUtils;

public class GetBusStationRequest extends RPCBaseRequest {
    private static final String REQUEST_NAME = "translateToStation";
    private static final String KEY_BUS_NUMBER = "busNum";
    private static final String KEY_GPS_NUMBER = "gps";
    private static final String KEY_STATION_NAME = "stationName";
    private static final String KEY_DIRECTION = "direction";

    private String mLineNumber;
    private String mGpsNumber;

    /** Request example
     *
     * translateToStation{gps=50022; } or translateToStation{busNum=102; gps=50022; }
     */
    public GetBusStationRequest(String lineNumber, String gpsNumber) {
        super(REQUEST_NAME);
        mLineNumber = lineNumber;
        mGpsNumber = gpsNumber;
        if (!TextUtils.isEmpty(lineNumber)) {
            addParameter(KEY_BUS_NUMBER, lineNumber);
        }
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
     * {stationName=新会展中心公交站; alias=奇诺咖啡餐厅; code=200; msg=成功; } or
     * {stationName=新会展中心公交站; alias=奇诺咖啡餐厅; direction=下行; code=200; msg=成功; }
     */
    //{stationName=新会展中心公交站; alias=奇诺咖啡餐厅; direction=下行; code=200; msg=成功; }
    @Override
    protected void handleResponse(SoapObject newDataSet) {
    	KXBusStation station = new KXBusStation();
    	station.setLineNumber(mLineNumber);
    	station.setGpsNumber(mGpsNumber);
        if(newDataSet.getPropertyCount() > 0) {
            SoapObject firstDataEntry = (SoapObject) newDataSet.getProperty(0);
            String name = firstDataEntry.getPrimitivePropertyAsString(KEY_STATION_NAME);
            station.setName(name);
            String direction = null;
            try {
                direction = firstDataEntry.getPrimitivePropertyAsString(KEY_DIRECTION);
            } catch (RuntimeException e) {
                // ignore
            }
            station.setDirection(direction);
        }
        if (mCallback != null) {
        	mCallback.onSuccess(station);
        }
    }
}
