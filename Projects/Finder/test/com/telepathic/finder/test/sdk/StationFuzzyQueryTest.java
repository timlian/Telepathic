package com.telepathic.finder.test.sdk;

import java.util.ArrayList;

import android.test.ApplicationTestCase;
import android.util.Log;

import com.telepathic.finder.app.FinderApplication;
import com.telepathic.finder.sdk.ICompletionListener;
import com.telepathic.finder.sdk.ITrafficService;

public class StationFuzzyQueryTest extends ApplicationTestCase<FinderApplication> {
    private static final String TAG = "StationFuzzyQueryTest";

    private FinderApplication mApp = null;
    private ITrafficService mTrafficService = null;
    private volatile boolean mIsDone;
    private Object mLock = new Object();

    public StationFuzzyQueryTest(Class<FinderApplication> applicationClass) {
        super(applicationClass);
    }

    public StationFuzzyQueryTest() {
        super(FinderApplication.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();
        mApp = getApplication();
        mTrafficService = mApp.getTrafficService();
    }

    public void test_retrieve_station_name() {
        mTrafficService.queryStationName("新会展", new ICompletionListener() {
            @Override
            public void onSuccess(Object result) {
                ArrayList<String> stationNames = (ArrayList<String>) result;
                assertEquals(5, stationNames.size());
                notifyDone();
            }

            @Override
            public void onFailure(int errorCode, String errorText) {
                Log.d(TAG, "onFailure: errorCode = " + errorCode + ", errorText: " + errorText);
                notifyDone();
            }
        });
        waitResponse();
    }

    public void test_retrieve_station_name_single() {
        mTrafficService.queryStationName("西门", new ICompletionListener() {
            @Override
            public void onSuccess(Object result) {
                ArrayList<String> stationNames = (ArrayList<String>) result;
                assertEquals(1, stationNames.size());
                notifyDone();
            }

            @Override
            public void onFailure(int errorCode, String errorText) {
                Log.d(TAG, "onFailure: errorCode = " + errorCode + ", errorText: " + errorText);
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
