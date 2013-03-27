package com.telepathic.finder.sdk.traffic.entity.kuaixin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KXTransferProgram {
	/**
	 * The program id
	 */
	private String mProgramId;
	/**
	 * The total transfer count
	 */
	private String mTransferTime;
	/**
	 * The transfer steps
	 */
	private List<KXProgramStep> mSteps;

	public KXTransferProgram(String programId, String transferTime) {
		mProgramId = programId;
		mTransferTime = transferTime;
		mSteps = new ArrayList<KXProgramStep>();
	}

	public String getProgramId() {
		return mProgramId;
	}

	public String getTransferTime() {
		return mTransferTime;
	}

	public List<KXProgramStep> getSteps() {
		return Collections.unmodifiableList(mSteps);
	}

	public void addStep(KXProgramStep step) {
		mSteps.add(step);
	}

}
