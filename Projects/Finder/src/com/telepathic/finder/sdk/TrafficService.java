package com.telepathic.finder.sdk;

import com.baidu.mapapi.BMapManager;
import com.telepathic.finder.sdk.TrafficListener.BusLocationListener;
import com.telepathic.finder.sdk.network.BusChargeRecordRequest;
import com.telepathic.finder.sdk.network.BusLineRouteRequest;
import com.telepathic.finder.sdk.network.NetWorkAdapter;

public class TrafficService implements ITrafficService {

    private static TrafficService mInstance;

    private BusLocationListener mLocationListener;

    private NetWorkAdapter mNetWorkAdapter;
    private BusRoutesStore mRoutesStore;
    private MapSearchHandler mSearchHandler;
    private BusLocationHandler mBusLocationHandler;

    private TrafficService(BMapManager manager) {
        mNetWorkAdapter = new NetWorkAdapter();
        mRoutesStore = new BusRoutesStore();
        mSearchHandler = new MapSearchHandler(manager, mRoutesStore);
        mBusLocationHandler = new BusLocationHandler(this, mNetWorkAdapter);
    }

    public static synchronized TrafficService getTrafficService(BMapManager manager) {
        if (mInstance == null) {
            mInstance = new TrafficService(manager);
        }
        return mInstance;
    }

    @Override
    public void getBusLineRoute(String busLine, BusLineListener listener) {
        BusLineRouteRequest request = new BusLineRouteRequest(busLine, listener);
        mNetWorkAdapter.execute(request);
    }

    /**
     * 
     * @param city
     * @param busNumber
     * @param listener
     */
    public void searchBusLine(String city, String busNumber, TrafficListener.BusLineListener listener) {
        mSearchHandler.searchBusLine(city, busNumber, listener);
    }

    /**
     * 
     * @param city
     * @param routeUid
     * @param listener
     */
    public void searchBusRoute(String city, String routeUid, TrafficListener.BusRouteListener listener) {
        mSearchHandler.searchBusRoute(city, routeUid, listener);
    }

    @Override
    public void getBusLocation(String lineNumber, String gpsNumber,
            String lastStation, BusLocationListener listener) {
        // BusLocationRequest request = new BusLocationRequest(lineNumber, gpsNumber, lastStation, listener);
        //mNetWorkAdapter.execute(request);
    }

    public void retrieveBusLocation(BusRoute route) {
        mBusLocationHandler.retrieveBusLocation(route);
    }

    public void registerBusLocationListener(BusLocationListener listener) {
        mLocationListener = listener;
    }

    public void unregisterBusLocationListener(BusLocationListener listener) {
        mLocationListener = null;
    }

    public BusLocationListener getBusLocationListener() {
        return mLocationListener;
    }

    public void getBusStationLines() {
        //        BusStationLinesRequest request1 = new BusStationLinesRequest("新会展中心公交站", "1", "10");
        //        BusStationLinesRequest request2 = new BusStationLinesRequest("天府软件园东侧站", "1", "10");
        //        BusStationLinesRequest request3 = new BusStationLinesRequest("天华路站", "1", "10");
        //        mNetWorkAdapter.execute(request1);
        //        mNetWorkAdapter.execute(request2);
        //        mNetWorkAdapter.execute(request3);
    }

    @Override
    public void getChargeRecords(String cardId, int count, ChargeRecordsListener listener) {
        BusChargeRecordRequest request = new BusChargeRecordRequest(cardId, String.valueOf(count), listener);
        mNetWorkAdapter.execute(request);
    }

    public BusRoutesStore getRoutesStore() {
        return mRoutesStore;
    }

    public void cancelSearch() {
        mSearchHandler.cancel();
    }

}
