package com.telepathic.finder.sdk.traffic;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.baidu.mapapi.MKBusLineResult;
import com.baidu.mapapi.MKPoiInfo;
import com.baidu.mapapi.MKRoute;
import com.baidu.mapapi.MKStep;
import com.telepathic.finder.sdk.traffic.entity.BusCard;
import com.telepathic.finder.sdk.traffic.entity.ConsumerRecord;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData;
import com.telepathic.finder.sdk.traffic.task.NetworkManager;
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
    	values.put(ITrafficData.BusCard.CARD_NUMBER, busCard.getCardNumber());
    	values.put(ITrafficData.BusCard.RESIDUAL_COUNT, busCard.getResidualCount());
    	values.put(ITrafficData.BusCard.RESIDUAL_AMOUNT, busCard.getResidualAmount());
    	values.put(ITrafficData.BusCard.LAST_UPDATE_TIME, System.currentTimeMillis());
    	Uri uri = mContentResolver.insert(ITrafficData.BusCard.CONTENT_URI, values);
    	long cardId = Long.parseLong(uri.getLastPathSegment());
    	for (ConsumerRecord record : busCard.getConsumerRecords()) {
    		values.clear();
    		values.put(ITrafficData.ConsumerRecord.CARD_ID, cardId);
    		values.put(ITrafficData.ConsumerRecord.LINE_NUMBER, record.getLineNumber());
    		values.put(ITrafficData.ConsumerRecord.BUS_NUMBER, record.getBusNumber());
    		values.put(ITrafficData.ConsumerRecord.DATE, Utils.formatDate(record.getConsumerTime()));
    		values.put(ITrafficData.ConsumerRecord.CONSUMPTION, record.getConsumption());
    		values.put(ITrafficData.ConsumerRecord.RESIDUAL, record.getResidual());
    		values.put(ITrafficData.ConsumerRecord.TYPE, record.getType().toString());
    		mContentResolver.insert(ITrafficData.ConsumerRecord.CONTENT_URI, values);
        }
    	if (notifyChange) {
    		mContentResolver.notifyChange(ITrafficData.BusCard.CONTENT_URI, null);
    	}
	}
	
	public void store(MKBusLineResult busLine, boolean notifyChange) {
		  String lineNumber = Utils.parseBusLineNumber(busLine.getBusName()).get(0);
          MKRoute route = busLine.getBusRoute();
          ContentValues values = new ContentValues();
          values.put(ITrafficData.BusRoute.LINE_NUMBER, lineNumber);
          final long routeId = -1;//mTrafficeStore.insertBusRoute(values);
          final int stepNumber = route.getNumSteps();
          for(int index = 0; index < stepNumber; index++) {
          	MKStep station = route.getStep(index);
          	values.clear();
          	values.put(ITrafficData.BusStation.NAME, station.getContent());
          	values.put(ITrafficData.BusStation.LATITUDE, station.getPoint().getLatitudeE6());
          	values.put(ITrafficData.BusStation.LONGITUDE, station.getPoint().getLongitudeE6());
          	final long stationId = -1;//mTrafficeStore.insertBusStation(values);
          	values.clear();
          	values.put(ITrafficData.BusRouteStation.ROUTE_ID, routeId);
          	values.put(ITrafficData.BusRouteStation.STATION_ID, stationId);
          	values.put(ITrafficData.BusRouteStation.INDEX, index);
          	//mTrafficeStore.insertBusRouteStation(values);
          }
	}
	
	private static String getStationName(String stationName) {
		if (!TextUtils.isEmpty(stationName)) {
			 if (stationName.charAt(stationName.length() - 1) != '\u7AD9') {
				 stationName += '\u7AD9';
             }
		}
		return stationName;
	}
	
	public void store(String lineNumber, ArrayList<MKPoiInfo> poisInfo) {
        for (int idx = 0; idx < poisInfo.size(); idx++) {
            int startPos = poisInfo.get(idx).name.indexOf('(');
            int endPos   = poisInfo.get(idx).name.indexOf(')');
            String[] line = poisInfo.get(idx).name.substring(startPos+1, endPos).split("-");
            String firstStation = line[0];
            String lastStation = line[1];
            ContentValues route = new ContentValues();
            route.put(ITrafficData.BusRoute.LINE_NUMBER, lineNumber);
            route.put(ITrafficData.BusRoute.ROUTE_UID, poisInfo.get(idx).uid);
            route.put(ITrafficData.BusRoute.FIRST_STATION, getStationName(firstStation));
            route.put(ITrafficData.BusRoute.LAST_STATION, getStationName(lastStation));
            route.put(ITrafficData.BusRoute.LAST_UPDATE_TIME, System.currentTimeMillis());
            mContentResolver.insert(ITrafficData.BusRoute.CONTENT_URI, route);
        }
	}
	
	public void store(String routeUid, MKRoute route) {
		String[] projection = new String[] { ITrafficData.BusRouteColumns._ID };
		String selection = ITrafficData.BusRouteColumns.ROUTE_UID + "=?";
		String[] selectionArgs = new String[] { routeUid };
		Cursor cursor = mContentResolver.query(ITrafficData.BusRoute.CONTENT_URI, projection, selection, selectionArgs, null);
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
				busStation.put(ITrafficData.BusStation.NAME,station.getContent());
				busStation.put(ITrafficData.BusStation.LATITUDE, station.getPoint().getLatitudeE6());
				busStation.put(ITrafficData.BusStation.LONGITUDE, station.getPoint().getLongitudeE6());
				uri = mContentResolver.insert(ITrafficData.BusStation.CONTENT_URI, busStation);
				final long stationId = Long.parseLong(uri.getLastPathSegment());
				ContentValues busRouteStation = new ContentValues();
				busRouteStation.put(ITrafficData.BusRouteStation.ROUTE_ID, routeId);
				busRouteStation.put(ITrafficData.BusRouteStation.STATION_ID, stationId);
				busRouteStation.put(ITrafficData.BusRouteStation.INDEX, index);
				mContentResolver.insert(ITrafficData.BusRouteStation.CONTENT_URI, busRouteStation);
			}
		}
	}
	

}
