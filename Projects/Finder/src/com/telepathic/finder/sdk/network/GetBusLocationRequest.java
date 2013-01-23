package com.telepathic.finder.sdk.network;

import org.ksoap2.serialization.SoapObject;

import com.telepathic.finder.sdk.BusRoute;
import com.telepathic.finder.sdk.TrafficeMonitor;

public class GetBusLocationRequest extends RPCBaseRequest {
    private static final String METHOD_NAME = "getBusLocation";
    private static final String KEY_LINE_NUMBER  = "lineNumber";
    private static final String KEY_GPS_NUMBER = "GPSNumber";
    private static final String KEY_LAST_STATION = "lastStation";
    private static final String KEY_DISTANCE = "distance";

    private static final int INVALID_POS_CURSOR = -1;
    
    private TrafficeMonitor mTrafficeMonitor;

    private BusRoute mRoute;
    private int mPosCursor;

    public GetBusLocationRequest(TrafficeMonitor monitor, BusRoute route) {
        super(METHOD_NAME);
        addParameter(KEY_LINE_NUMBER, route.getLineNumber());
        addParameter(KEY_GPS_NUMBER, route.getLastStation());
        addParameter(KEY_LAST_STATION, route.getLastStation());
        mRoute = route;
        mPosCursor = route.getStationCount() - 1;
        mTrafficeMonitor = monitor;
    }

    @Override
    protected boolean isComplete() {
        return mPosCursor <= 0;
    }

    private void setPositionCursor(int distance) {
        if (distance > 0) {
            mPosCursor = mPosCursor - distance;
        } else {
            mPosCursor = mPosCursor - 1;
        }

        if (!isComplete()) {
            setParameter(KEY_GPS_NUMBER, mRoute.getStationName(mPosCursor));
        }
    }

    @Override
    protected void handleError(String errorMessage) {
    	mTrafficeMonitor.setError(errorMessage);
    }

    /*
     * Location response data example:
     *
     * {lineNumber=102; distance=1; code=200; msg=�ɹ�; }
     *
     */
    @Override
    protected void handleResponse(SoapObject newDataSet) {
        final SoapObject firstDataEntry = (SoapObject) newDataSet.getProperty(0);
        final String lineNumber = firstDataEntry.getPrimitivePropertyAsString(KEY_LINE_NUMBER);
        final int distance = Integer.parseInt(firstDataEntry.getPrimitivePropertyAsString(KEY_DISTANCE));
        setPositionCursor(distance);
        if (mPosCursor >= 0 && distance >= 0) {
            mTrafficeMonitor.setUpdate(mRoute.getStation(mPosCursor));
        }
    }
}
