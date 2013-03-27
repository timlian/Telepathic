package com.telepathic.finder.test.sdk.traffic.request;

import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.telepathic.finder.app.FinderApplication;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXProgramStep;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXTransferProgram;
import com.telepathic.finder.sdk.traffic.request.GetBusTransferRouteRequest;
import com.telepathic.finder.sdk.traffic.request.RequestCallback;
import com.telepathic.finder.sdk.traffic.request.RequestExecutor;

public class GetBusTransferRouteTest extends ApplicationTestCase<FinderApplication> {
	private static final String TAG = "GetBusTransferRouteTest";
	
	private List<KXTransferProgram> mExpectedTransferPrograms = new ArrayList<KXTransferProgram>();
	
	public GetBusTransferRouteTest(Class<FinderApplication> applicationClass) {
		super(applicationClass);
	}

	public GetBusTransferRouteTest() {
		super(FinderApplication.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		KXTransferProgram transferProgram = new KXTransferProgram("1", "1");
		KXProgramStep step1 = new KXProgramStep("新会展中心公交站", "胜利村站", "102");
		KXProgramStep step2 = new KXProgramStep("胜利村站", "新南门汽车站", "102");
		transferProgram.addStep(step1);
		transferProgram.addStep(step2);
		super.setUp();
	}
	
	public void test_get_bus_transfer_route() {
		GetBusTransferRouteRequest request = new GetBusTransferRouteRequest("新会展中心公交站", "新南门汽车站");
		RequestExecutor.execute(request, new RequestCallback() {
			@Override
			public void onSuccess(Object result) {
				ArrayList<KXTransferProgram> transferPrograms = (ArrayList<KXTransferProgram>)result;
				assertNotNull(transferPrograms);
				assertEquals(8, transferPrograms.size());
				for(int i = 0; i < mExpectedTransferPrograms.size(); i++) {
					KXTransferProgram transferProgram = mExpectedTransferPrograms.get(i);
					assertEquals(transferProgram.getProgramId(), transferPrograms.get(i).getProgramId());
					assertEquals(transferProgram.getTransferTime(), transferPrograms.get(i).getTransferTime());
					assertEquals(transferProgram.getSteps().size(), transferPrograms.get(i).getSteps().size());
					List<KXProgramStep> expectedSteps = transferProgram.getSteps();
					List<KXProgramStep> actualSteps = transferPrograms.get(i).getSteps();
					for(int j = 0 ; j < transferProgram.getSteps().size(); j++) {
						assertEquals(expectedSteps.get(j).getSource(), actualSteps.get(j).getSource());
						assertEquals(expectedSteps.get(j).getDestination(), actualSteps.get(j).getDestination());
						assertEquals(expectedSteps.get(j).getLineName(), actualSteps.get(j).getLineName());
					}
				}
			}
			
			@Override
			public void onError(int errorCode, String errorMessage) {
				Log.d(TAG, "Get bus transfer route failed: error = " + errorCode + ", caused by " + errorMessage);
				assertTrue(false);
			}
		});
		
	}
	
}
