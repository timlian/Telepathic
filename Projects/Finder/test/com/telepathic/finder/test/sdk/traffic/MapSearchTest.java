package com.telepathic.finder.test.sdk.traffic;

import java.util.ArrayList;

import android.test.ApplicationTestCase;
import android.util.Log;

import com.baidu.mapapi.search.MKPoiInfo;
import com.telepathic.finder.app.FinderApplication;
import com.telepathic.finder.sdk.ICompletionListener;
import com.telepathic.finder.sdk.ITrafficService;

public class MapSearchTest extends ApplicationTestCase<FinderApplication> {
	private static final String TAG = "MapSearchTest";
	private ITrafficService mTrafficService;
	
	public MapSearchTest() {
		super(FinderApplication.class);
	}

	@Override
	protected void setUp() throws Exception {
		createApplication();
		mTrafficService = getApplication().getTrafficService();
		super.setUp();
	}
	

	public void test_busLineSearch() {
		mTrafficService.searchBusLine("成都", "102", new ICompletionListener() {
			@Override
			public void onSuccess(Object result) {
				ArrayList<MKPoiInfo> poiInfos = (ArrayList<MKPoiInfo>) result;
				assertNotNull(poiInfos);
				assertEquals(2, poiInfos.size());
				Log.d(TAG, "Success");
			}
			
			@Override
			public void onFailure(int errorCode, String errorText) {
				Log.d(TAG, "Failed");
			}
		});
		
		try {
			Thread.sleep(3 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
}
