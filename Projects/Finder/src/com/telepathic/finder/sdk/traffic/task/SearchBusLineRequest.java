package com.telepathic.finder.sdk.traffic.task;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

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
import com.telepathic.finder.sdk.traffic.store.ITrafficeStore.BusRouteColumns;
import com.telepathic.finder.sdk.traffic.store.ITrafficeStore.BusRouteStationColumns;
import com.telepathic.finder.sdk.traffic.store.ITrafficeStore.BusStationColumns;
import com.telepathic.finder.util.Utils;

public class SearchBusLineRequest {
    private static MKSearch sMapSearch;
    private static ContentResolver sContentResolver;
    
    public static void init(Context context, BMapManager manager) {
    	if (sContentResolver == null) {
    		sContentResolver = context.getContentResolver();
    	}
    	if (sMapSearch == null) {
    		sMapSearch = new MKSearch();
    		sMapSearch.init(manager, new MapSearchListener());
    	}
    }

	public void searchBusLine(String city, String lineNumber) {
		sMapSearch.poiSearchInCity(city, lineNumber);
	}

	public void searchBusRoute(String city, String routeUid) {
		sMapSearch.busLineSearch(city, routeUid);
	}
	
    private static class MapSearchListener implements MKSearchListener {

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
            final long routeId = -1;//mTrafficeStore.insertBusRoute(values);
            final int stepNumber = route.getNumSteps();
            for(int index = 0; index < stepNumber; index++) {
            	MKStep station = route.getStep(index);
            	values.clear();
            	values.put(BusStationColumns.NAME, station.getContent());
            	values.put(BusStationColumns.LATITUDE, station.getPoint().getLatitudeE6());
            	values.put(BusStationColumns.LONGITUDE, station.getPoint().getLongitudeE6());
            	final long stationId = -1;//mTrafficeStore.insertBusStation(values);
            	values.clear();
            	values.put(BusRouteStationColumns.ROUTE_ID, routeId);
            	values.put(BusRouteStationColumns.STATION_ID, stationId);
            	values.put(BusRouteStationColumns.INDEX, index);
            	//mTrafficeStore.insertBusRouteStation(values);
            }
          //  mTrafficeMonitor.setUpdate(new BusRoute(busLine, route));
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
          //  mTrafficeMonitor.setUpdate(busLineNumber, busPois);
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
