package com.telepathic.finder.test.sdk.traffic.request;

import android.test.ApplicationTestCase;
import android.util.Log;

import com.telepathic.finder.app.FinderApplication;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusStation;
import com.telepathic.finder.sdk.traffic.request.GetBusStationRequest;
import com.telepathic.finder.sdk.traffic.request.RequestCallback;
import com.telepathic.finder.sdk.traffic.request.RequestExecutor;

public class GetStationRequestTest extends ApplicationTestCase<FinderApplication>{
	private static final String TAG = "GetStationRequestTest";
	
	private KXBusStation mExpectedStaiton;
	
	public GetStationRequestTest(Class<FinderApplication> applicationClass) {
		super(applicationClass);
	}

	public GetStationRequestTest() {
		super(FinderApplication.class);
	}

	@Override
	protected void setUp() throws Exception {
		mExpectedStaiton = new KXBusStation("新会展中心公交站", "50023");
		super.setUp();
	}
	
	public void test_retrieveStation1() {
		GetBusStationRequest request = new GetBusStationRequest("50023");
		RequestExecutor.execute(request, new RequestCallback() {
			@Override
			public void onSuccess(Object result) {
				KXBusStation station = (KXBusStation)result;
				assertNotNull(station);
				assertEquals(mExpectedStaiton.getGpsNumber(), station.getGpsNumber());
				assertEquals(mExpectedStaiton.getName(), station.getName());
			}
			
			@Override
			public void onError(int errorCode, String errorMessage) {
				Log.d(TAG, "Retrieve station name failed: error = " + errorCode + ", caused by " + errorMessage);
				assertTrue(false);
			}
		});
	}
}
