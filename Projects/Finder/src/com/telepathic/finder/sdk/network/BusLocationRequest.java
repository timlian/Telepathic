package com.telepathic.finder.sdk.network;

import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;

import com.telepathic.finder.sdk.ProcessListener.BusLocationListener;
import com.telepathic.finder.sdk.BusRoute;

public class BusLocationRequest extends RPCRequest {

    private static final String METHOD_NAME = "getBusLocation";

    private static final String KEY_RESPONSE = "getBusLocationResult";
    private static final String KEY_LINE_NUMBER  = "lineNumber";
    private static final String KEY_GPS_NUMBER = "GPSNumber";
    private static final String KEY_LAST_STATION = "lastStation";
    private static final String KEY_DISTANCE = "distance";
    private static final int INVALID_POS_CURSOR = -1;

    private BusRoute mRoute;
    private int mPosCursor;
    
    private BusLocationListener mListener;
    
    public BusLocationRequest(String lineNumber,String currentStation, String lastStation, BusLocationListener listener) {
          super(METHOD_NAME);
          addParameter(KEY_LINE_NUMBER, lineNumber);
          addParameter(KEY_GPS_NUMBER, currentStation);
          addParameter(KEY_LAST_STATION, lastStation);
          mListener = listener;
    }
    
    public BusLocationRequest(BusRoute route, BusLocationListener listener) {
        super(METHOD_NAME);
        addParameter(KEY_LINE_NUMBER, route.getLineNumber());
        addParameter(KEY_GPS_NUMBER, route.getLastStation());
        addParameter(KEY_LAST_STATION, route.getLastStation());
        mRoute = route;
        mPosCursor = route.getStationCount() - 1;
        mListener = listener;
  }

    @Override
    protected boolean isComplete() {
    	return mPosCursor <= 0;
    }
    
    @Override
    void onResponse(Object result, String errorMessage) {
        if (errorMessage != null) {
            if (mListener != null) {
                mListener.onError(errorMessage);
            }
            return ;
        }
        if (result instanceof SoapObject) {
            final SoapObject response = (SoapObject)((SoapObject)result).getProperty(KEY_RESPONSE);
            process(response);
        } else if (result instanceof SoapFault) {

        } else {
            throw new RuntimeException("Unknown Exception!!!");
        }
    }

    private void setPositionCursor(int distance) {
    	if (distance >= 0) {
        	mPosCursor -= distance - 1;
        } else {
        	mPosCursor = INVALID_POS_CURSOR;
        }
    	
        if (!isComplete()) {
    		addParameter(KEY_GPS_NUMBER, mRoute.getStationName(mPosCursor));
    	}
    }
    
    /*
     * Location response data example:
     *
     * {lineNumber=102; distance=1; code=200; msg=³É¹¦; }
     *
     */
    private void process(SoapObject response) {
        if (response != null) {
            final SoapObject diffGram = (SoapObject) response.getProperty(KEY_DIFF_GRAM);
            if (diffGram != null) {
                final SoapObject newDataSet = (SoapObject) diffGram.getProperty(KEY_NEW_DATA_SET);
                if (newDataSet != null) {
                    final SoapObject firstDataEntry = (SoapObject) newDataSet.getProperty(0);
                    final String errorCode = firstDataEntry.getPrimitivePropertyAsString(KEY_ERROR_CODE);
                    final String errorMessage = firstDataEntry.getPrimitivePropertyAsString(KEY_ERROR_MESSAGE);
                    if (NO_ERROR == Integer.parseInt(errorCode)) {
                        final String lineNumber = firstDataEntry.getPrimitivePropertyAsString(KEY_LINE_NUMBER);
                        final int distance = Integer.parseInt(firstDataEntry.getPrimitivePropertyAsString(KEY_DISTANCE));
                        setPositionCursor(distance);
                        if (mListener != null && mPosCursor > 0) {
                            mListener.onSuccess(mRoute.getStation(mPosCursor));
                        }
                    } else {
                        if (mListener != null) {
                            mListener.onError(errorMessage);
                        }
                    }
                }
            }
        }
    }

}
