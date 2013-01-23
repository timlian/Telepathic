package com.telepathic.finder.sdk.traffic;


public class BusStation {
	private String mName;
	private String mGpsNumber;

	public BusStation(String name, String gpsNumber) {
		mName = name;
		mGpsNumber = gpsNumber;
	}
	
	public String getName() {
		return mName;
	}
	
	public String getGpsNumber() {
		return mGpsNumber;
	}

}
