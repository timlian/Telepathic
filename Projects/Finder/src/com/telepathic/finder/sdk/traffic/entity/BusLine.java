package com.telepathic.finder.sdk.traffic.entity;

import java.util.ArrayList;
import java.util.HashMap;

public class BusLine {
    /**
     * The bus number
     */
    private String mLineNumber;
    /**
     * The bus start time
     */
    private String mStartTime;
    /**
     * The bus end time
     */
    private String mEndTime;
    /**
     * The routes
     */
    private HashMap<Direction, ArrayList<BusStation>> mRouteMap = new HashMap<Direction, ArrayList<BusStation>>();
    
    public enum Direction {
        UP("上行"),
        DOWN("下行"),
        CIRCLE("环行");
        private final String mLabel;
        
        private Direction(String label) {
        	mLabel = label;
		}
        
        public static Direction fromString(String label) {
        	for(Direction direction : Direction.values()) {
        		if (direction.mLabel.equals(label)) {
        			return direction;
        		}
        	}
        	return null;
        }
        
        @Override
        public String toString() {
        	return mLabel;
        }
    }

    public String getLineNumber() {
        return mLineNumber;
    }
    
    public void setLineNumber(String lineNumber) {
    	mLineNumber = lineNumber;
    }

    public String getStartTime() {
        return mStartTime;
    }
    
    public void setStartTime(String startTime) {
    	mStartTime = startTime;
    }

    public String getEndTime() {
        return mEndTime;
    }
    
    public void setEndTime(String endTime) {
    	mEndTime = endTime;
    }

    public void addRoute(Direction direction, ArrayList<BusStation> route) {
    	mRouteMap.put(direction, route);
    }
    
    public ArrayList<BusStation> getRoute(Direction direction) {
    	return mRouteMap.get(direction);
    }
    
    public String getRouteStations(Direction direction) {
    	ArrayList<BusStation> route = mRouteMap.get(direction);
    	StringBuilder builder = new StringBuilder();
    	if (route != null) {
    		final int len = route.size();
    		for(int i = 0; i < len; i++) {
    			builder.append(route.get(i).getName());
    			builder.append(",");
    		}
    		builder.deleteCharAt(builder.lastIndexOf(","));
    	}
    	return builder.toString();
    }
    
    public String getFirstStation(Direction direction) {
    	String firstStation = "";
    	ArrayList<BusStation> route = mRouteMap.get(direction);
    	if (route != null && route.size() > 0) {
    		firstStation = route.get(0).getName();
    	} 
    	return firstStation;
    }
    
    public String getLastStation(Direction direction) {
    	String lastStation = "";
    	ArrayList<BusStation> route = mRouteMap.get(direction);
    	if (route != null && route.size() > 0) {
    		lastStation = route.get(route.size() -1).getName();
    	} 
    	return lastStation;
    }
    
    public HashMap<Direction, ArrayList<BusStation>> getRouteMap() {
    	return mRouteMap;
    }
    
    public int getStationIndex(Direction direction, String stationName) {
    	int index = -1;
    	ArrayList<BusStation> route = mRouteMap.get(direction);
    	if (route != null && route.size() > 0) {
    		BusStation station = null;
    		for(int idx = 0; idx < route.size(); idx++) {
    			station = route.get(idx);
    			if (station.getName().equals(stationName)) {
    				index = idx;
    				break;
    			}
    		}
    	} 
    	return index;
    }
}
