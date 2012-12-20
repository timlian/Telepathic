package com.telepathic.finder.sdk;

import com.baidu.mapapi.BMapManager;
import com.telepathic.finder.sdk.network.BusChargeRecordRequest;
import com.telepathic.finder.sdk.network.BusLineRouteRequest;
import com.telepathic.finder.sdk.network.BusLocationRequest;
import com.telepathic.finder.sdk.network.BusStationLinesRequest;
import com.telepathic.finder.sdk.network.NetWorkAdapter;

public class TrafficService implements ITrafficService {

    private static TrafficService mInstance;

    private NetWorkAdapter mNetWorkAdapter;
    private BMapManager mMapManager;

    public TrafficService(BMapManager mapManager) {
        mNetWorkAdapter = new NetWorkAdapter();
        mMapManager = mapManager;
    }

    public static synchronized TrafficService getTrafficService(BMapManager mapManager) {
        if (mInstance == null) {
            mInstance = new TrafficService(mapManager);
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

    public void getBusLocation(String lineNumber, String currentStation) {
        
    }
    
    public void getBusStationLines() {
        BusStationLinesRequest request1 = new BusStationLinesRequest("新会展中心公交站", "1", "10");
        BusStationLinesRequest request2 = new BusStationLinesRequest("天府软件园东侧站", "1", "10");
        BusStationLinesRequest request3 = new BusStationLinesRequest("天华路站", "1", "10");
        mNetWorkAdapter.execute(request1);
        mNetWorkAdapter.execute(request2);
        mNetWorkAdapter.execute(request3);
    }
    
    @Override
    public void getChargeRecords(String cardId, int count, ChargeRecordsListener listener) {
        BusChargeRecordRequest request = new BusChargeRecordRequest(cardId, String.valueOf(count), listener);
        mNetWorkAdapter.execute(request);
    }
}
