package com.telepathic.finder.sdk;

import java.util.ArrayList;

import com.baidu.mapapi.MKPoiInfo;
import com.baidu.mapapi.MKStep;
import com.telepathic.finder.sdk.TrafficListeners.BusLineListener;
import com.telepathic.finder.sdk.TrafficListeners.BusLocationListener;
import com.telepathic.finder.sdk.TrafficListeners.BusRouteListener;
import com.telepathic.finder.sdk.TrafficListeners.ConsumerRecordsListener;
import com.telepathic.finder.sdk.TrafficListeners.ErrorListener;

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
	
	public void setUpdate(ConsumptionInfo infoData) {
		for(ConsumerRecordsListener listener : mConsumerRecordsListeners) {
			listener.onReceived(infoData);
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