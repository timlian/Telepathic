package com.telepathic.finder.sdk.traffic;

import java.util.concurrent.ExecutorService;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.baidu.mapapi.MKBusLineResult;
import com.baidu.mapapi.MKRoute;
import com.baidu.mapapi.MKStep;
import com.telepathic.finder.sdk.traffic.entity.BusCard;
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
		mExecutorService.execute(new Runnable() {
			@Override
			public void run() {
				ContentValues values = new ContentValues();
		    	values.put(ITrafficData.BusCard.CARD_NUMBER, busCard.getCardNumber());
		    	values.put(ITrafficData.BusCard.RESIDUAL_COUNT, busCard.getResidualCount());
		    	values.put(ITrafficData.BusCard.RESIDUAL_AMOUNT, busCard.getResidualAmount());
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
		});
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

}
