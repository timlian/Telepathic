package com.telepathic.finder.sdk.traffic;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
	private static final int REQUEST_TIMEOUT = 15 * 1000;
	private static int REQUEST_ID = 0;
	private MyMapSearchListener mMapSearchListener;
    private MKSearch mMapSearch;
	private TrafficStore mTrafficStore;
	private CopyOnWriteArrayList<BusLineSearchRequest> mBusLineSearchRequests = new CopyOnWriteArrayList<BusLineSearchRequest>();
	private CopyOnWriteArrayList<BusRouteSearchRequest> mBusRouteSearchRequests = new CopyOnWriteArrayList<BusRouteSearchRequest>();
	private boolean mIsSearching;
	private Handler mHandler;
	
	abstract class SearchRequest{
		private int mId;
		private ICompletionListener mListener;
		
		SearchRequest(int id, ICompletionListener listener) {
			mId = id;
			mListener = listener;
		}
		
		int getId() {
			return mId;
		}
		
		ICompletionListener getListener() {
			return mListener;
		}
		
		abstract void doSearch();
	}
	
	MapSearchHandler(BMapManager manager, TrafficStore store) {
        mMapSearch = new MKSearch();
        mMapSearchListener = new MyMapSearchListener();
        mMapSearch.init(manager, mMapSearchListener);
        mTrafficStore = store;
        mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
			@Override
			public boolean handleMessage(Message msg) {
				onSearchTimeout(msg.arg1);
				return true;
			}
		});
	}

	void searchBusLine(String city, String lineNumber, ICompletionListener listener) {
		mBusLineSearchRequests.add(new BusLineSearchRequest(++REQUEST_ID, listener, city, lineNumber));
		if (!mIsSearching) {
			doSearch();
		} else {
			Utils.debug(TAG, "searchBusLine - request is queued.");
		}
	}
	
	void searchBusRoute(String city, String routeUid, ICompletionListener listener) {
		mBusRouteSearchRequests.add(new BusRouteSearchRequest(++REQUEST_ID, listener, city, routeUid));
		if (!mIsSearching) {
			doSearch();
		} else {
			Utils.debug(TAG, "searchBusRoute - request is queued.");
		}
	}
	
	private void doSearch() {
		SearchRequest searchRequest = null;
		if (mBusLineSearchRequests.size() > 0) {
			searchRequest = mBusLineSearchRequests.get(0);
		}
		if (mBusRouteSearchRequests.size() > 0) {
			searchRequest = mBusRouteSearchRequests.get(0);
		}
		if (searchRequest != null) {
			searchRequest.doSearch();
			mIsSearching = true;
			Message msg = Message.obtain();
			msg.arg1 = searchRequest.getId();
			mHandler.sendMessageDelayed(msg, REQUEST_TIMEOUT);
		} else {
			Utils.debug(TAG, "map search finished - there is no search request.");
		}
	}
	
	private class MyMapSearchListener extends MKSearchListenerImpl {
		
		@Override
		public void onGetPoiResult(MKPoiResult res, int type, int error) {
			try {
				handleBusLineSearchResult(res, type, error);
			} catch (Exception e) {
				Utils.debug(TAG, "onGetPoiResult - handle bus line search result crashed: " + e.getMessage());
			} finally {
				onSearchCompleted();
			}
		}

		@Override
		public void onGetBusDetailResult(MKBusLineResult result, int error) {
			try {
				handleBusRouteSearchResult(result, error);
			} catch (Exception e) {
				Utils.debug(TAG, "onGetBusDetailResult - handle bus route search result crashed: " + e.getMessage());
			} finally {
				onSearchCompleted();
			}
		}
	}
	
	private void onSearchCompleted() {
		mIsSearching = false;
		doSearch();
	}
	
	private void onSearchTimeout(int requestId) {
		SearchRequest searchRequest = remove(requestId);
		if (searchRequest != null) {
			ICompletionListener listener = searchRequest.getListener();
			if (listener != null) {
				listener.onFailure(IErrorCode.ERROR_TIME_OUT, "map search timeout.");
			}
			Utils.debug(TAG, "onSearchTimeout - search request timeout, requestId: " + requestId);
		}
		mIsSearching = false;
		doSearch();
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
	
	private SearchRequest remove(final int requestId) {
		if (requestId <= 0) {
			Utils.debug(TAG, "remove - invalid request id: " + requestId);
			return null;
		}
		SearchRequest searchRequest = null;
		for(int i = 0 ; i < mBusLineSearchRequests.size(); i++) {
			if (mBusLineSearchRequests.get(i).getId() == requestId) {
				searchRequest = mBusLineSearchRequests.remove(i);
				break;
			}
		}
		if (searchRequest == null) {
			for(int i = 0; i < mBusRouteSearchRequests.size(); i++) {
				if (mBusRouteSearchRequests.get(i).getId() ==  requestId) {
					searchRequest = mBusRouteSearchRequests.remove(i);
					break;
				}
			}
		}
		return searchRequest;
	}
	
	private class BusLineSearchRequest extends SearchRequest {
		private String mCity;
		private String mLineNumber;
		
		BusLineSearchRequest(int id, ICompletionListener listener, String city, String lineNumber) {
			super(id, listener);
			mCity = city;
			mLineNumber = lineNumber;
		}
		
		@Override
		public void doSearch() {
			mMapSearch.poiSearchInCity(mCity, mLineNumber);
		}
		
		public String getLineNumber() {
			return mLineNumber;
		}
	}
	
	private class BusRouteSearchRequest extends SearchRequest {
		private String mCity;
		private String mRouteUid;
		
		BusRouteSearchRequest(int id, ICompletionListener listener, String city, String routeUid) {
			super(id, listener);
			mCity = city;
			mRouteUid = routeUid;
		}
		
		@Override
		public void doSearch() {
			mMapSearch.busLineSearch(mCity, mRouteUid);
		}
		
		public String getRouteUid() {
			return mRouteUid;
		}
	}
	
}
