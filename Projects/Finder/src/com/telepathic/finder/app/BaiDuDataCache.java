package com.telepathic.finder.app;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import com.baidu.mapapi.search.MKRoute;
import com.baidu.mapapi.search.MKStep;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.telepathic.finder.sdk.traffic.entity.baidu.BDBusLine;
import com.telepathic.finder.sdk.traffic.entity.baidu.BDBusRoute;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData;
import com.telepathic.finder.util.Utils;

import android.R.integer;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

public class BaiDuDataCache {
	private static final String TAG = "BaiDuDataCache";
	/**
	 * The app context
	 */
	private Context mContext;
	/**
	 * The content resolver
	 */
	private ContentResolver mContentResolver;
	
	private static final String[] BUS_LINE_PROJECTION = {
		ITrafficData.BaiDuData.BusLine.LINE_NUMBER
	};
	private static final int IDX_BUS_LINE_NUMBER = 0;
	
	private static final String[] BUS_ROUTE_PROJECTON = {
		ITrafficData.BaiDuData.BusLine.CITY,
		ITrafficData.BaiDuData.BusRoute.UID,
		ITrafficData.BaiDuData.BusRoute.FIRST_STATION,
		ITrafficData.BaiDuData.BusRoute.LAST_STATION 
	};
	private static final int IDX_BUS_ROUTE_CITY = 0;
	private static final int IDX_BUS_ROUTE_UID = 1;
	private static final int IDX_BUS_ROUTE_FIRST_STATION = 2;
	private static final int IDX_BUS_ROUTE_LAST_STATION = 3;
	    
    private static final String[] BUS_ROUTE_STATION_PROJECTION = {
    	ITrafficData.BaiDuData.BusStation.NAME,
    	ITrafficData.BaiDuData.BusStation.LATITUDE,
    	ITrafficData.BaiDuData.BusStation.LONGITUDE
    };
    private static final int IDX_BUS_STATION_NAME = 0;
    private static final int IDX_BUS_STATION_LATITUDE  = 1;
    private static final int IDX_BUS_STATION_LONGITUDE = 2;
    
    private static final String[] BUS_ROUTE_POINT_PROJECTION = {
    	ITrafficData.BaiDuData.BusRoutePoint.INDEX,
    	ITrafficData.BaiDuData.BusRoutePoint.LATITUDE,
    	ITrafficData.BaiDuData.BusRoutePoint.LONGITUDE,
    };
    private static final int IDX_BUS_ROUTE_POINT_INDEX = 0;
    private static final int IDX_BUS_ROUTE_POINT_LATITUDE  = 1;
    private static final int IDX_BUS_ROUTE_POINT_LONGITUDE = 2;
    
	
	public BaiDuDataCache(Context context) {
		mContext = context;
		if (mContext != null) {
			mContentResolver = mContext.getContentResolver();
		}
	}

	public String getRouteLineNumber(String uid) {
		String lineNumber = null;
		if (!TextUtils.isEmpty(uid)) {
			Cursor cursor = queryRouteLineNumber(uid);
			if (cursor != null && cursor.moveToFirst()) {
				try {
					lineNumber = cursor.getString(IDX_BUS_LINE_NUMBER);
				} finally {
					cursor.close();
				}
			}
		}
		return lineNumber;
	}
	
	public BDBusLine getBusLine(String lineNumber) {
		BDBusLine retLine = null;
		Cursor cursor = queryBusLineRoutes(lineNumber);
		if (cursor != null && cursor.moveToFirst()) {
			try {
				BDBusLine line = new BDBusLine(lineNumber);
				do {
					BDBusRoute route = new BDBusRoute();
					route.setCity(cursor.getString(IDX_BUS_ROUTE_CITY));
					route.setUid(cursor.getString(IDX_BUS_ROUTE_UID));
					route.setFirstStation(cursor.getString(IDX_BUS_ROUTE_FIRST_STATION));
					route.setLastStation(cursor.getString(IDX_BUS_ROUTE_LAST_STATION));
					line.addRoute(route);
				} while (cursor.moveToNext());
				retLine = line;
			} finally {
				cursor.close();
			}
		}
		return retLine;
	}
	
	public MKRoute getRoute(String uid) {
		MKRoute retRoute = null;
    	ArrayList<ArrayList<GeoPoint>> routePoints = null;
    	ArrayList<MKStep> routeSteps= getRouteSteps(uid);
    	if (routeSteps != null && routeSteps.size() > 0) {
    		routePoints = getRoutePoints(uid);
    	}
    	if (routePoints == null || routePoints.size() == 0) {
    		return null;
    	}
    	
    	MKStep firstStep = routeSteps.get(0);
    	MKStep lastStep = routeSteps.get(routeSteps.size() - 1);
		
		try {
			Class<?> routeClass = MKRoute.class;
			Method setStepList = routeClass.getDeclaredMethod("a", ArrayList.class);
			Method setTip = routeClass.getDeclaredMethod("a", String.class);
			Method setStart = routeClass.getDeclaredMethod("a", GeoPoint.class);
			Method setDistance = routeClass.getDeclaredMethod("a", int.class);
			Method setIndex = routeClass.getDeclaredMethod("b", int.class);
			Method setEnd = routeClass.getDeclaredMethod("b", GeoPoint.class);
			Method setPoints = routeClass.getDeclaredMethod("b", ArrayList.class);
			Method setRouteType = routeClass.getDeclaredMethod("c", int.class);
			Field routeField = routeClass.getDeclaredField("a");
			
			setStepList.setAccessible(true);
			setTip.setAccessible(true);
			setStart.setAccessible(true);
			setDistance.setAccessible(true);
			setIndex.setAccessible(true);
			setEnd.setAccessible(true);
			setPoints.setAccessible(true);
			setRouteType.setAccessible(true);
			routeField.setAccessible(true);
			
			MKRoute receiver = new MKRoute();
			setStepList.invoke(receiver, routeSteps);
			//setTip.invoke(route, "");
			//setPoints.invoke(route, points);
			setStart.invoke(receiver, firstStep.getPoint());
			setEnd.invoke(receiver, lastStep.getPoint());
			setDistance.invoke(receiver, 0);
			setIndex.invoke(receiver, 0);
			setRouteType.invoke(receiver, MKRoute.ROUTE_TYPE_BUS_LINE);
			routeField.set(receiver, routePoints);
			retRoute = receiver;
		} catch (Exception e) {
			Utils.debug(TAG, "getRoute catch " + e.getClass().getName() + ": " + e.getMessage());
		}
		return retRoute;
	}
	
	private ArrayList<MKStep> getRouteSteps(String uid) {
    	ArrayList<MKStep> stepList = null;
    	Cursor cursor = queryBusRouteStations(uid);
    	if (cursor != null && cursor.getCount() > 1 && cursor.moveToFirst()) {
    		try {
    			Method setPoint = null, setContent = null;
    			try {
    				Class<?> stepClass = MKStep.class;
    	    		setPoint = stepClass.getDeclaredMethod("a", GeoPoint.class);
    				setContent = stepClass.getDeclaredMethod("a", String.class);
    				setPoint.setAccessible(true);
    				setContent.setAccessible(true);
    			} catch (NoSuchMethodException e) {
    				Utils.debug(TAG, "MKStep has no such method: " + e.getMessage());
    			}
				if (setPoint != null && setContent != null) {
					stepList = new ArrayList<MKStep>();
					do {
						String content = cursor.getString(IDX_BUS_STATION_NAME);
						int latitude = cursor.getInt(IDX_BUS_STATION_LATITUDE);
						int longitude = cursor.getInt(IDX_BUS_STATION_LONGITUDE);
						try {
							MKStep step = new MKStep();
							setPoint.invoke(step, new GeoPoint(latitude,longitude));
							setContent.invoke(step, content);
							stepList.add(step);
						} catch (Exception e) {
							Utils.debug(TAG, "MKStep invoke exception: " + e.getMessage());
						}
					} while (cursor.moveToNext());
				}
    		} finally {
    			cursor.close();
    		}
    	}
    	return stepList;
    }
	
	private ArrayList<ArrayList<GeoPoint>> getRoutePoints(String uid) {
		ArrayList<ArrayList<GeoPoint>> result = null;
		ArrayList<GeoPoint> routePoints = null;
		Cursor cursor = queryBusRoutePoints(uid);
		if (cursor != null && cursor.getCount() > 1 && cursor.moveToFirst()) {
			try {
				routePoints = new ArrayList<GeoPoint>();
				do {
					int latitude = cursor.getInt(IDX_BUS_ROUTE_POINT_LATITUDE);
					int longitude = cursor.getInt(IDX_BUS_ROUTE_POINT_LONGITUDE);
					routePoints.add(new GeoPoint(latitude, longitude));
				} while (cursor.moveToNext());
			} finally {
				cursor.close();
			}
		}
		if (routePoints != null && routePoints.size() > 0) {
			result = new ArrayList<ArrayList<GeoPoint>>();
			result.add(routePoints);
		}
		return result;
	}
	
    private Cursor queryBusRouteStations(String routeUid) {
        String selection = ITrafficData.BaiDuData.BusRoute.UID + "=?";
        String[] args = new String[]{ routeUid };
        Cursor cursor = mContentResolver.query(ITrafficData.BaiDuData.BusRoute.CONTENT_URI_WITH_STATION, BUS_ROUTE_STATION_PROJECTION, selection, args, null);
        return cursor;
    }
    
    private Cursor queryBusRoutePoints(String routeUid) {
        String selection = ITrafficData.BaiDuData.BusRoute.UID + "=?";
        String[] args = new String[]{ routeUid };
        Cursor cursor = mContentResolver.query(ITrafficData.BaiDuData.BusRoute.CONTENT_URI_WITH_POINT, BUS_ROUTE_POINT_PROJECTION, selection, args, null);
        Utils.printCursorContent("Test", cursor);
        return cursor;
    }
    
    private Cursor queryBusLineRoutes(String lineNumber) {
        String selection = ITrafficData.BaiDuData.BusLine.LINE_NUMBER + "=?";
        String[] args = new String[]{ lineNumber };
        Cursor cursor = mContentResolver.query(ITrafficData.BaiDuData.BusLine.CONTENT_URI_WITH_ROUTE, BUS_ROUTE_PROJECTON, selection, args, null);
        return cursor;
    }
    
    private Cursor queryRouteLineNumber(String routeUid) {
    	 String selection = ITrafficData.BaiDuData.BusRoute.UID + "=?";
         String[] args = new String[]{ routeUid };
         Cursor cursor = mContentResolver.query(ITrafficData.BaiDuData.BusLine.CONTENT_URI_WITH_ROUTE, BUS_LINE_PROJECTION, selection, args, null);
         return cursor;
    }

}
