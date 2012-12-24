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
        mListener = listener;
  }

    @Override
    void onRequestComplete(Object result, String errorMessage) {
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
                        final String distance = firstDataEntry.getPrimitivePropertyAsString(KEY_DISTANCE);
                        if (mListener != null) {
                            mListener.onSuccess(lineNumber, Integer.parseInt(distance));
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
