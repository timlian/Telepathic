package com.telepathic.finder.sdk.traffic.entity;

public class KXBusRoute {
    /**
     * The bus start time
     */
    private String mStartTime;
    /**
     * The bus end time
     */
    private String mEndTime;
    
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
}
