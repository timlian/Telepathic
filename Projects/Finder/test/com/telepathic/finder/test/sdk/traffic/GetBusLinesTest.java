package com.telepathic.finder.test.sdk.traffic;

import com.telepathic.finder.app.FinderApplication;
import com.telepathic.finder.sdk.ICompletionListener;
import com.telepathic.finder.sdk.ITrafficService;
import com.telepathic.finder.util.Utils;

import android.test.ApplicationTestCase;
import android.util.Log;

public class GetBusLinesTest extends ApplicationTestCase<FinderApplication> {
	private static final String TAG = "GetBusLinesTest";
	
	private FinderApplication mApp = null;
	private ITrafficService mTrafficService = null;
	private volatile boolean mIsDone;
	private Object mLock = new Object();
	
	public GetBusLinesTest(Class<FinderApplication> applicationClass) {
		super(applicationClass);
	}

	public GetBusLinesTest() {
		super(FinderApplication.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		createApplication();
		mApp = getApplication();
		mTrafficService = mApp.getTrafficService();
	}

	public void test_retrieve_bus_Lines() {
		mTrafficService.getBusStationLines("新会展中心公交站", new ICompletionListener() {
			
			@Override
			public void onSuccess(Object result) {
				Utils.copyAppDatabaseFiles(mApp.getPackageName());
				notifyDone();
			}
			
			@Override
			public void onFailure(int errorCode, String errorText) {
				Utils.copyAppDatabaseFiles(mApp.getPackageName());
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
