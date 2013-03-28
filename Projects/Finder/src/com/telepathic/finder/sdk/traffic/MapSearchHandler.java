package com.telepathic.finder.sdk.traffic;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import android.text.TextUtils;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.search.MKBusLineResult;
import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKRoute;
import com.baidu.mapapi.search.MKSearch;
import com.telepathic.finder.sdk.ICompletionListener;
import com.telepathic.finder.sdk.IErrorCode;
import com.telepathic.finder.sdk.traffic.task.MKSearchListenerImpl;
import com.telepathic.finder.util.Utils;

class MapSearchHandler {
	private static final String TAG = "MapSearchHandler";
	private MyMapSearchListener mMapSearchListener;
    private MKSearch mMapSearch;
	private TrafficStore mTrafficStore;
	private CopyOnWriteArrayList<BusLineSearchRequest> mBusLineSearchRequests = new CopyOnWriteArrayList<BusLineSearchRequest>();
	private CopyOnWriteArrayList<BusRouteSearchRequest> mBusRouteSearchRequests = new CopyOnWriteArrayList<BusRouteSearchRequest>();
	
	interface SearchRequest{
		void doSearch();
	}
	
	MapSearchHandler(BMapManager manager, TrafficStore store) {
        mMapSearch = new MKSearch();
        mMapSearchListener = new MyMapSearchListener();
        mMapSearch.init(manager, mMapSearchListener);
        mTrafficStore = store;
	}

	void searchBusLine(String city, String lineNumber, ICompletionListener listener) {
		mBusLineSearchRequests.add(new BusLineSearchRequest(city, lineNumber, listener));
		doSearch();
	}
	
	void searchBusRoute(String city, String routeUid, ICompletionListener listener) {
		mBusRouteSearchRequests.add(new BusRouteSearchRequest(city, routeUid, listener));
		doSearch();
	}
	
	private void doSearch() {
		if (mBusLineSearchRequests.size() > 0) {
			SearchRequest searchRequest = mBusLineSearchRequests.get(0);
			searchRequest.doSearch();
			Utils.debug(TAG, "start bus line search.");
			return ;
		}
		if (mBusRouteSearchRequests.size() > 0) {
			SearchRequest searchRequest = mBusRouteSearchRequests.get(0);
			searchRequest.doSearch();
			Utils.debug(TAG, "start bus route search.");
			return ;
		}
		Utils.debug(TAG, "doSearch - there is no search request.");
	}
	
	private class MyMapSearchListener extends MKSearchListenerImpl {
		
		@Override
		public void onGetPoiResult(MKPoiResult res, int type, int error) {
			handleBusLineSearchResult(res, type, error);
			doSearch();
		}

		@Override
		public void onGetBusDetailResult(MKBusLineResult result, int error) {
			handleBusRouteSearchResult(result, error);
			doSearch();
		}
	}
	
	private void handleBusLineSearchResult(MKPoiResult res, int type, int error) {
		BusLineSearchRequest searchRequest = null;
		if (mBusLineSearchRequests.size() > 0) {
			searchRequest = mBusLineSearchRequests.remove(0);
		}
		if (searchRequest == null) {
			Utils.debug(TAG, "handleBusLineSearchResult - there is no search request!");
			return ;
		}
		ICompletionListener listener = searchRequest.getListener();
		String lineNumber = searchRequest.getLineNumber();
		if (listener == null && TextUtils.isEmpty(lineNumber)) {
			Utils.debug(TAG, "Exception: listener or lineNumber is null.");
			return ;
		}
		if (error != 0) {
			Utils.debug(TAG, "bus line search failed - error = " + error);
			listener.onFailure(error, "bus line search failed - error: " + error + ", lineNumber = " + searchRequest.getLineNumber());
			return;
		}
		if (res == null || res.getAllPoi() == null || res.getAllPoi().size() == 0) {
			Utils.debug(TAG, "poi search exception: the result is empty.");
			listener.onFailure(error, "bus line search failed, the result is empty. lineNumber = " + searchRequest.getLineNumber());
			return;
		}
		ArrayList<MKPoiInfo> poiList = new ArrayList<MKPoiInfo>();
		for (MKPoiInfo poiInfo : res.getAllPoi()) {
			if (poiInfo.ePoiType == 2) { // 2：公交线路
				poiList.add(poiInfo);
			}
		}
		if (poiList.size() > 0) {
			mTrafficStore.store(lineNumber, poiList);
		}
		listener.onSuccess(poiList);
	}

	private void handleBusRouteSearchResult(MKBusLineResult result, int error) {
		BusRouteSearchRequest searchRequest = null;
		if (mBusRouteSearchRequests.size() > 0) {
			searchRequest = mBusRouteSearchRequests.remove(0);
		}
		if (searchRequest == null) {
			Utils.debug(TAG, "handleBusRouteSearchResult - there is no search request!");
			return ;
		}
		ICompletionListener listener = searchRequest.getListener();
		String routeUid = searchRequest.getRouteUid();
		if (listener == null && TextUtils.isEmpty(routeUid)) {
			Utils.debug(TAG, "Exception: listener or route uid is null.");
			return ;
		}
		// String busLine = Utils.parseBusLineNumber(result.getBusName()).get(0);
		if (error != 0) {
			Utils.debug(TAG, "bus route search failed - error = " + error);
			listener.onFailure(error, "bus route search failed");
			return;
		}
		final MKRoute route = result.getBusRoute();
		if (result == null || route == null) {
			Utils.debug(TAG, "bus route search exception: the result is empty.");
			listener.onFailure(IErrorCode.ERROR_UNKNOWN, "bus route search failed");
			return;
		}
		mTrafficStore.store(routeUid, route);
		listener.onSuccess(route);
	}
	
	private class BusLineSearchRequest implements SearchRequest {
		private String mCity;
		private String mLineNumber;
		private ICompletionListener mListener;
		
		BusLineSearchRequest(String city, String lineNumber, ICompletionListener listener) {
			mCity = city;
			mLineNumber = lineNumber;
			mListener = listener;
		}
		
		@Override
		public void doSearch() {
			mMapSearch.poiSearchInCity(mCity, mLineNumber);
		}
		
		public String getLineNumber() {
			return mLineNumber;
		}
		
		public ICompletionListener getListener() {
			return mListener;
		}
	}
	
	private class BusRouteSearchRequest implements SearchRequest {
		private String mCity;
		private String mRouteUid;
		private ICompletionListener mListener;
		
		BusRouteSearchRequest(String city, String routeUid, ICompletionListener listener) {
			mCity = city;
			mRouteUid = routeUid;
			mListener = listener;
		}
		
		@Override
		public void doSearch() {
			mMapSearch.busLineSearch(mCity, mRouteUid);
		}
		
		public String getRouteUid() {
			return mRouteUid;
		}
		
		public ICompletionListener getListener() {
			return mListener;
		}
	}
	
}
