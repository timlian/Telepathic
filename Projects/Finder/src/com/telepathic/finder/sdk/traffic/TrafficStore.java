package com.telepathic.finder.sdk.traffic;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.mapapi.search.MKRoute;
import com.baidu.mapapi.search.MKStep;
import com.telepathic.finder.sdk.traffic.entity.BusCard;
import com.telepathic.finder.sdk.traffic.entity.ConsumerRecord;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusLine;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusRoute;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusStationLines;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData.KuaiXinData;
import com.telepathic.finder.util.Utils;

public class TrafficStore {
    /**
     * The context resolver
     */
    private final ContentResolver mContentResolver;
    /**
     * The executor service
     */
    private final ExecutorService mExecutorService;

    public TrafficStore(Context context, ExecutorService service) {
        mContentResolver = context.getContentResolver();
        mExecutorService = service;
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
        values.put(ITrafficData.BaiDuData.BusRoute.LINE_NUMBER, lineNumber);
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

    public void store(String lineNumber, ArrayList<MKPoiInfo> poisInfo) {
        for (int idx = 0; idx < poisInfo.size(); idx++) {
            ContentValues route = new ContentValues();
            route.put(ITrafficData.BaiDuData.BusRoute.LINE_NUMBER, lineNumber);
            route.put(ITrafficData.BaiDuData.BusRoute.UID, poisInfo.get(idx).uid);
            route.put(ITrafficData.BaiDuData.BusRoute.NAME,  poisInfo.get(idx).name);
            route.put(ITrafficData.BaiDuData.BusRoute.LAST_UPDATE_TIME, System.currentTimeMillis());
            mContentResolver.insert(ITrafficData.BaiDuData.BusRoute.CONTENT_URI, route);
        }
    }

    public void store(String routeUid, MKRoute route) {
        String[] projection = new String[] { ITrafficData.BaiDuData.BusRouteColumns._ID };
        String selection = ITrafficData.BaiDuData.BusRouteColumns.UID + "=?";
        String[] selectionArgs = new String[] { routeUid };
        Cursor cursor = mContentResolver.query(ITrafficData.BaiDuData.BusRoute.CONTENT_URI, projection, selection, selectionArgs, null);
        long routeId = -1;
        if (cursor != null && cursor.moveToFirst()) {
            routeId = cursor.getLong(0);
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
        }
    }

    public void store(KXBusStationLines stationLines) {
        ContentValues station = new ContentValues();
        station.put(KuaiXinData.BusStation.NAME, stationLines.getName());
        station.put(KuaiXinData.BusStation.GPS_NUMBER, stationLines.getGpsNumber());
        station.put(KuaiXinData.BusStation.LAST_UPDATE_TIME, System.currentTimeMillis());
        Uri uri = mContentResolver.insert(KuaiXinData.BusStation.CONTENT_URI, station);
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

}
