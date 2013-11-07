package com.telepathic.finder.test.sdk.traffic.request;

import java.util.ArrayList;

import android.test.ApplicationTestCase;
import android.util.Log;

import com.telepathic.finder.app.FinderApplication;
import com.telepathic.finder.sdk.IErrorCode;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXStationLines;
import com.telepathic.finder.sdk.traffic.request.GetBusStationLinesRequest;
import com.telepathic.finder.sdk.traffic.request.RequestCallback;
import com.telepathic.finder.sdk.traffic.request.RequestExecutor;

public class GetBusStationLinesRequestTest extends ApplicationTestCase<FinderApplication> {
    private static final String TAG = "GetBusStationLinesRequestTest";

    private ArrayList<KXStationLines> mExpectedStationLines = new ArrayList<KXStationLines>();

    public GetBusStationLinesRequestTest(Class<FinderApplication> applicationClass) {
        super(applicationClass);
    }

    public GetBusStationLinesRequestTest() {
        super(FinderApplication.class);
    }

    @Override
    protected void setUp() throws Exception {
        String[] lines1 = {"115", "102", "118", "298", "84"};
        String[] lines2 = {"118", "115", "102", "298", "84"};
        KXStationLines stationLines1 = new KXStationLines("新会展中心公交站", "50022", lines1);
        KXStationLines stationLines2 = new KXStationLines("新会展中心公交站", "50023", lines2);
        mExpectedStationLines.add(stationLines1);
        mExpectedStationLines.add(stationLines2);
        super.setUp();
    }

    public void test_retrieveStationLines1() {

        GetBusStationLinesRequest request = new GetBusStationLinesRequest("新会展中心公交站");
        RequestExecutor.execute(request, new RequestCallback() {
            @Override
            public void onSuccess(Object result) {
                ArrayList<KXStationLines> stationList = (ArrayList<KXStationLines>)result;
                assertNotNull(stationList);
                assertEquals(mExpectedStationLines.size(), stationList.size());
                for(int i = 0; i < mExpectedStationLines.size(); i++) {
                    assertEquals(mExpectedStationLines.get(i).getName(), stationList.get(i).getName());
                    assertEquals(mExpectedStationLines.get(i).getGpsNumber(), stationList.get(i).getGpsNumber());
                    String[] expectedLines = mExpectedStationLines.get(i).getLines();
                    String[] actualLines = stationList.get(i).getLines();
                    assertEquals(expectedLines.length, actualLines.length);
                    for(int j = 0; j < expectedLines.length; j++) {
                        assertEquals(expectedLines[j], actualLines[j]);
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
