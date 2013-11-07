package com.telepathic.finder.test.sdk.traffic.request;

import android.test.ApplicationTestCase;
import android.util.Log;

import com.telepathic.finder.app.FinderApplication;
import com.telepathic.finder.sdk.IErrorCode;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusLine;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusLine.Direction;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusRoute;
import com.telepathic.finder.sdk.traffic.request.GetBusLineRequest;
import com.telepathic.finder.sdk.traffic.request.RequestCallback;
import com.telepathic.finder.sdk.traffic.request.RequestExecutor;

public class GetBusLineRequestTest extends ApplicationTestCase<FinderApplication>{
    private static final String TAG = "GetBusLineRequestTest";
    private static final String UP_STATIONS ="胜利村站,新南门汽车站,新南路站,磨子桥站,磨子村站,章灵寺站,科华中路站,桂溪公交站,科华中路南站,科华南路北站,天仁路站,天仁路西站,天府长城站,名都公园站,天府大道北段站,成都出口加工区站,新会展中心北侧站,新会展北侧世纪城路口站,新会展中心东侧站,新会展中心南侧站,天华路站,天府软件园东侧站,新会展中心公交站";
    private static final String DOWN_STATIONS = "新会展中心公交站,天府软件园东侧站,天华路站,新会展中心南侧站,新会展中心东侧站,新会展北侧世纪城路口站,科华南路锦悦东路站,科华南路锦城大道口站,污水处理厂站,科华南路府城大道口站,濯锦北路站,科华南路北站,科华中路南站,桂溪公交站,科华路二环路口站,章灵寺站,磨子村站,旅游村街站,胜利村站";
    private static final String CIRCLE_STATIONS = "火车北站公交站,人民北路二段北站,人民北路二段站,人民北路站,西北桥站,一环路九里堤路口西站,沙湾站,西门车站,抚琴小区站,青羊小区站,省医院站,青羊宫站,一环路大石路口南站,一环路菊乐路口站,高升桥站,一环路高升桥东路口站,一环路南四段站,衣冠庙站,九如村站,一环路南二段站,磨子桥站,红瓦寺站,九眼桥北站,牛王庙路口站,水碾河站,一环路玉双路口北站,一环路东三段站,一环路新鸿路口北站,一环路建设路口北站,一环路东一段站,一环路北四段站,梁家巷站,一环路北三段站,人民北路站,火车北站公交站";

    public GetBusLineRequestTest(Class<FinderApplication> applicationClass) {
        super(applicationClass);
    }

    public GetBusLineRequestTest() {
        super(FinderApplication.class);
    }

    public void test_retrieve_bus_line1() {
        GetBusLineRequest request = new GetBusLineRequest("102");
        RequestExecutor.execute(request, new RequestCallback() {
            @Override
            public void onSuccess(Object result) {
                KXBusLine busLine = (KXBusLine) result;
                assertTrue(busLine != null);
                assertEquals("102", busLine.getLineNumber());
                // verify the up route
                KXBusRoute upRoute = busLine.getRoute(Direction.UP);
                assertTrue(upRoute != null);
                //				assertEquals("06:30", upRoute.getStartTime());
                //				assertEquals("22:00", upRoute.getEndTime());
                assertEquals(UP_STATIONS, upRoute.getStationNames());
                assertEquals(Direction.UP, upRoute.getDirection());
                // verfify the down route
                KXBusRoute downRoute = busLine.getRoute(Direction.DOWN);
                assertTrue(downRoute != null);
                //                assertEquals("06:30", downRoute.getStartTime());
                //                assertEquals("22:00", downRoute.getEndTime());
                assertEquals(DOWN_STATIONS, downRoute.getStationNames());
                assertEquals(Direction.DOWN, downRoute.getDirection());

                assertTrue(busLine.getRoute(Direction.CIRCLE) == null);
            }

            @Override
            public void onError(int errorCode, String errorMessage) {
                Log.i(TAG, "Retrieve bus line faled: " + errorCode + " caused by " + errorMessage);
                assertEquals(0, errorCode);
            }
        });
    }

    public void test_retrieve_bus_line2() {
        GetBusLineRequest request = new GetBusLineRequest("27");
        RequestExecutor.execute(request, new RequestCallback() {
            @Override
            public void onSuccess(Object result) {
                KXBusLine busLine = (KXBusLine) result;
                assertTrue(busLine != null);
                assertEquals("27", busLine.getLineNumber());
                assertTrue(busLine.getRoute(Direction.UP) == null);
                assertTrue(busLine.getRoute(Direction.DOWN) == null);
                assertTrue(busLine.getRoute(Direction.CIRCLE) != null);
                assertEquals(1, busLine.getAllRoutes().size());
                KXBusRoute route = busLine.getRoute(Direction.CIRCLE);
                //				assertEquals("05:45", route.getStartTime());
                //				assertEquals("23:00", route.getEndTime());
                assertEquals(CIRCLE_STATIONS, route.getStationNames());
            }
            @Override
            public void onError(int errorCode, String errorMessage) {
                Log.i(TAG, "Retrieve bus line faled: " + errorCode + " caused by " + errorMessage);
                assertEquals(0, errorCode);
            }
        });
    }

    public void test_retrieve_bus_line3() {
        GetBusLineRequest request = new GetBusLineRequest("777777");
        RequestExecutor.execute(request, new RequestCallback() {
            @Override
            public void onSuccess(Object result) {
                assertFalse(true);
            }
            @Override
            public void onError(int errorCode, String errorMessage) {
                Log.i(TAG, "Retrieve bus line faled: " + errorCode + " caused by " + errorMessage);
                assertEquals(IErrorCode.ERROR_LINE_NUMBER_NOT_PRESENT, errorCode);
            }
        });
    }

}
