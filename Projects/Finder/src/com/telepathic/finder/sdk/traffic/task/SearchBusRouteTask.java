package com.telepathic.finder.sdk.traffic.task;

import java.lang.reflect.Field;
import java.util.ArrayList;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKRoute;
import com.baidu.mapapi.search.MKSearch;
import com.baidu.mapapi.search.MKStep;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.telepathic.finder.util.Utils;

public class SearchBusRouteTask extends BaseTask<MKRoute>{
    private static final String TAG = SearchBusRouteTask.class.getSimpleName();
    private MKSearch mMapSearch;
    private String mCity;
    private String mRouteUid;
    private final Object mLock;

    public SearchBusRouteTask(BMapManager manager, String city, String routeUid) {
        super("SearchBusRouteTask");
        mCity = city;
        mRouteUid = routeUid;
        mMapSearch = new MKSearch();
        mMapSearch.init(manager, new BusLineSearchListener());
        mLock = new Object();
    }

    @Override
    public void doWork() {
        mMapSearch.busLineSearch(mCity, mRouteUid);
        synchronized (mLock) {
            try {
                mLock.wait();
            } catch (InterruptedException e) {
                Utils.debug(TAG, "doWork is interrupted.");
            }
        }
    }

    public void searchBusRoute(String city, String routeUid) {
        mMapSearch.busLineSearch(city, routeUid);
    }

    private void debugPoints(ArrayList<ArrayList<GeoPoint>> points, String tag) {
    	Utils.debug(TAG, "### start " + tag + " ###");
    	for (int i = 0; i < points.size(); i++) {
    		ArrayList<GeoPoint> pointList = points.get(i);
    		for(int j = 0; j < pointList.size(); j++) {
    			GeoPoint point = pointList.get(j);
    			Utils.debug(TAG, "Point[" + i + "," + j + "]" + " = " + point.getLatitudeE6() + ", " + point.getLongitudeE6());
    		}
		}
    	Utils.debug(TAG, "### end " + tag + " ###");
    }
    private class BusLineSearchListener extends MKSearchListenerImpl {
        @Override
        public void onGetBusDetailResult(MKBusLineResult result, int error) {
            String busLine = Utils.parseBusLineNumber(result.getBusName()).get(0);
            MKRoute route = result.getBusRoute();
            ArrayList<ArrayList<GeoPoint>> points = route.getArrayPoints();
            debugPoints(route.getArrayPoints(), "External Points");
//            Utils.debug("RouteTest", "From server:");
//            for(int i = 0; i < points.size(); i++) {
//            	Utils.debug("RouteTest", "Route:# " + i);
//            	ArrayList<GeoPoint> pointList = points.get(i);
//            	Utils.debug("RouteTest", "Route Points: ");
//            	for(GeoPoint point : pointList) {
//            		Utils.debug("RouteTest", "Point: " + point.getLatitudeE6() + ", " + point.getLongitudeE6());
//            	}
//            }
//            Utils.debug("RouteTest", "distance: " + route.getDistance());
//            Utils.debug("RouteTest", "End: " + route.getEnd().getLatitudeE6() + ", " + route.getEnd().getLongitudeE6());
//            Utils.debug("RouteTest", "Index: " + route.getIndex());
//            Utils.debug("RouteTest", "num Steps: " + route.getNumSteps());
//            Utils.debug("RouteTest", "route type: " + route.getRouteType());
//            Utils.debug("RouteTest", "Start: " + route.getStart().getLatitudeE6() + ", " + route.getStart().getLongitudeE6());
//            Utils.debug("RouteTest", "Tip: " + route.getTip());
//            Utils.debug("RouteTest", "Stations: ");
//            for(int j = 0; j < route.getNumSteps(); j++) {
//            	MKStep step = route.getStep(j);
//            	Utils.debug("RouteTest", "station: " + step.getContent() + ", location: " + step.getPoint().getLatitudeE6() + ", " + step.getPoint().getLongitudeE6());
//            }
            
			try {
				Class<?> routeClass = MKRoute.class;
	            Field routeField = routeClass.getDeclaredField("a");
				routeField.setAccessible(true);
				ArrayList<ArrayList<GeoPoint>> pointsTest = (ArrayList<ArrayList<GeoPoint>>)routeField.get(route);
				debugPoints(pointsTest, "Internal Points");
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			
			
            TaskResult<MKRoute> taskResult = new TaskResult<MKRoute>();
            taskResult.setErrorCode(error);
            taskResult.setResult(route);
            setTaskResult(taskResult);
            synchronized (mLock) {
                mLock.notifyAll();
            }
        }
    }

}
