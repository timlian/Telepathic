package com.telepathic.finder.sdk.traffic.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ksoap2.serialization.SoapObject;

import android.util.Log;

import com.telepathic.finder.sdk.traffic.request.GetBusTransferRouteRequest.TransferProgram.ProgramStep;

public class GetBusTransferRouteRequest extends RPCBaseRequest {
	private static final String TAG = "GetBusTransferRouteRequest";
	private static final String METHOD_NAME = "getBusTransferRoute";
	private static final String KEY_STARTING = "starting";
	private static final String KEY_DESTINATION = "destination";
	//private static final String KEY_TOTAL_NUMBER = "totalNumber";
	private static final String KEY_PROGRAM = "program";
	private static final String KEY_TRANSFER_TIME = "TransferTime";
	private static final String KEY_LINE_NAME = "lineName";
	
	
	public static class TransferProgram {
		
		public static class ProgramStep {
			private String mSource;
			private String mDestination;
			private String mLineName;
			
			public ProgramStep(String source, String destination, String lineName) {
				mSource = source;
				mDestination = destination;
				mLineName = lineName;
			}
			
			public String getSource() {
				return mSource;
			}
			
			public String getDestination() {
				return mDestination;
			}
			
			public String getLineName() {
				return mLineName;
			}
		}
		
		private String mProgramId;
		private String mTransferTime;
		private List<ProgramStep> mSteps;
		
		public TransferProgram(String programId, String transferTime) {
			mProgramId = programId;
			mTransferTime = transferTime;
			mSteps = new ArrayList<ProgramStep>();
		}
		
		public String getProgramId() {
			return mProgramId;
		}
		
		public String getTransferTime() {
			return mTransferTime;
		}
		
		public List<ProgramStep> getSteps() {
			return Collections.unmodifiableList(mSteps);
		}
		
		public void addStep(ProgramStep step) {
			mSteps.add(step);
		}
	}
	public GetBusTransferRouteRequest(String source, String destination) {
		super(METHOD_NAME);
		addParameter(KEY_STARTING, source);
		addParameter(KEY_DESTINATION, destination);
	}
	
	@Override
	void handleError(int errorCode, String errorMessage) {
		if (mCallback != null) {
			mCallback.onError(errorCode, errorMessage);
		}
	}

	@Override
	void handleResponse(SoapObject dataSet) {
		List<TransferProgram> transferPrograms = new ArrayList<TransferProgram>();
		if (dataSet != null && dataSet.getPropertyCount() > 1) {
			SoapObject dataEntry = null;
			String preProgramId = null, curProgramId = null;
			boolean isNewProgram = false;
			TransferProgram transferProgram = null;
			for(int i = 0; i < dataSet.getPropertyCount(); i++) {
				dataEntry = (SoapObject)dataSet.getProperty(i);
				curProgramId = dataEntry.getPrimitivePropertyAsString(KEY_PROGRAM);
				if (preProgramId == null || !preProgramId.equals(curProgramId)) {
					isNewProgram = true;
				} else {
					isNewProgram = false;
				}
				if (isNewProgram) {
					String transferTime = dataEntry.getPrimitivePropertyAsString(KEY_TRANSFER_TIME);
					transferProgram = new TransferProgram(curProgramId, transferTime);
					transferPrograms.add(transferProgram);
				}
				String source = dataEntry.getPrimitivePropertyAsString(KEY_STARTING);
				String destination = dataEntry.getPrimitivePropertyAsString(KEY_DESTINATION);
				String lineName = dataEntry.getPrimitivePropertyAsString(KEY_LINE_NAME);
				ProgramStep programStep = new ProgramStep(source, destination, lineName);
				transferProgram.addStep(programStep);
				preProgramId = curProgramId;
			}
		}
		if (mCallback != null) {
			mCallback.onSuccess(transferPrograms);
		}
	}

}
