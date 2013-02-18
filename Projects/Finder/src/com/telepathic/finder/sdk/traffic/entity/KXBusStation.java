package com.telepathic.finder.sdk.traffic.entity;

public class KXBusStation {
    /**
     * The name of the bus station
     */
    private String mName;
    /**
     * The gps number of the bus station
     */
    private String mGpsNumber;
    
    public String getName() {
        return mName;
    }
    
    public void setName(String name) {
        mName = name;
    }
    
    public String getGpsNumber() {
        return mGpsNumber;
    }

    public void setGpsNumber(String gpsNumber) {
        mGpsNumber = gpsNumber;
    }
}
