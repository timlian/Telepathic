package com.telepathic.finder.sdk;

import java.util.ArrayList;

import com.baidu.mapapi.MKPoiInfo;
import com.baidu.mapapi.MKStep;

public abstract interface TrafficListener {

    public abstract interface BusLineListener {
        /**
         *
         * @param busPois
         */
       public void done(String busLineNumber, ArrayList<MKPoiInfo> busPois, int error);

    }

    public abstract interface BusRouteListener {
        /**
         *
         * @param busRoute
         */
       public void done(BusRoute route, int error);
    }

    public abstract interface BusLocationListener {
        /**
         *
         * @param busLocation
         */
        public void onLocationUpdated(MKStep busLocation);

        /**
         *
         * @param errorMessage
         */
        public void onError(String errorMessage);

    }
}
