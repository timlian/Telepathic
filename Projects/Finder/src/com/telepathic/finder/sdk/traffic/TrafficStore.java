package com.telepathic.finder.sdk.traffic;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.baidu.mapapi.MKBusLineResult;
import com.baidu.mapapi.MKPoiInfo;
import com.baidu.mapapi.MKRoute;
import com.baidu.mapapi.MKStep;
import com.telepathic.finder.sdk.traffic.entity.BusCard;
import com.telepathic.finder.sdk.traffic.entity.BusLine;
import com.telepathic.finder.sdk.traffic.entity.BusLine.Direction;
import com.telepathic.finder.sdk.traffic.entity.BusStationLines;
import com.telepathic.finder.sdk.traffic.entity.ConsumerRecord;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData;
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
	
	public void store(BusStationLines stationLines) {
		ContentValues station = new ContentValues();
		station.put(ITrafficData.KuaiXinData.BusStation.NAME, stationLines.getStationName());
		station.put(ITrafficData.KuaiXinData.BusStation.GPS_NUMBER, stationLines.getGpsNumber());
		Uri uri = mContentResolver.insert(ITrafficData.KuaiXinData.BusStation.CONTENT_URI, station);
		long stationId = Long.parseLong(uri.getLastPathSegment());
		for(BusLine line : stationLines.getBusLines()) {
			for(Direction direction : line.getRouteMap().keySet()) {
	    		ContentValues route = new ContentValues();
	    		route.put(ITrafficData.KuaiXinData.BusRoute.LINE_NUMBER, line.getLineNumber());
	    		route.put(ITrafficData.KuaiXinData.BusRoute.DIRECTION, direction.toString());
	    		route.put(ITrafficData.KuaiXinData.BusRoute.START_TIME, line.getStartTime());
	    		route.put(ITrafficData.KuaiXinData.BusRoute.END_TIME, line.getEndTime());
	    		route.put(ITrafficData.KuaiXinData.BusRoute.STATIONS, line.getRouteStations(direction));
	    		route.put(ITrafficData.KuaiXinData.BusRoute.LAST_UPDATE_TIME, System.currentTimeMillis());
	    		uri = mContentResolver.insert(ITrafficData.KuaiXinData.BusRoute.CONTENT_URI, route);
	    		long routeId = Long.parseLong(uri.getLastPathSegment());
	    		ContentValues routeStation = new ContentValues();
	    		routeStation.put(ITrafficData.KuaiXinData.BusRouteStation.ROUTE_ID, routeId);
	    		routeStation.put(ITrafficData.KuaiXinData.BusRouteStation.STATION_ID, stationId);
	    		int index = line.getStationIndex(direction, stationLines.getStationName());
	    		routeStation.put(ITrafficData.KuaiXinData.BusRouteStation.INDEX, index);
	    		mContentResolver.insert(ITrafficData.KuaiXinData.BusRouteStation.CONTENT_URI, routeStation);
	    	}
		}
	}

}
