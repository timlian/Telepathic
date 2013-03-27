package com.telepathic.finder.sdk.traffic.request;

import java.util.ArrayList;
import java.util.List;

import org.ksoap2.serialization.SoapObject;

import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXProgramStep;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXTransferProgram;

public class GetBusTransferRouteRequest extends RPCBaseRequest {
	private static final String METHOD_NAME = "getBusTransferRoute";
	private static final String KEY_STARTING = "starting";
	private static final String KEY_DESTINATION = "destination";
	//private static final String KEY_TOTAL_NUMBER = "totalNumber";
	private static final String KEY_PROGRAM = "program";
	private static final String KEY_TRANSFER_TIME = "TransferTime";
	private static final String KEY_LINE_NAME = "lineName";
	
	
	
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
		List<KXTransferProgram> transferPrograms = new ArrayList<KXTransferProgram>();
		if (dataSet != null && dataSet.getPropertyCount() > 1) {
			SoapObject dataEntry = null;
			String preProgramId = null, curProgramId = null;
			boolean isNewProgram = false;
			KXTransferProgram transferProgram = null;
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
					transferProgram = new KXTransferProgram(curProgramId, transferTime);
					transferPrograms.add(transferProgram);
				}
				String source = dataEntry.getPrimitivePropertyAsString(KEY_STARTING);
				String destination = dataEntry.getPrimitivePropertyAsString(KEY_DESTINATION);
				String lineName = dataEntry.getPrimitivePropertyAsString(KEY_LINE_NAME);
				KXProgramStep programStep = new KXProgramStep(source, destination, lineName);
				transferProgram.addStep(programStep);
				preProgramId = curProgramId;
			}
		}
		if (mCallback != null) {
			mCallback.onSuccess(transferPrograms);
		}
	}

}
