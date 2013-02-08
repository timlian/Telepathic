package com.telepathic.finder.sdk;

import java.util.ArrayList;

import com.baidu.mapapi.MKPoiInfo;
import com.baidu.mapapi.MKStep;
import com.telepathic.finder.sdk.traffic.entity.BusCard;
import com.telepathic.finder.sdk.traffic.entity.BusRoute;

public interface ITrafficListeners {

	interface ConsumerRecordsListener {

		public void onReceived(BusCard info);

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
