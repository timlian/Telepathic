package com.telepathic.finder.sdk.traffic.entity;

public class BusStation {
	/**
	 * The name of the bus station
	 */
	private String mName;
	/**
	 * The gps number of the bus station
	 */
	private String mGpsNumber;
	/**
	 * The latitude of the bus station's position
	 */
	private String mLatitude;
	/**
	 * The longitude of the bus station's position
	 */
	private String mLongitude;

	public BusStation() {
		
	}
	
	public BusStation(String name) {
		mName = name;
	}
	
	public String getName() {
		return mName;
	}
	
	public void setName(String name) {
		mName = name;
	}
	
	public String getGpsNumber() {
		return mGpsNumber;
	}

	public void setGpsNumber(String gpsNumber) {
		mGpsNumber = gpsNumber;
	}
	
	public String getLatitude() {
		return mLatitude;
	}
	
	public void setLatitude(String latitude) {
		mLatitude = latitude;
	}
	
	public String getLongitude() {
		return mLongitude;
	}
	
	public void setLongitude(String longitude) {
		mLongitude = longitude;
	}
	
}
