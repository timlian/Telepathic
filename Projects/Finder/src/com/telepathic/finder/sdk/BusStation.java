package com.telepathic.finder.sdk;

import java.util.Date;

public class BusStation {
    private String mAlias;
    private Date mDate;
    private int index = -1;
    private String mLineName;
    private String mName;
    private String mStationGPS;
    private String mType;

    public BusStation() {}

    public BusStation(String paramString) {
        mName = paramString;
    }

    public boolean equals(Object paramObject)
    {
      boolean bool = true;
      if (this == paramObject) {
          return bool;
      }
      else
      {
        if (paramObject == null)
        {
          bool = false;
        }
        else if (getClass() != paramObject.getClass())
        {
          bool = false;
        }
        else
        {
          BusStation localStation = (BusStation)paramObject;
          if (mName == null)
          {
            if (localStation.mName != null)
              bool = false;
          }
          else if (!mName.equals(localStation.mName))
            bool = false;
        }
      }
      return bool;
    }

    public String getAlias() {
        return mAlias;
    }

    public Date getDate() {
        return mDate;
    }

    public int getIndex() {
        return index;
    }

    public String getLineName() {
        return mLineName;
    }

    public String getName() {
        return mName;
    }

    public String getStationGPS() {
        return mStationGPS;
    }

    public String getType() {
        return mType;
    }

    public int hashCode() {
        if (mName == null)
            ;
        for (int i = 0;; i = mName.hashCode())
            return i + 31;
    }

    public void setAlias(String paramString) {
        mAlias = paramString;
    }

    public void setDate(Date paramDate) {
        mDate = paramDate;
    }

    public void setIndex(int paramInt) {
        index = paramInt;
    }

    public void setLineName(String paramString) {
        mLineName = paramString;
    }

    public void setName(String paramString) {
        mName = paramString;
    }

    public void setStationGPS(String paramString) {
        mStationGPS = paramString;
    }

    public void setType(String paramString) {
        mType = paramString;
    }

    public String toString() {
        return "车站名称:" + mName;
    }
}
