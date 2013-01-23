package com.telepathic.finder.sdk.traffic;


import com.baidu.mapapi.MKRoute;
import com.baidu.mapapi.MKStep;

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

    public String getLastStation() {
        return getStationName(getStationCount() - 1);
    }

    public MKStep getStation(int index) {
        if (index < 0 || index >= getStationCount()) {
            return null;
        }
        return mRoute.getStep(index);
    }

    public String getStationName(int index) {
        if (index < 0 || index >= getStationCount()) {
            return null;
        }
        String stationName = mRoute.getStep(index).getContent();
        if (stationName != null && stationName.length() != 0) {
            if (stationName.charAt(stationName.length() - 1) != '\u7AD9') {
                stationName += '\u7AD9';
            }
        }
        return stationName;
    }
}
