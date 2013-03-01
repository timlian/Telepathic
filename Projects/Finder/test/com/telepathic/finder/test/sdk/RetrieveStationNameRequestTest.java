//package com.telepathic.finder.test.sdk;
//
//import android.test.ApplicationTestCase;
//import android.util.Log;
//
//import com.telepathic.finder.app.FinderApplication;
//import com.telepathic.finder.sdk.BusStation;
//import com.telepathic.finder.sdk.Event;
//import com.telepathic.finder.sdk.EventListener;
//import com.telepathic.finder.sdk.TrafficService;
//import com.telepathic.finder.sdk.store.BusLineStation;
//
//public class RetrieveStationNameRequestTest extends ApplicationTestCase<FinderApplication> {
//  private FinderApplication mApp = null;
//  private TrafficService mTrafficService = null;
//
//  public RetrieveStationNameRequestTest(Class<FinderApplication> applicationClass) {
//      super(applicationClass);
//  }
//
//  public RetrieveStationNameRequestTest() {
//      super(FinderApplication.class);
//  }
//
//  @Override
//  protected void setUp() throws Exception {
//      super.setUp();
//      createApplication();
//      mApp = getApplication();
//      mTrafficService = TrafficService.getTrafficService(mApp.getMapManager(), getApplication());
//  }
//
//  public void test_retrieve_station_name() {
//      //Response: {stationName=新会展中心公交站; alias=奇诺咖啡餐厅; direction=下行; code=200; msg=成功; }
//      //Response: {stationName=新会展中心公交站; alias=奇诺咖啡餐厅; direction=上行; code=200; msg=成功; }
//      TestEventListener listener = new TestEventListener();
//      mTrafficService.addEventListener(listener);
//      mTrafficService.retrieveStationName("102", "50022");
//      //mTrafficService.retrieveStationName("102", "50023");
//      while(!listener.isDone) {
//          try {
//              Thread.sleep(500);
//          } catch (InterruptedException e) {
//              e.printStackTrace();
//          }
//      }
//  }
//
//  private class TestEventListener extends EventListener {
//      private boolean isDone = false;
//
//      TestEventListener() {
//          super(Event.RECEIVED_BUS_STATION);
//      }
//
//      public boolean done() {
//          return isDone;
//      }
//
//      @Override
//      public void onEvent(Object result) {
//          BusLineStation station = (BusLineStation)result;
//          assertEquals("102", station.getLineNumber());
//          isDone = true;
//      }
//  }
//}
