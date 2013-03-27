package com.telepathic.finder.test.sdk.traffic.request;

import android.test.ApplicationTestCase;
import android.util.Log;

import com.telepathic.finder.app.FinderApplication;
import com.telepathic.finder.sdk.traffic.request.GetBusStationRequest;
import com.telepathic.finder.sdk.traffic.request.GetBusStationRequest.Station;
import com.telepathic.finder.sdk.traffic.request.RequestCallback;
import com.telepathic.finder.sdk.traffic.request.RequestExecutor;

public class GetStationRequestTest extends ApplicationTestCase<FinderApplication>{
	private static final String TAG = "GetStationRequestTest";
	
	private Station mExpectedStaiton1;
	private Station mExpectedStaiton2;
	
	public GetStationRequestTest(Class<FinderApplication> applicationClass) {
		super(applicationClass);
	}

	public GetStationRequestTest() {
		super(FinderApplication.class);
	}

	@Override
	protected void setUp() throws Exception {
		mExpectedStaiton1 = new Station("102", "新会展中心公交站", "50023", "上行");
		mExpectedStaiton2 = new Station(null, "新会展中心公交站", "50023", null);
		super.setUp();
	}
	
	public void test_retrieveStation1() {
		GetBusStationRequest request = new GetBusStationRequest("102", "50023");
		RequestExecutor.execute(request, new RequestCallback() {
			@Override
			public void onSuccess(Object result) {
				Station station = (Station)result;
				assertNotNull(station);
				assertEquals(mExpectedStaiton1.getLineNumber(), station.getLineNumber());
				assertEquals(mExpectedStaiton1.getGpsNumber(), station.getGpsNumber());
				assertEquals(mExpectedStaiton1.getName(), station.getName());
				assertEquals(mExpectedStaiton1.getDirection(), station.getDirection());
			}
			
			@Override
			public void onError(int errorCode, String errorMessage) {
				Log.d(TAG, "Retrieve station name failed: error = " + errorCode + ", caused by " + errorMessage);
				assertTrue(false);
			}
		});
	}
	
	public void test_retrieveStation2() {
		GetBusStationRequest request = new GetBusStationRequest(null, "50023");
		RequestExecutor.execute(request, new RequestCallback() {
			@Override
			public void onSuccess(Object result) {
				Station station = (Station)result;
				assertEquals(mExpectedStaiton2.getLineNumber(), station.getLineNumber());
				assertEquals(mExpectedStaiton2.getGpsNumber(), station.getGpsNumber());
				assertEquals(mExpectedStaiton2.getName(), station.getName());
				assertEquals(mExpectedStaiton2.getDirection(), station.getDirection());
			}
			
			@Override
			public void onError(int errorCode, String errorMessage) {
				Log.d(TAG, "Retrieve station name failed: error = " + errorCode + ", caused by " + errorMessage);
				assertTrue(false);
			}
		});
	}
}
