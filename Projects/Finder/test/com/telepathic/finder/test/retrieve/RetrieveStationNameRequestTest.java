package com.telepathic.finder.test.retrieve;

import com.telepathic.finder.app.FinderApplication;
import com.telepathic.finder.sdk.TrafficService;

import android.test.ApplicationTestCase;

public class RetrieveStationNameRequestTest extends ApplicationTestCase<FinderApplication> {
	private FinderApplication mApp = null;
	private TrafficService mTrafficService = null;
	    
	public RetrieveStationNameRequestTest(Class<FinderApplication> applicationClass) {
		super(applicationClass);
	}

	public RetrieveStationNameRequestTest() {
		super(FinderApplication.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		createApplication();
		mApp = getApplication();
		mTrafficService = TrafficService.getTrafficService(mApp.getMapManager(), getApplication());
	}
	 
	public void test_retrieve_station_name() {
		//Response: {stationName=新会展中心公交站; alias=奇诺咖啡餐厅; direction=下行; code=200; msg=成功; }
		mTrafficService.retrieveStationName("102", "50022");
		//Response: {stationName=新会展中心公交站; alias=奇诺咖啡餐厅; direction=上行; code=200; msg=成功; }
		mTrafficService.retrieveStationName("102", "50023");
		try {
			Thread.sleep(50000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
