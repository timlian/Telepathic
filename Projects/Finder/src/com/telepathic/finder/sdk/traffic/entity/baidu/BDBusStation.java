package com.telepathic.finder.sdk.traffic.entity.baidu;

public class BDBusStation {
    /**
     * The name of the bus station
     */
    private String mName;
    /**
     * The latitude of the bus station's position
     */
    private String mLatitude;
    /**
     * The longitude of the bus station's position
     */
    private String mLongitude;

    public BDBusStation(String name, String latitude, String longitude) {
        mName = name;
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public String getName() {
        return mName;
    }

    public String getLatitude() {
        return mLatitude;
    }

    public String getLongitude() {
        return mLongitude;
    }
}
