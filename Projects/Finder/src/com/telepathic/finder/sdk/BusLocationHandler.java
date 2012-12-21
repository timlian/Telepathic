package com.telepathic.finder.sdk;

import java.util.ArrayList;

import com.telepathic.finder.sdk.ProcessListener;
import com.telepathic.finder.sdk.network.BusLocationRequest;
import com.telepathic.finder.sdk.network.NetWorkAdapter;

public class BusLocationHandler {
    
    private ArrayList<BusLineListener> mLocListeners;
    private NetWorkAdapter mNetWorkAdapter;
    private BusRoute mBusRoute;
    private TrafficService mService;
    
    BusLocationHandler(TrafficService service, NetWorkAdapter adapter) {
        mService = service;
        mNetWorkAdapter = adapter;
    }
    
    public void retrieveBusLocation(BusRoute route) {
        mBusRoute = route;
        BusLocationRequest request = new BusLocationRequest(
                route.getLineNumber(), route.getLastStation(),
                route.getLastStation(), new MyBusLocationListener());
        mNetWorkAdapter.execute(request);
    }
    
    public void addListener(BusLineListener listener) {
        if (!mLocListeners.contains(listener)) {
            mLocListeners.add(listener);
        }
    }
    
    public void removeListener(BusLineListener listener) {
        mLocListeners.remove(listener);
    }
    
    private class MyBusLocationListener implements ProcessListener.BusLocationListener {
        
        @Override
        public void onSuccess(String lineNumber, int distance) {
            if (mService.getBusLocationListener() != null) {
                mService.getBusLocationListener().onLocationUpdated(mBusRoute.getStation(distance));
                int index = mBusRoute.setIndex(distance);
                if (mBusRoute.needContinue()) {
                    BusLocationRequest request = new BusLocationRequest(mBusRoute.getLineNumber(), mBusRoute.getStationName(index), mBusRoute.getLastStation(), this);
                    mNetWorkAdapter.execute(request);
                }
            }
        }

        @Override
        public void onError(String errorMessage) {
            // TODO Auto-generated method stub
            
        }
    }

}
