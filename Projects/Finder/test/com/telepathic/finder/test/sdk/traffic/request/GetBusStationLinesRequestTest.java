package com.telepathic.finder.test.sdk.traffic.request;

import java.util.ArrayList;

import android.test.ApplicationTestCase;
import android.util.Log;

import com.telepathic.finder.app.FinderApplication;
import com.telepathic.finder.sdk.IErrorCode;
import com.telepathic.finder.sdk.ITrafficService;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXStationLines;
import com.telepathic.finder.sdk.traffic.request.GetBusStationLinesRequest;
import com.telepathic.finder.sdk.traffic.request.RequestCallback;
import com.telepathic.finder.sdk.traffic.request.RequestExecutor;

public class GetBusStationLinesRequestTest extends ApplicationTestCase<FinderApplication> {
	private static final String TAG = "GetBusStationLinesRequestTest";
	private FinderApplication mApp = null;
	private ITrafficService mTrafficService = null;
	private ArrayList<KXStationLines> mExpectedData = new ArrayList<KXStationLines>();
	
	public GetBusStationLinesRequestTest(Class<FinderApplication> applicationClass) {
		super(applicationClass);
	}

	public GetBusStationLinesRequestTest() {
		super(FinderApplication.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		createApplication();
		mApp = getApplication();
		mTrafficService = mApp.getTrafficService();
		KXStationLines stationLines1 = new KXStationLines("新会展中心公交站","50022", "298,84,102,115,118".split(","));
		KXStationLines stationLines2 = new KXStationLines("新会展中心公交站","50023", "118,298,115,102,84".split(","));
		mExpectedData.add(stationLines1);
		mExpectedData.add(stationLines2);
	}

	public void test_retrieveStationLines1() {
		
		GetBusStationLinesRequest request = new GetBusStationLinesRequest("新会展中心公交站");
		RequestExecutor.execute(request, new RequestCallback() {
			@Override
			public void onSuccess(Object result) {
				ArrayList<KXStationLines> stationList = (ArrayList<KXStationLines>)result;
				assertNotNull(stationList);
				assertEquals(2, stationList.size());
				KXStationLines firstStation = stationList.get(0);
				assertNotNull(firstStation);
				assertEquals("50022", firstStation.getGpsNumber());
				assertEquals("50023", stationList.get(1).getGpsNumber());
			}
			
			@Override
			public void onError(int errorCode, String errorMessage) {
				Log.d(TAG, "get bus station lines failed: " + errorCode + ", caused by " + errorMessage);
				assertTrue(errorCode == 0);
			}
		});
	}
	
	public void test_retrieveStationLines2() {
		
		GetBusStationLinesRequest request = new GetBusStationLinesRequest("新会展中心公交站123");
		RequestExecutor.execute(request, new RequestCallback() {
			@Override
			public void onSuccess(Object result) {
				assertFalse(true);
			}
			
			@Override
			public void onError(int errorCode, String errorMessage) {
				Log.d(TAG, "get bus station lines failed: " + errorCode + ", caused by " + errorMessage);
				assertEquals(IErrorCode.ERROR_STATION_NAME_NOT_PRESENT, errorCode);
			}
		});
	}
}
