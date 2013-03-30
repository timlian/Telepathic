package com.telepathic.finder.sdk.traffic.entity.kuaixin;

public class KXBusStation {
	private String mLineNumber;
	private String mName;
	private String mGpsNumber;
	private String mDirection;
	
	public KXBusStation(String lineNumber, String name, String gpsNumber, String direction) {
		mLineNumber = lineNumber;
		mName = name;
		mGpsNumber = gpsNumber;
		mDirection = direction;
	}
	
	public KXBusStation() {
		
	}
	
	public void setLineNumber(String lineNumber) {
		mLineNumber = lineNumber;
	}
	
	public void setGpsNumber(String gpsNumber) {
		mGpsNumber = gpsNumber;
	}
	
	public void setName(String name) {
		mName = name;
	}
	
	public void setDirection(String direction) {
		mDirection = direction;
	}
	
	public String getName() {
		return mName;
	}
	
	public String getGpsNumber() {
		return mGpsNumber;
	}
	
	public String getDirection() {
		return mDirection;
	}
	
	public String getLineNumber() {
		return mLineNumber;
	}

}
