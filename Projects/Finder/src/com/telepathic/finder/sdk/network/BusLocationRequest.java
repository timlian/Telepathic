package com.telepathic.finder.sdk.network;

import org.ksoap2.serialization.SoapObject;

import com.telepathic.finder.sdk.BusLineRoute;
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

    @Override
    void onResponse(SoapObject result) {
        SoapObject resultObject = (SoapObject) result.getProperty("getBusLocationResult");
        if (resultObject != null) {
            resultObject = (SoapObject) resultObject.getProperty("diffgram");
            if (resultObject != null) {
                resultObject = (SoapObject) resultObject.getProperty("NewDataSet");
                if (resultObject != null) {
                    resultObject = (SoapObject) resultObject.getProperty("Table1");
                    if (resultObject != null) {
                        Log.d("BusLocationRequest", resultObject.toString());
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
