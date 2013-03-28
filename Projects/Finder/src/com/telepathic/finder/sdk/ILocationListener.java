package com.telepathic.finder.sdk;

public interface ILocationListener {
	/**
	 * Called when there is a location update.
	 * 
	 * @param distance
	 */
	void onUpdate(int distance);
	/**
	 * 
	 */
	void done();

}
