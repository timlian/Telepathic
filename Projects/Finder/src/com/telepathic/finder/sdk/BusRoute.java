package com.telepathic.finder.sdk;


import com.baidu.mapapi.MKRoute;
import com.baidu.mapapi.MKStep;

public class BusRoute {
    private MKRoute mRoute;
    private String mLineNumber;
    private int mIndex;
    
    public BusRoute(String number, MKRoute route) {
        mRoute = route;
        mLineNumber = number;
        mIndex = getStationCount() - 1;
    }
    
    public int setIndex(int distance) {
        mIndex -= distance;
        return mIndex;
    }
    
    public boolean needContinue() {
        if (mIndex > 0) {
            return true;
        } else {
            return false;
        }
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
    
    public String getLastStation() {
        return getStationName(getStationCount() - 1);
    }
    
    public MKStep getStation(int distance) {
        int position = mIndex;
        if (distance > 0) {
            position -= distance;
        }
        if (position > 0) {
            return mRoute.getStep(position);
        } else {
            return null;
        }
    }
    
    public String getStationName(int index) {
        if (index < 0 || index >= getStationCount()) {
            return null;
        } 
        String stationName = mRoute.getStep(index).getContent();
        if (stationName != null && stationName.length() != 0) {
            if (stationName.charAt(stationName.length() - 1) != 'վ') {
                stationName += "վ";
            }
        }
        return stationName;
    }
}
