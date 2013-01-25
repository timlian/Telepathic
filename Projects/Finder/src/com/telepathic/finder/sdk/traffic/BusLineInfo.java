package com.telepathic.finder.sdk.traffic;

import java.util.ArrayList;
import java.util.HashMap;

public class BusLineInfo {
    /**
     * The class of bus line infomation
     * @author Iverson
     *
     */

    public enum Direction {
        UP,DOWN,CIRCLE
    }

    /**
     * The bus number
     */
    private String mBusNumber;

    /**
     * The station information of the bus line
     */
    private HashMap<Direction,ArrayList<String>> mBusStations;

    /**
     * The bus start time
     */
    private String mStartTime;

    /**
     * The bus end time
     */
    private String mEndTime;

    public BusLineInfo(String busNumber, String startTime, String endTime, HashMap<Direction,ArrayList<String>> busStations){
        mBusNumber = busNumber;
        mStartTime = startTime;
        mEndTime = endTime;
        mBusStations = busStations;
    }

    public String getmBusNumber() {
        return mBusNumber;
    }

    public String getmStartTime() {
        return mStartTime;
    }

    public String getmEndTime() {
        return mEndTime;
    }

    /**
     * Get the bus stations by the appointed direction
     * @param direction
     * @return Bus stations list
     */
    public ArrayList<String> getBusStationsByDirection(Direction direction){
        ArrayList<String> busStationsOfDirection;
        busStationsOfDirection = mBusStations.get(direction);
        if (busStationsOfDirection != null) {
            return busStationsOfDirection;
        } else {
            throw new RuntimeException("The bus don't have this direction");
        }
    }
}
