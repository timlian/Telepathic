package com.telepathic.finder.sdk.store;

import com.telepathic.finder.sdk.BusStation;

public class BusLineStation extends BusStation {
	private String mLineNumber;
	private String mDirection;
	
	public BusLineStation(String name, String gpsNumber, String lineNumber, String direction) {
		super(name, gpsNumber);
		mLineNumber = lineNumber;
		mDirection = direction;
	}
	
	public String getLineNumber() {
		return mLineNumber;
	}
	
	public String getDirection() {
		return mDirection;
	}
}
