package com.telepathic.finder.test.sdk.traffic;

import android.test.ApplicationTestCase;
import android.util.Log;

import com.telepathic.finder.app.FinderApplication;
import com.telepathic.finder.sdk.ICompletionListener;
import com.telepathic.finder.sdk.IErrorCode;
import com.telepathic.finder.sdk.ITrafficService;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusLine;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusLine.Direction;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusRoute;

public class GetBusLineTest extends ApplicationTestCase<FinderApplication> {
    private static final String TAG = "GetBusLineTest";

    private FinderApplication mApp = null;
    private ITrafficService mTrafficService = null;
    private KXBusLine mExpectedBusLine;
    private volatile boolean mIsDone;
    private Object mLock = new Object();

    public GetBusLineTest(Class<FinderApplication> applicationClass) {
        super(applicationClass);
    }

    public GetBusLineTest() {
        super(FinderApplication.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();
        mApp = getApplication();
        mTrafficService = mApp.getTrafficService();
        mExpectedBusLine = new KXBusLine("102");
        KXBusRoute upRoute = new KXBusRoute();
        upRoute.setDirection(Direction.UP);
        upRoute.setStartTime("06:30");
        upRoute.setEndTime("22:00");
        String upStations = "胜利村站,新南门汽车站,新南路站,磨子桥站,磨子村站,章灵寺站,科华中路站,桂溪公交站,科华中路南站,科华南路北站,天仁路站,天仁路西站,天府长城站,名都公园站,天府大道北段站,成都出口加工区站,新会展中心北侧站,新会展北侧世纪城路口站,新会展中心东侧站,新会展中心南侧站,天华路站,天府软件园东侧站,新会展中心公交站";
        upRoute.setStations(upStations.split(","));
        KXBusRoute downRoute = new KXBusRoute();
        downRoute.setDirection(Direction.DOWN);
        downRoute.setStartTime("06:30");
        downRoute.setEndTime("22:00");
        String downStations = "新会展中心公交站,天府软件园东侧站,天华路站,新会展中心南侧站,新会展中心东侧站,新会展北侧世纪城路口站,科华南路锦悦东路站,科华南路锦城大道口站,污水处理厂站,科华南路府城大道口站,濯锦北路站,科华南路北站,科华中路南站,桂溪公交站,科华路二环路口站,章灵寺站,磨子村站,旅游村街站,胜利村站";
        downRoute.setStations(downStations.split(","));
        mExpectedBusLine.addRoute(upRoute);
        mExpectedBusLine.addRoute(downRoute);
    }

    public void test_getBusLine1() {
        mTrafficService.getBusLine("102", new ICompletionListener() {
            @Override
            public void onSuccess(Object result) {
                KXBusLine busLine = (KXBusLine)result;
                assertNotNull(busLine);
                assertEquals("102", busLine.getLineNumber());
                KXBusRoute upRoute = busLine.getRoute(Direction.UP);
                KXBusRoute expectedUpRoute = mExpectedBusLine.getRoute(Direction.UP);
                assertNotNull(upRoute);
                assertEquals(expectedUpRoute.getDirection(), upRoute.getDirection());
                //				assertEquals(expectedUpRoute.getStartTime(), upRoute.getStartTime());
                //				assertEquals(expectedUpRoute.getEndTime(), upRoute.getEndTime());
                assertEquals(expectedUpRoute.getStationNames(), upRoute.getStationNames());
                KXBusRoute downRoute = busLine.getRoute(Direction.DOWN);
                KXBusRoute expectedDownRoute = mExpectedBusLine.getRoute(Direction.DOWN);
                assertNotNull(downRoute);
                assertEquals(expectedDownRoute.getDirection(), downRoute.getDirection());
                //                assertEquals(expectedDownRoute.getStartTime(), downRoute.getStartTime());
                //                assertEquals(expectedDownRoute.getEndTime(), downRoute.getEndTime());
                assertEquals(expectedDownRoute.getStationNames(), downRoute.getStationNames());
                notifyDone();
            }

            @Override
            public void onFailure(int errorCode, String errorText) {
                Log.d(TAG, "Get bus line failed, error = " + errorCode + ", caused by " + errorText);
                assertEquals(0, errorCode);
                notifyDone();
            }
        });
        waitResponse();
    }

    public void test_getBusLine2() {
        mTrafficService.getBusLine("000", new ICompletionListener() {
            @Override
            public void onSuccess(Object result) {
                KXBusLine busLine = (KXBusLine)result;
                assertTrue(false);
                notifyDone();
            }

            @Override
            public void onFailure(int errorCode, String errorText) {
                Log.d(TAG, "Get bus line failed, error = " + errorCode + ", caused by " + errorText);
                assertEquals(IErrorCode.ERROR_LINE_NUMBER_NOT_PRESENT, errorCode);
                notifyDone();
            }
        });
        waitResponse();
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
