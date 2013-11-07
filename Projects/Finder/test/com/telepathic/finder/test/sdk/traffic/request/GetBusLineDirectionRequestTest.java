package com.telepathic.finder.test.sdk.traffic.request;

import android.test.ApplicationTestCase;
import android.util.Log;

import com.telepathic.finder.app.FinderApplication;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusLineDirection;
import com.telepathic.finder.sdk.traffic.request.GetBusLineDirectionRequest;
import com.telepathic.finder.sdk.traffic.request.RequestCallback;
import com.telepathic.finder.sdk.traffic.request.RequestExecutor;

public class GetBusLineDirectionRequestTest extends ApplicationTestCase<FinderApplication> {
    private static final String TAG = "GetBusLineDirectionRequestTest";

    public GetBusLineDirectionRequestTest(Class<FinderApplication> applicationClass) {
        super(applicationClass);
    }

    public GetBusLineDirectionRequestTest() {
        super(FinderApplication.class);
    }

    public void test_getBusLineDirection1() {
        GetBusLineDirectionRequest request = new GetBusLineDirectionRequest("102", "50022");
        RequestExecutor.execute(request, new RequestCallback() {
            @Override
            public void onSuccess(Object result) {
                KXBusLineDirection lineDirection = (KXBusLineDirection)result;
                assertNotNull(lineDirection);
                assertEquals("102", lineDirection.getLineNumber());
                assertEquals("上行", lineDirection.getDirection());
            }
            @Override
            public void onError(int errorCode, String errorMessage) {
                Log.d(TAG, "Get line direction failed, error = " + errorCode + ", caused by " + errorMessage);
                assertEquals(0, errorCode);
            }
        });
    }

    public void test_getBusLineDirection2() {
        GetBusLineDirectionRequest request = new GetBusLineDirectionRequest("102", "50023");
        RequestExecutor.execute(request, new RequestCallback() {
            @Override
            public void onSuccess(Object result) {
                KXBusLineDirection lineDirection = (KXBusLineDirection)result;
                assertNotNull(lineDirection);
                assertEquals("102", lineDirection.getLineNumber());
                assertEquals("下行", lineDirection.getDirection());
            }
            @Override
            public void onError(int errorCode, String errorMessage) {
                Log.d(TAG, "Get line direction failed, error = " + errorCode + ", caused by " + errorMessage);
                assertEquals(0, errorCode);
            }
        });
    }

    public void test_getBusLineDirection3() {
        GetBusLineDirectionRequest request = new GetBusLineDirectionRequest(null, "50023");
        RequestExecutor.execute(request, new RequestCallback() {
            @Override
            public void onSuccess(Object result) {
                KXBusLineDirection lineDirection = (KXBusLineDirection)result;
                assertNotNull(lineDirection);
                assertNull(lineDirection.getLineNumber());
                assertNull(lineDirection.getDirection());
            }
            @Override
            public void onError(int errorCode, String errorMessage) {
                Log.d(TAG, "Get line direction failed, error = " + errorCode + ", caused by " + errorMessage);
                assertEquals(0, errorCode);
            }
        });
    }

}
