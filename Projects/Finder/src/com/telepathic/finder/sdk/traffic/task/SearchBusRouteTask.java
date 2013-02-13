package com.telepathic.finder.sdk.traffic.task;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKBusLineResult;
import com.baidu.mapapi.MKRoute;
import com.baidu.mapapi.MKSearch;
import com.telepathic.finder.util.Utils;

public class SearchBusRouteTask extends BaseTask<MKRoute>{
	private MKSearch mMapSearch;
	private String mCity;
	private String mRouteUid;
	
	public SearchBusRouteTask(BMapManager manager, String city, String routeUid) {
		super("SearchBusRouteTask");
		mCity = city;
		mRouteUid = routeUid;
		mMapSearch = new MKSearch();
		mMapSearch.init(manager, new BusLineSearchListener());
	}
	
	@Override
	public void doWork() {
		mMapSearch.busLineSearch(mCity, mRouteUid);
	}
	
	public void searchBusRoute(String city, String routeUid) {
		mMapSearch.busLineSearch(city, routeUid);
	}
	
	private class BusLineSearchListener extends MKSearchListenerImpl {
        @Override
        public void onGetBusDetailResult(MKBusLineResult result, int error) {
            String busLine = Utils.parseBusLineNumber(result.getBusName()).get(0);
            MKRoute route = result.getBusRoute();
            TaskResult<MKRoute> taskResult = new TaskResult<MKRoute>();
            taskResult.setErrorCode(error);
            taskResult.setResult(route);
            setTaskResult(taskResult);
        }
    }
	
}
