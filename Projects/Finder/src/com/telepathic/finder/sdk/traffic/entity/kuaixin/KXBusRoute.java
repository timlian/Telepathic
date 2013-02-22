package com.telepathic.finder.sdk.traffic.entity.kuaixin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.R.interpolator;

import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusLine.Direction;

public class KXBusRoute {
    /**
     * The bus start time
     */
    private String mStartTime;
    /**
     * The bus end time
     */
    private String mEndTime;
    /**
     * The route direction
     */
    private Direction mDirection;
    /**
     * The route stations
     */
    private List<String> mStations = new ArrayList<String>();
    
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
    
    public Direction getDirection() {
    	return mDirection;
    }
    
    public void setDirection(Direction direction) {
    	mDirection = direction;
    }
    
    public List<String> getStations() {
    	return Collections.unmodifiableList(mStations);
    }
    
    public String getStationNames() {
    	StringBuilder builder = new StringBuilder();
    	for(String name : mStations) {
    		builder.append(name);
    		builder.append(",");
    	}
    	builder.deleteCharAt(builder.lastIndexOf(","));
    	return builder.toString();
    }
    
    public void setStations(String[] stations) {
    	mStations = Arrays.asList(stations);
    }
    
    public int getStationIndex(String stationName) {
    	int index = -1;
    	for(int idx = 0; idx < mStations.size(); idx++) {
    		if (mStations.get(idx).equals(stationName)) {
    			index = idx;
    			break;
    		}
    	}
    	return index;
    }
    
}
