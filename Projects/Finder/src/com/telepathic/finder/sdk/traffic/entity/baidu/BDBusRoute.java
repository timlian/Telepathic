package com.telepathic.finder.sdk.traffic.entity.baidu;

public class BDBusRoute {
    /**
     * The route uid
     */
    private String mUid;
    /**
     * The first station
     */
    private String mFirstStation;
    /**
     * The last station
     */
    private String mLastStation;
    /**
     * The city
     */
    private String mCity;

    public void setCity(String city) {
        mCity = city;
    }

    public void setUid(String uid) {
        mUid = uid;
    }

    public void setFirstStation(String firstStation) {
        mFirstStation = firstStation;
    }

    public void setLastStation(String lastStation) {
        mLastStation = lastStation;
    }

    public String getUid() {
        return mUid;
    }

    public String getFirstStation() {
        return mFirstStation;
    }

    public String getLastStation() {
        return mLastStation;
    }

    public String getCity() {
        return mCity;
    }

}
