package com.telepathic.finder.sdk.traffic;

import java.util.ArrayList;

import com.baidu.mapapi.MKPoiInfo;
import com.baidu.mapapi.MKStep;
import com.telepathic.finder.sdk.ITrafficMonitor;
import com.telepathic.finder.sdk.ITrafficListeners.BusLineListener;
import com.telepathic.finder.sdk.ITrafficListeners.BusLocationListener;
import com.telepathic.finder.sdk.ITrafficListeners.BusRouteListener;
import com.telepathic.finder.sdk.ITrafficListeners.ConsumerRecordsListener;
import com.telepathic.finder.sdk.ITrafficListeners.ErrorListener;
import com.telepathic.finder.sdk.traffic.entity.BusCard;
import com.telepathic.finder.sdk.traffic.entity.BusRoute;

public class TrafficeMonitor implements ITrafficMonitor {

	private ArrayList<ConsumerRecordsListener> mConsumerRecordsListeners;
	private ArrayList<BusLocationListener> mBusLocationListeners;
	private ArrayList<BusLineListener> mBusLineListeners;
	private ArrayList<BusRouteListener> mBusRouteListeners;
	private ArrayList<ErrorListener> mErrorListeners;
	
	TrafficeMonitor() {
		mConsumerRecordsListeners = new ArrayList<ConsumerRecordsListener>();
		mBusLocationListeners = new ArrayList<BusLocationListener>();
		mBusLineListeners = new ArrayList<BusLineListener>();
		mBusRouteListeners = new ArrayList<BusRouteListener>();
		mErrorListeners = new ArrayList<ErrorListener>();
	}
	
	@Override
	public void add(ConsumerRecordsListener listener) {
		if (!mConsumerRecordsListeners.contains(listener)) {
			mConsumerRecordsListeners.add(listener);
		}
	}
	
	@Override
	public void remove(ConsumerRecordsListener listener) {
		mConsumerRecordsListeners.remove(listener);
	}
	
	public void setUpdate(BusCard busCard) {
		for(ConsumerRecordsListener listener : mConsumerRecordsListeners) {
			listener.onReceived(busCard);
		}
	}
	
	@Override
	public void add(ErrorListener listener) {
		if (!mErrorListeners.contains(listener)) {
			mErrorListeners.add(listener);
		}
	}
	
	@Override
	public void remove(ErrorListener listener) {
		mErrorListeners.remove(listener);
	}
	
	public void setError(String error) {
		for(ErrorListener listener : mErrorListeners) {
			listener.done(error);
		}
	}

	@Override
	public void add(BusLocationListener listener) {
		if (!mBusLocationListeners.contains(listener)) {
			mBusLocationListeners.add(listener);
		}
		
	}

	@Override
	public void remove(BusLocationListener listener) {
		mBusLocationListeners.remove(listener);
	}

	public void setUpdate(MKStep station) {
		for(BusLocationListener listener : mBusLocationListeners) {
			listener.onReceived(station);
		}
	}

	@Override
	public void add(BusLineListener listener) {
		if (!mBusLineListeners.contains(listener)) {
			mBusLineListeners.add(listener);
		}
	}

	@Override
	public void remove(BusLineListener listener) {
		mBusLineListeners.remove(listener);
	}
	
	public void setUpdate(String busLineNumber, ArrayList<MKPoiInfo> busPois) {
		for(BusLineListener listener : mBusLineListeners) {
			listener.onReceived(busLineNumber, busPois);
		}
	}

	@Override
	public void add(BusRouteListener listener) {
		if (!mBusRouteListeners.contains(listener)) {
			mBusRouteListeners.add(listener);
		}
	}

	@Override
	public void remove(BusRouteListener listener) {
		mBusRouteListeners.remove(listener);
	}
	
	public void setUpdate(BusRoute route) {
		for(BusRouteListener listener : mBusRouteListeners) {
			listener.onReceived(route);
		}
	}
	
}
