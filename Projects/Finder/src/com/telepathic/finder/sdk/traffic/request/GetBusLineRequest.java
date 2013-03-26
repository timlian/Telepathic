/**
 * Copyright (C) 2013 Telepathic LTD. All Rights Reserved.
 *
 * * Author: Tim Lian
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
        mCallback.onError(errorCode, errorMessage);
    }

    /*
     * Line route response data entry example:
     *
     * {lineName=111; departureTime=06:00; closeOffTime=22:00; type=锟斤拷锟斤拷; stations=锟斤拷锟斤拷站锟桔合斤拷通锟斤拷纽站,盛锟斤拷一路锟斤拷站,锟斤拷锟斤拷锟斤拷锟秸�桐锟斤拷锟斤拷小锟斤拷站,锟斤拷锟斤拷路站,锟较撅拷锟斤拷路站,锟较撅拷锟斤拷路站,锟较撅拷锟斤拷路锟斤拷站,锟较撅拷锟斤拷路锟斤拷业路锟斤拷站,锟斤拷业路锟斤拷锟斤拷路锟斤拷锟斤拷站,锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷站,锟斤拷锟斤拷路锟斤拷锟侥讹拷站,锟斤拷锟斤拷楼站,锟斤拷锟斤拷路锟斤拷一锟斤拷锟斤拷站,锟斤拷锟斤拷锟斤拷锟秸�锟斤拷锟斤拷路锟斤拷锟斤拷路锟斤拷站,锟斤拷水锟斤拷站,锟斤拷锟斤拷路锟斤拷一锟斤拷站,锟斤拷锟斤拷路锟解华锟斤拷锟斤拷锟秸�锟铰筹拷锟斤拷路锟斤拷站,锟斤拷锟斤拷路锟斤拷站,锟斤拷锟斤拷路锟斤拷站,锟斤拷锟斤拷锟斤拷锟斤拷站,锟斤拷锟斤拷锟斤拷锟斤拷站,锟斤拷锟斤拷小锟斤拷站,锟斤拷路同锟斤拷路锟斤拷站,锟斤拷路站,锟斤拷路锟斤拷站,锟斤拷路锟斤拷犀锟斤拷锟斤拷锟脚讹拷站,锟斤拷锟斤拷路站,锟竭家达拷站,锟斤拷锟脚达拷站,锟竭硷拷小锟斤拷站; stationAliases= , , , , , , , , , , , , , , , , , , , , , , , , , , , , , , , , ; code=200; msg=锟缴癸拷; }
     *
     */
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
