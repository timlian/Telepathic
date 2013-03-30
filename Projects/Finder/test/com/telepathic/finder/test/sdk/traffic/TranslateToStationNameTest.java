package com.telepathic.finder.test.sdk.traffic;

import android.test.ApplicationTestCase;
import android.util.Log;

import com.telepathic.finder.app.FinderApplication;
import com.telepathic.finder.sdk.ICompletionListener;
import com.telepathic.finder.sdk.IErrorCode;
import com.telepathic.finder.sdk.ITrafficService;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusStation;

public class TranslateToStationNameTest extends ApplicationTestCase<FinderApplication> {
	private static final String TAG = "TranslateToStationNameTest";
	private FinderApplication mApp = null;
	private ITrafficService mTrafficService = null;
	private volatile boolean mIsDone;
	private Object mLock = new Object();
	
	public TranslateToStationNameTest(Class<FinderApplication> applicationClass) {
		super(applicationClass);
	}

	public TranslateToStationNameTest() {
		super(FinderApplication.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		createApplication();
		mApp = getApplication();
		mTrafficService = mApp.getTrafficService();
	}
	
	public void test_translateToStationName1() {
		mTrafficService.translateToStationName("50022", new ICompletionListener() {
			@Override
			public void onSuccess(Object result) {
				KXBusStation station = (KXBusStation)result;
				assertNotNull(station);
				assertEquals("新会展中心公交站", station.getName());
				assertEquals("50022", station.getGpsNumber());
				assertNull(station.getLineNumber());
				assertNull(station.getDirection());
				notifyDone();
			}
			@Override
			public void onFailure(int errorCode, String errorText) {
				Log.d(TAG, "translate to station name failed, error = " + errorCode + ", caused by " + errorText);
				assertEquals(0, errorCode);
				notifyDone();
			}
		});
		waitResponse();
	}
	
	public void test_translateToStationName2() {
		mTrafficService.translateToStationName("80000", new ICompletionListener() {
			@Override
			public void onSuccess(Object result) {
				KXBusStation station = (KXBusStation)result;
				assertTrue(false);
				notifyDone();
			}
			@Override
			public void onFailure(int errorCode, String errorText) {
				Log.d(TAG, "translate to station name failed, error = " + errorCode + ", caused by " + errorText);
				assertEquals(IErrorCode.ERROR_GPS_NUMBER_NOT_PRESENT, errorCode);
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
