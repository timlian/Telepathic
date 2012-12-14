package com.telepathic.finder.sdk;

import com.telepathic.finder.sdk.network.BusLineRouteRequest;
import com.telepathic.finder.sdk.network.BusLocationRequest;
import com.telepathic.finder.sdk.network.NetWorkAdapter;

public class TrafficService implements ITrafficService {

    private static TrafficService mInstance;

    private NetWorkAdapter mNetWorkAdapter;

    private TrafficService() {
        mNetWorkAdapter = new NetWorkAdapter();
    }

    public static synchronized TrafficService getTrafficService() {
        if (mInstance == null) {
            mInstance = new TrafficService();
        }
        return mInstance;
    }

    @Override
    public void getBusLineRoute(String busLine, BusLineListener listener) {
        BusLineRouteRequest request = new BusLineRouteRequest(busLine, listener);
        mNetWorkAdapter.execute(request);
    }

    @Override
    public void getBusLocation(String lineNumber, String gpsNumber,
            String lastStation, BusLocationListener listener) {
        BusLocationRequest request = new BusLocationRequest(lineNumber, gpsNumber, lastStation, listener);
        mNetWorkAdapter.execute(request);
    }
}
