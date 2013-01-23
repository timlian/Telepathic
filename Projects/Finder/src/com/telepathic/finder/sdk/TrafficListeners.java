package com.telepathic.finder.sdk;

import java.util.ArrayList;

import com.baidu.mapapi.MKPoiInfo;
import com.baidu.mapapi.MKStep;

public interface TrafficListeners {

	interface ConsumerRecordsListener {

		public void onReceived(ConsumptionInfo info);

	}

	interface BusLocationListener {

		public void onReceived(MKStep station);

	}

	interface ErrorListener {

		public void done(String error);

	}

	interface BusLineListener {

		public void onReceived(String busLineNumber, ArrayList<MKPoiInfo> busPois);

	}

	interface BusRouteListener {

		public void onReceived(BusRoute route);
	}

}
