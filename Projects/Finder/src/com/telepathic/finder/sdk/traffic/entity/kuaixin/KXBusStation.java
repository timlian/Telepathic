package com.telepathic.finder.sdk.traffic.entity.kuaixin;

public class KXBusStation {
	private String mName;
	private String mGpsNumber;
	
	public KXBusStation(String name, String gpsNumber) {
		mName = name;
		mGpsNumber = gpsNumber;
	}
	
	public KXBusStation() {
		
	}
	
	public void setGpsNumber(String gpsNumber) {
		mGpsNumber = gpsNumber;
	}
	
	public void setName(String name) {
		mName = name;
	}
	
	public String getName() {
		return mName;
	}
	
	public String getGpsNumber() {
		return mGpsNumber;
	}
	
}
