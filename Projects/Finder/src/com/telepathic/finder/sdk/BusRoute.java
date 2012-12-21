package com.telepathic.finder.sdk;

import com.baidu.mapapi.MKRoute;

public class BusRoute {
    private MKRoute mRoute;
    private String mLineNumber;
    
    public BusRoute(String number, MKRoute route) {
        mRoute = route;
        mLineNumber = number;
    }
    
    public MKRoute getRoute() {
        return mRoute;
    }
    
    public String getLineNumber() {
        return mLineNumber;
    }
    
    public int getStationCount() {
        return mRoute.getNumSteps();
    }
    public String getFirstStation() {
        String stationName = mRoute.getStep(0).getContent();
        return handle(stationName);
    }
    
    public String getLastStation() {
        int last = getStationCount() - 1;
        String stationName = mRoute.getStep(last).getContent();
        return handle(stationName);
    }
    
    private String handle(String name) {
        char last = name.charAt(name.length() - 1);
        if (last == 'վ') {
            return name;
        } else {
            return name.concat("վ");
        }
    }
}
