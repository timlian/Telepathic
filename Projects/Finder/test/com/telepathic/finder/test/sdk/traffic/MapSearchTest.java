package com.telepathic.finder.test.sdk.traffic;

import java.util.ArrayList;

import android.os.Handler;
import android.os.Looper;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.baidu.mapapi.search.MKPoiInfo;
import com.telepathic.finder.app.FinderApplication;
import com.telepathic.finder.sdk.ICompletionListener;
import com.telepathic.finder.sdk.ITrafficService;

public class MapSearchTest extends ApplicationTestCase<FinderApplication> {
	private static final String TAG = "MapSearchTest";
	private ITrafficService mTrafficService;
	private Object mLock = new Object();
	private boolean mIsDone;
	private Handler mHandler;
	
	public MapSearchTest() {
		super(FinderApplication.class);
	}

	@Override
	protected void setUp() throws Exception {
		createApplication();
		mTrafficService = getApplication().getTrafficService();
		mHandler = new Handler(Looper.getMainLooper());
		super.setUp();
	}
	

	public void test_busLineSearch() {
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				searchBusLine("成都","102");
			}
		});
		
		waitResponse();
		
	}
	
	private void searchBusLine(String city, String lineNumber) {
		mTrafficService.searchBusLine(city, lineNumber, new ICompletionListener() {
			@Override
			public void onSuccess(Object result) {
				ArrayList<MKPoiInfo> poiInfos = (ArrayList<MKPoiInfo>) result;
				assertNotNull(poiInfos);
				assertEquals(2, poiInfos.size());
				Log.d(TAG, "Success");
				notifyDone();
			}
			
			@Override
			public void onFailure(int errorCode, String errorText) {
				Log.d(TAG, "Failed");
				notifyDone();
			}
		});
	}
	
	private void waitResponse() {
		synchronized (mLock) {
			try {
				while(!mIsDone) {
					mLock.wait();
				}
			} catch (InterruptedException e) {
				Log.d(TAG, "Exception: " + e.getMessage());
			}
		}
	}
	
	private void notifyDone() {
		synchronized (mLock) {
			mIsDone = true;
			mLock.notifyAll();
		}
	}
}
