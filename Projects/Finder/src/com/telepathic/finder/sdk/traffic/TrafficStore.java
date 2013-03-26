package com.telepathic.finder.sdk.traffic;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.mapapi.search.MKRoute;
import com.baidu.mapapi.search.MKStep;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.telepathic.finder.sdk.traffic.entity.BusCard;
import com.telepathic.finder.sdk.traffic.entity.ConsumerRecord;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusLine;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusRoute;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusStationLines;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXStationLines;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData.KuaiXinData;
import com.telepathic.finder.util.Utils;

public class TrafficStore {
    private static final String TAG = "TrafficStore";
    
    private static final String[] BUS_ROUTE_PROJECTION = {
    	ITrafficData.KuaiXinData.BusRoute._ID,
    	ITrafficData.KuaiXinData.BusRoute.LINE_NUMBER,
    	ITrafficData.KuaiXinData.BusRoute.DIRECTION,
    	ITrafficData.KuaiXinData.BusRoute.STATIONS
    };
    private static final int IDX_ROUTE_ID = 0;
    private static final int IDX_LINE_NUMBER = 1;
    private static final int IDX_ROUTE_DIRECTION = 2;
    private static final int IDX_ROUTE_STATIONS = 3;
    /**
     * The context resolver
     */
    private final ContentResolver mContentResolver;

    public TrafficStore(Context context, ExecutorService service) {
        mContentResolver = context.getContentResolver();
    }

    public void store(final BusCard busCard, final boolean notifyChange) {
        ContentValues values = new ContentValues();
        values.put(ITrafficData.KuaiXinData.BusCard.CARD_NUMBER, busCard.getCardNumber());
        values.put(ITrafficData.KuaiXinData.BusCard.RESIDUAL_COUNT, busCard.getResidualCount());
        values.put(ITrafficData.KuaiXinData.BusCard.RESIDUAL_AMOUNT, busCard.getResidualAmount());
        values.put(ITrafficData.KuaiXinData.BusCard.LAST_UPDATE_TIME, System.currentTimeMillis());
        Uri uri = mContentResolver.insert(ITrafficData.KuaiXinData.BusCard.CONTENT_URI, values);
        long cardId = Long.parseLong(uri.getLastPathSegment());
        for (ConsumerRecord record : busCard.getConsumerRecords()) {
            values.clear();
            values.put(ITrafficData.KuaiXinData.ConsumerRecord.CARD_ID, cardId);
            values.put(ITrafficData.KuaiXinData.ConsumerRecord.LINE_NUMBER, record.getLineNumber());
            values.put(ITrafficData.KuaiXinData.ConsumerRecord.BUS_NUMBER, record.getBusNumber());
            values.put(ITrafficData.KuaiXinData.ConsumerRecord.DATE, Utils.formatDate(record.getConsumerTime()));
            values.put(ITrafficData.KuaiXinData.ConsumerRecord.CONSUMPTION, record.getConsumption());
            values.put(ITrafficData.KuaiXinData.ConsumerRecord.RESIDUAL, record.getResidual());
            values.put(ITrafficData.KuaiXinData.ConsumerRecord.TYPE, record.getType().toString());
            mContentResolver.insert(ITrafficData.KuaiXinData.ConsumerRecord.CONTENT_URI, values);
        }
        if (notifyChange) {
            mContentResolver.notifyChange(ITrafficData.KuaiXinData.BusCard.CONTENT_URI, null);
        }
    }

    public void store(MKBusLineResult busLine, boolean notifyChange) {
          String lineNumber = Utils.parseBusLineNumber(busLine.getBusName()).get(0);
          MKRoute route = busLine.getBusRoute();
          ContentValues values = new ContentValues();
          //values.put(ITrafficData.BaiDuData.BusRoute.LINE_NUMBER, lineNumber);
          final long routeId = -1;//mTrafficeStore.insertBusRoute(values);
          final int stepNumber = route.getNumSteps();
          for(int index = 0; index < stepNumber; index++) {
            MKStep station = route.getStep(index);
            values.clear();
            values.put(ITrafficData.BaiDuData.BusStation.NAME, station.getContent());
            values.put(ITrafficData.BaiDuData.BusStation.LATITUDE, station.getPoint().getLatitudeE6());
            values.put(ITrafficData.BaiDuData.BusStation.LONGITUDE, station.getPoint().getLongitudeE6());
            final long stationId = -1;//mTrafficeStore.insertBusStation(values);
            values.clear();
            values.put(ITrafficData.BaiDuData.BusRouteStation.ROUTE_ID, routeId);
            values.put(ITrafficData.BaiDuData.BusRouteStation.STATION_ID, stationId);
            values.put(ITrafficData.BaiDuData.BusRouteStation.INDEX, index);
            //mTrafficeStore.insertBusRouteStation(values);
          }
    }

    public void store(String lineNumber, ArrayList<MKPoiInfo> lineRoutes) {
        if (TextUtils.isEmpty(lineNumber) || lineRoutes == null || lineRoutes.size() == 0) {
            return ;
        }
        boolean isFirstRoute = true;
        long lineId = -1;
        for(int idx = 0; idx < lineRoutes.size(); idx++) {
            MKPoiInfo poiInfo = lineRoutes.get(idx);
            int startPos = poiInfo.name.indexOf('(');
            int endPos   = poiInfo.name.indexOf(')');
            String station[] = poiInfo.name.substring(startPos+1, endPos).split("-");
            String firstStation = station[0].trim();
            String lastStation  = station[1].trim();
            if (isFirstRoute) {
                ContentValues line = new ContentValues();
                line.put(ITrafficData.BaiDuData.BusLine.LINE_NUMBER, lineNumber);
                line.put(ITrafficData.BaiDuData.BusLine.CITY, poiInfo.city);
                line.put(ITrafficData.BaiDuData.BusLine.START_STATION, firstStation);
                line.put(ITrafficData.BaiDuData.BusLine.END_STATION, lastStation);
                line.put(ITrafficData.BaiDuData.BusLine.LAST_UPDATE_TIME, System.currentTimeMillis());
                Uri uri = mContentResolver.insert(ITrafficData.BaiDuData.BusLine.CONTENT_URI, line);
                mContentResolver.notifyChange(ITrafficData.BaiDuData.BusLine.CONTENT_URI, null);
                lineId = Long.parseLong(uri.getLastPathSegment());
                isFirstRoute = false;
            }
            ContentValues route = new ContentValues();
            route.put(ITrafficData.BaiDuData.BusRoute.LINE_ID, lineId);
            route.put(ITrafficData.BaiDuData.BusRoute.UID, poiInfo.uid);
            route.put(ITrafficData.BaiDuData.BusRoute.FIRST_STATION, firstStation);
            route.put(ITrafficData.BaiDuData.BusRoute.LAST_STATION, lastStation);
            Uri uri = mContentResolver.insert(ITrafficData.BaiDuData.BusRoute.CONTENT_URI, route);
            Utils.debug(TAG, "insert bus route: " + uri);
        }
    }

    public void store(String routeUid, MKRoute route) {
        String[] projection = new String[] {
                ITrafficData.BaiDuData.BusRouteColumns._ID,
                ITrafficData.BaiDuData.BusRouteColumns.LINE_ID
                };
        String selection = ITrafficData.BaiDuData.BusRouteColumns.UID + "=?";
        String[] selectionArgs = new String[] { routeUid };
        Cursor cursor = mContentResolver.query(ITrafficData.BaiDuData.BusRoute.CONTENT_URI, projection, selection, selectionArgs, null);
        long routeId = -1, lineId = -1;
        if (cursor != null && cursor.moveToFirst()) {
            routeId = cursor.getLong(0);
            lineId  = cursor.getLong(1);
            cursor.close();
        }
        if (routeId > 0) {
            final int stepNumber = route.getNumSteps();
            Uri uri = null;
            for (int index = 0; index < stepNumber; index++) {
                MKStep station = route.getStep(index);
                ContentValues busStation = new ContentValues();
                busStation.put(ITrafficData.BaiDuData.BusStation.NAME,station.getContent());
                busStation.put(ITrafficData.BaiDuData.BusStation.LATITUDE, station.getPoint().getLatitudeE6());
                busStation.put(ITrafficData.BaiDuData.BusStation.LONGITUDE, station.getPoint().getLongitudeE6());
                uri = mContentResolver.insert(ITrafficData.BaiDuData.BusStation.CONTENT_URI, busStation);
                final long stationId = Long.parseLong(uri.getLastPathSegment());
                ContentValues busRouteStation = new ContentValues();
                busRouteStation.put(ITrafficData.BaiDuData.BusRouteStation.ROUTE_ID, routeId);
                busRouteStation.put(ITrafficData.BaiDuData.BusRouteStation.STATION_ID, stationId);
                busRouteStation.put(ITrafficData.BaiDuData.BusRouteStation.INDEX, index);
                mContentResolver.insert(ITrafficData.BaiDuData.BusRouteStation.CONTENT_URI, busRouteStation);
            }
            // store internal points
            ArrayList<ArrayList<GeoPoint>> internalPointsArray = null;
            try {
                Class<?> routeClass = MKRoute.class;
                Field routeField = routeClass.getDeclaredField("a");
                routeField.setAccessible(true);
                internalPointsArray = (ArrayList<ArrayList<GeoPoint>>)routeField.get(route);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if (internalPointsArray != null && internalPointsArray.size() > 0) {
                for (int i = 0; i < internalPointsArray.size(); i++) {
                    ArrayList<GeoPoint>  internalPoints = internalPointsArray.get(i);
                    for(int j = 0; j < internalPoints.size(); j++) {
                        GeoPoint point = internalPoints.get(j);
                        ContentValues pointValues = new ContentValues();
                        pointValues.put(ITrafficData.BaiDuData.BusRoutePoint.ROUTE_ID, routeId);
                        pointValues.put(ITrafficData.BaiDuData.BusRoutePoint.INDEX, j);
                        pointValues.put(ITrafficData.BaiDuData.BusRoutePoint.LATITUDE, point.getLatitudeE6());
                        pointValues.put(ITrafficData.BaiDuData.BusRoutePoint.LONGITUDE, point.getLongitudeE6());
                        mContentResolver.insert(ITrafficData.BaiDuData.BusRoutePoint.CONTENT_URI, pointValues);
                    }
                }
            }
            // update bus line last_update_time
            if (lineId > 0) {
                ContentValues values = new ContentValues();
                values.put(ITrafficData.BaiDuData.BusLine.LAST_UPDATE_TIME, System.currentTimeMillis());
                String where = ITrafficData.BaiDuData.BusLine._ID + "=?";
                mContentResolver.update(ITrafficData.BaiDuData.BusLine.CONTENT_URI, values, where, new String[]{String.valueOf(lineId)});
            }
        }
    }

    public void store(KXBusStationLines stationLines) {
        ContentValues station = new ContentValues();
        station.put(KuaiXinData.BusStation.NAME, stationLines.getName());
        station.put(KuaiXinData.BusStation.GPS_NUMBER, stationLines.getGpsNumber());
        station.put(KuaiXinData.BusStation.LAST_UPDATE_TIME, System.currentTimeMillis());
        Uri uri = mContentResolver.insert(KuaiXinData.BusStation.CONTENT_URI, station);
        mContentResolver.notifyChange(KuaiXinData.BusStation.CONTENT_URI, null);
        long stationId = Long.parseLong(uri.getLastPathSegment());
        for(KXBusLine busLine : stationLines.getAllBusLines()) {
            for(KXBusRoute busRoute : busLine.getAllRoutes()) {
                ContentValues route = new ContentValues();
                route.put(KuaiXinData.BusRoute.LINE_NUMBER, busLine.getLineNumber());
                route.put(KuaiXinData.BusRoute.DIRECTION, busRoute.getDirection().toString());
                route.put(KuaiXinData.BusRoute.START_TIME, busRoute.getStartTime());
                route.put(KuaiXinData.BusRoute.END_TIME, busRoute.getEndTime());
                route.put(KuaiXinData.BusRoute.STATIONS, busRoute.getStationNames());
                //route.put(KuaiXinData.BusRoute.LAST_UPDATE_TIME, System.currentTimeMillis());
                uri = mContentResolver.insert(KuaiXinData.BusRoute.CONTENT_URI, route);
                if (stationLines.contains(busLine.getLineNumber(), busRoute.getDirection())) {
                    long routeId = Long.parseLong(uri.getLastPathSegment());
                    ContentValues routeStation = new ContentValues();
                    routeStation.put(KuaiXinData.BusRouteStation.ROUTE_ID, routeId);
                    routeStation.put(KuaiXinData.BusRouteStation.STATION_ID, stationId);
                    routeStation.put(KuaiXinData.BusRouteStation.INDEX, busRoute.getStationIndex(stationLines.getName()));
                    mContentResolver.insert(KuaiXinData.BusRouteStation.CONTENT_URI, routeStation);
                }
            }
        }
        mContentResolver.notifyChange(KuaiXinData.BusStationLines.CONTENT_URI, null);
    }
    
    public void store(KXBusLine busLine) {
    	for(KXBusRoute busRoute : busLine.getAllRoutes()) {
            ContentValues route = new ContentValues();
            route.put(KuaiXinData.BusRoute.LINE_NUMBER, busLine.getLineNumber());
            route.put(KuaiXinData.BusRoute.DIRECTION, busRoute.getDirection().toString());
            route.put(KuaiXinData.BusRoute.START_TIME, busRoute.getStartTime());
            route.put(KuaiXinData.BusRoute.END_TIME, busRoute.getEndTime());
            route.put(KuaiXinData.BusRoute.STATIONS, busRoute.getStationNames());
            mContentResolver.insert(KuaiXinData.BusRoute.CONTENT_URI, route);
        }
    }
    
    public void store(List<KXStationLines> stationList) {
    	for(KXStationLines station : stationList) {
    		ContentValues stationValues = new ContentValues();
    		stationValues.put(KuaiXinData.BusStation.NAME, station.getName());
    		stationValues.put(KuaiXinData.BusStation.GPS_NUMBER, station.getGpsNumber());
    		stationValues.put(KuaiXinData.BusStation.LAST_UPDATE_TIME, System.currentTimeMillis());
    	    Uri uri = mContentResolver.insert(KuaiXinData.BusStation.CONTENT_URI, stationValues);
    	    long stationId = Long.parseLong(uri.getLastPathSegment());
    		for(String lineNumber : station.getLines()) {
    			int routeId = -1, stationIdx = -1;
    			String direction = station.getDirection(lineNumber);
    			String selection = ITrafficData.KuaiXinData.BusRoute.LINE_NUMBER + "=? AND " +ITrafficData.KuaiXinData.BusRoute.DIRECTION + "=?";
    			String[] selectionArgs = new String[]{lineNumber, direction};
    			Cursor cursor = mContentResolver.query(ITrafficData.KuaiXinData.BusRoute.CONTENT_URI, BUS_ROUTE_PROJECTION, selection, selectionArgs, null);
    			if (cursor != null) {
    				try {
    					if (cursor.moveToFirst()) {
    						routeId = cursor.getInt(IDX_ROUTE_ID);
    						String stations = cursor.getString(IDX_ROUTE_STATIONS);
    						stationIdx = getStationIndex(Arrays.asList(stations.split(",")), station.getName());
    					}
    				} finally {
    					cursor.close();
    				}
    			}
    			if (stationId != -1 && routeId != -1 && stationIdx != -1) {
    				 ContentValues routeStation = new ContentValues();
                     routeStation.put(KuaiXinData.BusRouteStation.ROUTE_ID, routeId);
                     routeStation.put(KuaiXinData.BusRouteStation.STATION_ID, stationId);
                     routeStation.put(KuaiXinData.BusRouteStation.INDEX, stationIdx);
                     mContentResolver.insert(KuaiXinData.BusRouteStation.CONTENT_URI, routeStation);
    			}
    		}
    	}
    }
    
    private int getStationIndex(List<String> stations, String stationName) {
    	int pos = -1;
    	for(int i = 0; i < stations.size(); i++) {
    		if (stationName.equals(stations.get(i))) {
    			pos = i;
    			break;
    		}
    	}
    	return pos;
    }

}
