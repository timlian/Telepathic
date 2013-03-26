/**
 * Copyright (C) 2013 Telepathic LTD. All Rights Reserved.
 *
 * * Status: Pass
 */
package com.telepathic.finder.sdk.traffic.request;

import org.ksoap2.serialization.SoapObject;

import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusLine;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusRoute;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusLine.Direction;

public class GetBusLineRequest extends RPCBaseRequest {
    private static final String METHOD_NAME = "getBusLineRoute";
    // parameter keys
    private static final String KEY_BUS_LINE = "busLine";
    // response keys
    //private static final String KEY_LINE_NAME = "lineName";
    private static final String KEY_DEPARTURE_TIME ="departureTime";
    private static final String KEY_CLOSE_OFF_TIME = "closeOffTime";
    private static final String KEY_DIRECTION = "type";
    private static final String KEY_STATIONS = "stations";

    private String mLineNumber;

    public GetBusLineRequest(String lineNumber) {
        super(METHOD_NAME);
        addParameter(KEY_BUS_LINE, lineNumber);
        mLineNumber = lineNumber;
    }

    @Override
    void handleError(int errorCode, String errorMessage) {
    	if (mCallback != null) {
    		mCallback.onError(errorCode, errorMessage);
    	}
    }

    // Response can not be null.
    @Override
    void handleResponse(SoapObject newDataSet) {
        KXBusLine busLine = new KXBusLine(mLineNumber);
        final int count = newDataSet.getPropertyCount();
        SoapObject dataEntry = null;
        for (int idx = 0; idx < count; idx++) {
            dataEntry = (SoapObject) newDataSet.getProperty(idx);
            String stations = dataEntry.getPrimitivePropertyAsString(KEY_STATIONS);
            if (stations != null && !stations.equals("")) {
                KXBusRoute busRoute = new KXBusRoute();
                busRoute.setStations(stations.split(","));
                //String lineNumber = dataEntry.getPrimitivePropertyAsString(KEY_LINE_NAME);
                busRoute.setStartTime(dataEntry.getPrimitivePropertyAsString(KEY_DEPARTURE_TIME));
                busRoute.setEndTime(dataEntry.getPrimitivePropertyAsString(KEY_CLOSE_OFF_TIME));
                busRoute.setDirection(Direction.fromString(dataEntry.getPrimitivePropertyAsString(KEY_DIRECTION)));
                busLine.addRoute(busRoute);
            }
        }
        if (mCallback != null) {
            mCallback.onSuccess(busLine);
        }
    }
}
