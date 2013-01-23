package com.telepathic.finder.sdk;

import com.telepathic.finder.sdk.ITrafficListeners.BusLineListener;
import com.telepathic.finder.sdk.ITrafficListeners.BusLocationListener;
import com.telepathic.finder.sdk.ITrafficListeners.BusRouteListener;
import com.telepathic.finder.sdk.ITrafficListeners.ConsumerRecordsListener;
import com.telepathic.finder.sdk.ITrafficListeners.ErrorListener;

public interface ITrafficMonitor {

	public void add(ConsumerRecordsListener listener);
	
	public void remove(ConsumerRecordsListener listener);
	
	public void add(BusLocationListener listener);
	
	public void remove(BusLocationListener listener);
	
	public void add(BusLineListener listener);
	
	public void remove(BusLineListener listener);
	
	public void add(BusRouteListener listener);
	
	public void remove(BusRouteListener listener);
	
	public void add(ErrorListener listener) ;
	
	public void remove(ErrorListener listener) ;

}
