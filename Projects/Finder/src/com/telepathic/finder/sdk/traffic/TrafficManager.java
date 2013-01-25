
package com.telepathic.finder.sdk.traffic;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKAddrInfo;
import com.baidu.mapapi.MKBusLineResult;
import com.baidu.mapapi.MKDrivingRouteResult;
import com.baidu.mapapi.MKPoiInfo;
import com.baidu.mapapi.MKPoiResult;
import com.baidu.mapapi.MKRoute;
import com.baidu.mapapi.MKSearch;
import com.baidu.mapapi.MKSearchListener;
import com.baidu.mapapi.MKStep;
import com.baidu.mapapi.MKSuggestionResult;
import com.baidu.mapapi.MKTransitRouteResult;
import com.baidu.mapapi.MKWalkingRouteResult;
import com.telepathic.finder.sdk.ITrafficListeners;
import com.telepathic.finder.sdk.ITrafficMonitor;
import com.telepathic.finder.sdk.ITrafficService;
import com.telepathic.finder.sdk.ITrafficeMessage;
import com.telepathic.finder.sdk.traffic.network.GetBusLocationRequest;
import com.telepathic.finder.sdk.traffic.network.GetConsumerRecordRequest;
import com.telepathic.finder.sdk.traffic.network.NetworkManager;
import com.telepathic.finder.sdk.traffic.store.ITrafficeStore.BusRouteColumns;
import com.telepathic.finder.sdk.traffic.store.ITrafficeStore.BusRouteStationColumns;
import com.telepathic.finder.sdk.traffic.store.ITrafficeStore.BusStationColumns;
import com.telepathic.finder.sdk.traffic.store.TrafficeStore;
import com.telepathic.finder.util.Utils;

public class TrafficManager {

    private static TrafficManager mInstance;

    private Context mAppContext;

    private MKSearch mMapSearch;

    private NetworkManager mNetWorkAdapter;
    
    private TrafficeStore mTrafficeStore;

    private TrafficeMonitor mTrafficeMonitor;
    
    private Handler mMessageHandler;

    private MyConsumerRecordsListener mConsumerRecordsListener;

    private TrafficManager(BMapManager manager, Context appContext, Handler msgHandler) {
        mAppContext = appContext;
        mNetWorkAdapter = new NetworkManager();
        mTrafficeStore =  TrafficeStore.getDefaultStore(mAppContext);
        mMapSearch = new MKSearch();
        mMapSearch.init(manager, new MapSearchListener());
        mTrafficeMonitor = new TrafficeMonitor();
        Utils.debug("TrafficManager", String.valueOf(System.identityHashCode(mAppContext.getMainLooper())));
        mMessageHandler = msgHandler;
    }

    public static synchronized TrafficManager getTrafficManager(BMapManager manager,
            Context appContext, Handler handler) {
        if (mInstance == null) {
            mInstance = new TrafficManager(manager, appContext, handler);
        }
        return mInstance;
    }
    
    public ITrafficService getTrafficService() {
        return new TrafficeService();
    }

    private class TrafficeService implements ITrafficService {

        @Override
        public void searchBusLine(String city, String lineNumber) {
            mMapSearch.poiSearchInCity(city, lineNumber);
        }

        @Override
        public void searchBusRoute(String city, String routeUid) {
            mMapSearch.busLineSearch(city, routeUid);
        }

        @Override
        public void getBusStationLines(String stationName) {
            // TODO Auto-generated method stub

        }

        // @Override
        // public void getBusLineRoute(String LineNumber, BusLineListener
        // listener) {
        // // TODO Auto-generated method stub
        //
        // }
        
        @Override
        public void getBusLocation(String lineNumber, String anchorStation, String lastStation) {

        }

        @Override
        public void getConsumerRecords(String cardId, int count) {
            if (mConsumerRecordsListener == null) {
                mConsumerRecordsListener = new MyConsumerRecordsListener();
                mTrafficeMonitor.add(mConsumerRecordsListener);
            }
            GetConsumerRecordRequest request = new GetConsumerRecordRequest(mTrafficeMonitor,
                    cardId, count);
            mNetWorkAdapter.execute(request);
        }

        @Override
        public void translateToStation(String lineNumber, String gpsNumber) {
            // TODO Auto-generated method stub

        }

        @Override
        public void translateToStation(String gpsNumber) {
            // TODO Auto-generated method stub

        }

        @Override
        public ITrafficMonitor getTrafficMonitor() {
            return mTrafficeMonitor;
        }
        
        @Override
        public TrafficeStore getTrafficeStore() {
        	return mTrafficeStore;
        }
        
        @Override
        public void getBusLocation(BusRoute route) {
            GetBusLocationRequest request = new GetBusLocationRequest(mTrafficeMonitor, route);
            mNetWorkAdapter.execute(request);
        }

    }

    private class MyConsumerRecordsListener implements ITrafficListeners.ConsumerRecordsListener {
        @Override
        public void onReceived(ConsumptionInfo info) {
        	Message msg = Message.obtain();
        	msg.arg1 = ITrafficeMessage.RECEIVED_CONSUMER_RECORDS;
        	msg.arg2 = 0;
        	msg.obj = info;
        	mMessageHandler.sendMessage(msg);
            for (ConsumerRecord consumerRecord : info.getRecordList()) {
            	mTrafficeStore.insertRecord(consumerRecord);
            }
        }
    }

    private class MapSearchListener implements MKSearchListener {

        @Override
        public void onGetAddrResult(MKAddrInfo arg0, int arg1) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onGetBusDetailResult(MKBusLineResult result, int error) {
            String busLine = Utils.parseBusLineNumber(result.getBusName()).get(0);
            MKRoute route = result.getBusRoute();
            ContentValues values = new ContentValues();
            values.put(BusRouteColumns.LINE_NUMBER, busLine);
            final long routeId = mTrafficeStore.insertBusRoute(values);
            final int stepNumber = route.getNumSteps();
            for(int index = 0; index < stepNumber; index++) {
            	MKStep station = route.getStep(index);
            	values.clear();
            	values.put(BusStationColumns.NAME, station.getContent());
            	values.put(BusStationColumns.LATITUDE, station.getPoint().getLatitudeE6());
            	values.put(BusStationColumns.LONGITUDE, station.getPoint().getLongitudeE6());
            	final long stationId = mTrafficeStore.insertBusStation(values);
            	values.clear();
            	values.put(BusRouteStationColumns.ROUTE_ID, routeId);
            	values.put(BusRouteStationColumns.STATION_ID, stationId);
            	values.put(BusRouteStationColumns.INDEX, index);
            	mTrafficeStore.insertBusRouteStation(values);
            }
            mTrafficeMonitor.setUpdate(new BusRoute(busLine, route));
        }

        @Override
        public void onGetDrivingRouteResult(MKDrivingRouteResult arg0, int arg1) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onGetPoiDetailSearchResult(int arg0, int arg1) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onGetPoiResult(MKPoiResult res, int type, int error) {
            ArrayList<MKPoiInfo> busPois = null;
            String busLineNumber = null;
            if (error == 0 || res != null) {
                ArrayList<MKPoiInfo> allPois = res.getAllPoi();
                if (allPois != null && allPois.size() > 0) {
                    busPois = new ArrayList<MKPoiInfo>();
                    for (MKPoiInfo poiInfo : allPois) {
                        // poi类型，0：普通点，1：公交站，2：公交线路，3：地铁站，4：地铁线路
                        if (poiInfo.ePoiType == 2) {
                            if (busLineNumber == null) {
                                busLineNumber = Utils.parseBusLineNumber(poiInfo.name).get(0);
                            }
                            busPois.add(poiInfo);
                        }
                    }
                }
            }
            mTrafficeMonitor.setUpdate(busLineNumber, busPois);
        }

        @Override
        public void onGetRGCShareUrlResult(String arg0, int arg1) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onGetSuggestionResult(MKSuggestionResult arg0, int arg1) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onGetTransitRouteResult(MKTransitRouteResult arg0, int arg1) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onGetWalkingRouteResult(MKWalkingRouteResult arg0, int arg1) {
            // TODO Auto-generated method stub

        }
    }

}
