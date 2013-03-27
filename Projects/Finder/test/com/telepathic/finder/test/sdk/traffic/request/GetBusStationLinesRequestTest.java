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

	private static final String[] EXPECTED_GPS_NUMBERS = {
		"50022",
		"50023"
		};
	
	private static final String[][] EXPECTED_LINES = {
		{"298", "84", "102", "115", "118"},
		{"118", "298", "115", "102", "84"}
		};
	
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
	}

	public void test_retrieveStationLines1() {
		
		GetBusStationLinesRequest request = new GetBusStationLinesRequest("新会展中心公交站");
		RequestExecutor.execute(request, new RequestCallback() {
			@Override
			public void onSuccess(Object result) {
				ArrayList<KXStationLines> stationList = (ArrayList<KXStationLines>)result;
				assertNotNull(stationList);
				assertEquals(2, stationList.size());
				for(int i = 0; i < stationList.size(); i++) {
					assertEquals(EXPECTED_GPS_NUMBERS[i], stationList.get(i).getGpsNumber());
					String[] lines = stationList.get(i).getLines();
					assertEquals(EXPECTED_LINES[i].length, lines.length);
					for (int j = 0; j < lines.length; j++) {
						assertEquals(EXPECTED_LINES[i][j], lines[j]);
					}
				}
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
