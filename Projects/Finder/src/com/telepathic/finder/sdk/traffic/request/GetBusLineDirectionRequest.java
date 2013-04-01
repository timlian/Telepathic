package com.telepathic.finder.sdk.traffic.request;

import org.ksoap2.serialization.SoapObject;

import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusLineDirection;

public class GetBusLineDirectionRequest extends RPCBaseRequest {
	private static final String REQUEST_NAME = "translateToStation";
    private static final String KEY_DIRECTION = "direction";
    private static final String KEY_GPS_NUMBER = "gps";
    private static final String KEY_LINE_NUMBER = "busNum";
    private String mLineNumber;
    
    /** Request example
    *
    * translateToStation{busNum=102; gps=50022}
    */
    public GetBusLineDirectionRequest(String lineNumber, String gpsNumber) {
    	super(REQUEST_NAME);
    	mLineNumber = lineNumber;
    	addParameter(KEY_LINE_NUMBER, lineNumber);
    	addParameter(KEY_GPS_NUMBER, gpsNumber);
    }
    
	@Override
	void handleError(int errorCode, String errorMessage) {
		if (mCallback != null) {
			mCallback.onError(errorCode, errorMessage);
		}
	}

	/**
     * Response example:
     *
     * {stationName=新会展中心公交站; alias=奇诺咖啡餐厅; direction=下行}
     */
	@Override
	void handleResponse(SoapObject dataSet) {
		KXBusLineDirection lineDirection = new KXBusLineDirection();
		lineDirection.setLineNumber(mLineNumber);
        if(dataSet.getPropertyCount() > 0) {
            SoapObject firstDataEntry = (SoapObject) dataSet.getProperty(0);
            try {
            	String direction = firstDataEntry.getPrimitivePropertyAsString(KEY_DIRECTION);
            	lineDirection.setDirection(direction);
            } catch (RuntimeException e) {
            	// ignore
            }
        }
        if (mCallback != null) {
        	mCallback.onSuccess(lineDirection);
        }
	}

}
