package com.telepathic.finder.sdk.network;

import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;

import com.telepathic.finder.sdk.BusLocationListener;

import android.util.Log;

public class BusLocationRequest extends RPCRequest {

    private static final String METHOD_NAME = "getBusLocation";

    private static final String KEY_LINE_NUMBER  = "lineNumber";
    private static final String KEY_GPS_NUMBER = "GPSNumber";
    private static final String KEY_LAST_STATION = "lastStation";
    private static final String KEY_DISTANCE = "distance";

    private BusLocationListener mListener;

    public BusLocationRequest(String lineName, String gpsNumber, String lastStation, BusLocationListener listener) {
        super(METHOD_NAME);
        addParameter(KEY_GPS_NUMBER, gpsNumber);
        addParameter(KEY_LAST_STATION, lastStation);
        addParameter(KEY_LINE_NUMBER, lineName);
        mListener = listener;
    }

    private int getErrorCode(SoapObject returnInfo) {
        int retCode = -1;
        String errCode = returnInfo.getPrimitivePropertyAsString("code");
        String errMessage = returnInfo.getPrimitivePropertyAsString("msg");
        retCode = Integer.parseInt(errCode);
        if (retCode != 200 && mListener != null) {
            mListener.onError(errMessage);
        }
        return retCode;
    }
    
    private void process(SoapObject response) {
        SoapObject resultObject = (SoapObject) ((SoapObject)response).getProperty("getBusLocationResult");
        if (resultObject != null) {
            resultObject = (SoapObject) resultObject.getProperty("diffgram");
            if (resultObject != null) {
                resultObject = (SoapObject) resultObject.getProperty("NewDataSet");
                if (resultObject != null) {
                    resultObject = (SoapObject) resultObject.getProperty("Table1");
                    if (resultObject != null) {
                        if (getErrorCode(resultObject) == 200) {
                            String lineNumber = resultObject.getPrimitivePropertyAsString(KEY_LINE_NUMBER);
                            String distance = resultObject.getPrimitivePropertyAsString(KEY_DISTANCE);
                            if (mListener != null) {
                                mListener.onSuccess(lineNumber, distance);
                            }
                        }
                    }
                }
            }
        } 
    }
    
    @Override
    void onRequestComplete(Object response, String errorMessage) {
        if (errorMessage != null) {
            if (mListener != null) {
                mListener.onError(errorMessage);
            }
            return ;
        }
        if (response instanceof SoapObject) {
            process((SoapObject) response);
        } else if (response instanceof SoapFault) {
            
        } else {
            throw new RuntimeException("Unknown Exception!!!");
        }

    }
}
