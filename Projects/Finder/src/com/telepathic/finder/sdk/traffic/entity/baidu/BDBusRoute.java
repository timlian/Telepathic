package com.telepathic.finder.sdk.traffic.entity.baidu;

public class BDBusRoute {
	/**
	 * The route uid
	 */
	private String mUid;
	/**
	 * The route name
	 */
	private String mName;
	/**
	 * The city
	 */
	private String mCity;
	
	public BDBusRoute(String uid, String name, String city) {
		mUid = uid;
		mName = name;
		mCity = city;
	}
	
	public String getUid() {
		return mUid;
	}
	
	public String getName() {
		return mName;
	}
	
	public String getCity() {
		return mCity;
	}
	
}
