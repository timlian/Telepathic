package com.telepathic.finder.test.sdk.traffic.request;

import java.util.ArrayList;

import com.telepathic.finder.app.FinderApplication;
import com.telepathic.finder.sdk.traffic.request.GetBusStationRequest.Station;
import com.telepathic.finder.sdk.traffic.request.GetStationNameRequest;
import com.telepathic.finder.sdk.traffic.request.RequestCallback;
import com.telepathic.finder.sdk.traffic.request.RequestExecutor;

import android.test.ApplicationTestCase;
import android.util.Log;

public class GetStationNameRequestTest extends ApplicationTestCase<FinderApplication> {
	private static final String TAG = "GetStationNameRequestTest";
	
	private ArrayList<String> mExpectedStationNames = new ArrayList<String>();
	
	public GetStationNameRequestTest(Class<FinderApplication> applicationClass) {
		super(applicationClass);
	}

	public GetStationNameRequestTest() {
		super(FinderApplication.class);
	}

	@Override
	protected void setUp() throws Exception {
		mExpectedStationNames.add("新会展中心北侧站");
		mExpectedStationNames.add("新会展中心东侧站");
		mExpectedStationNames.add("新会展中心公交站");
		mExpectedStationNames.add("新会展中心南侧站");
		super.setUp();
	}
	
	public void test_retrieveStationNames() {
		GetStationNameRequest request = new GetStationNameRequest("新会展中心");
		RequestExecutor.execute(request, new RequestCallback() {
			
			@Override
			public void onSuccess(Object result) {
				ArrayList<String> stationNames = (ArrayList<String>)result;
				assertNotNull(stationNames);
				assertEquals(mExpectedStationNames.size(), stationNames.size());
				for(int i = 0; i < mExpectedStationNames.size(); i++) {
					assertEquals(mExpectedStationNames.get(i), stationNames.get(i));
				}
			}
			
			@Override
			public void onError(int errorCode, String errorMessage) {
				Log.d(TAG,"get station names failed: error = " + errorCode + ", caused by" + errorMessage);
				assertTrue(false);
			}
		});
	}

}
