package com.telepathic.finder.sdk.traffic.entity.kuaixin;

public class KXProgramStep {
	/**
	 * The start station name
	 */
	private String mSource;
	/**
	 * The destination station name
	 */
	private String mDestination;
	/**
	 * The line number
	 */
	private String mLineName;

	public KXProgramStep(String source, String destination, String lineName) {
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
