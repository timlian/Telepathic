package com.telepathic.finder.sdk;

import java.util.ArrayList;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKAddrInfo;
import com.baidu.mapapi.MKBusLineResult;
import com.baidu.mapapi.MKDrivingRouteResult;
import com.baidu.mapapi.MKPoiInfo;
import com.baidu.mapapi.MKPoiResult;
import com.baidu.mapapi.MKSearch;
import com.baidu.mapapi.MKSearchListener;
import com.baidu.mapapi.MKSuggestionResult;
import com.baidu.mapapi.MKTransitRouteResult;
import com.baidu.mapapi.MKWalkingRouteResult;

import com.telepathic.finder.sdk.TrafficListener.BusLineListener;
import com.telepathic.finder.sdk.TrafficListener.BusRouteListener;
import com.telepathic.finder.util.Utils;

public class MapSearchHandler implements MKSearchListener {
    
    private MKSearch mMapSearch;
    
    private BusLineListener mBusLineListener;
    private BusRouteListener mBusRouteListener;
    private BusRoutesStore mRoutesStore;
    private String uid;
    
    MapSearchHandler(BMapManager manager, BusRoutesStore store) {
        mMapSearch = new MKSearch();
        mMapSearch.init(manager, this);
        mRoutesStore = store;
    }

    public void searchBusLine(String city, String busNumber, BusLineListener listener) {
        mBusLineListener = listener;
        mMapSearch.poiSearchInCity(city, busNumber);
    }
    
    public void searchBusRoute(String city, String routeUid, BusRouteListener listener) {
        uid = routeUid;
        mBusRouteListener = listener;
        mMapSearch.busLineSearch(city, routeUid);
    }
    
    @Override
    public void onGetBusDetailResult(MKBusLineResult result, int error) {
        if (mBusRouteListener != null) {
            String busLine = Utils.parseBusLineNumber(result.getBusName()).get(0);
            BusRoute route = new BusRoute(busLine, result.getBusRoute());
            mRoutesStore.add(uid, route);
            mBusRouteListener.done(route, error);
        }
    }
    
    @Override
    public void onGetPoiResult(MKPoiResult res, int type, int error) {
        ArrayList<MKPoiInfo> busPois = null;
        if (error == 0 || res != null) {
            ArrayList<MKPoiInfo> allPois = res.getAllPoi();
            if (allPois != null && allPois.size() > 0) {
                busPois= new ArrayList<MKPoiInfo>();
                for(MKPoiInfo poiInfo : allPois) {
                    // poi类型，0：普通点，1：公交站，2：公交线路，3：地铁站，4：地铁线路
                    if (poiInfo.ePoiType == 2) {
                        busPois.add(poiInfo);
                    }
                }
            }
        }
        if (mBusLineListener != null) {
            mBusLineListener.done(busPois, error);
        }
    }
    
    @Override
    public void onGetAddrResult(MKAddrInfo arg0, int arg1) {
        // Nothing need to do.
    }

    @Override
    public void onGetDrivingRouteResult(MKDrivingRouteResult arg0, int arg1) {
        // Nothing need to do.
    }

    @Override
    public void onGetPoiDetailSearchResult(int arg0, int arg1) {
        // Nothing need to do.
    }

    @Override
    public void onGetRGCShareUrlResult(String arg0, int arg1) {
        // Nothing need to do.
    }

    @Override
    public void onGetSuggestionResult(MKSuggestionResult arg0, int arg1) {
        // Nothing need to do.
    }

    @Override
    public void onGetTransitRouteResult(MKTransitRouteResult arg0, int arg1) {
        // Nothing need to do.
    }

    @Override
    public void onGetWalkingRouteResult(MKWalkingRouteResult arg0, int arg1) {
        // Nothing need to do.
    }
}
