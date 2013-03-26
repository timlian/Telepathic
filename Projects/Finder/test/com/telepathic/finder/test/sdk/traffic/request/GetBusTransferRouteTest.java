package com.telepathic.finder.test.sdk.traffic.request;

import java.util.ArrayList;

import android.test.ApplicationTestCase;
import android.util.Log;

import com.telepathic.finder.app.FinderApplication;
import com.telepathic.finder.sdk.ICompletionListener;
import com.telepathic.finder.sdk.ITrafficService;
import com.telepathic.finder.sdk.traffic.request.GetBusTransferRouteRequest.TransferProgram;
import com.telepathic.finder.sdk.traffic.request.GetBusTransferRouteRequest.TransferProgram.ProgramStep;

public class GetBusTransferRouteTest extends ApplicationTestCase<FinderApplication> {

	private static final String TAG = "GetBusTransferRouteTest";
	
	private FinderApplication mApp = null;
	private ITrafficService mTrafficService = null;
	private volatile boolean mIsDone;
	private Object mLock = new Object();
	
	public GetBusTransferRouteTest(Class<FinderApplication> applicationClass) {
		super(applicationClass);
	}

	public GetBusTransferRouteTest() {
		super(FinderApplication.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		createApplication();
		mApp = getApplication();
		mTrafficService = mApp.getTrafficService();
	}
	
	public void test_get_bus_transfer_route() {
		mTrafficService.getBusTransferRoute("新会展中心公交站", "新南门汽车站", new ICompletionListener() {
			
			@Override
			public void onSuccess(Object result) {
				ArrayList<TransferProgram> programs = (ArrayList<TransferProgram>)result;
				assertEquals(8, programs.size());
				Log.d(TAG, "方案总数： " + programs.size());
				for(TransferProgram program : programs) {
					Log.d(TAG, "方案 " + program.getProgramId() + " : ");
					for(ProgramStep step : program.getSteps()) {
						Log.d(TAG, "步骤： " + step.getSource() + ", " + step.getDestination() + ", " + step.getLineName());
					}
				}
				notifyDone();
			}
			
			@Override
			public void onFailure(int errorCode, String errorText) {
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
