package com.telepathic.finder.network.ksoap.entity;

import java.util.Date;

public class Station
{
  private String alias;
  private Date date;
  private int index = -1;
  private String lineName;
  private String name;
  private String stationGPS;
  private String type;

  public Station()
  {
  }

  public Station(String paramString)
  {
    this.name = paramString;
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
        Station localStation = (Station)paramObject;
        if (this.name == null)
        {
          if (localStation.name != null)
            bool = false;
        }
        else if (!this.name.equals(localStation.name))
          bool = false;
      }
    }
    return bool;
  }

  public String getAlias()
  {
    return this.alias;
  }

  public Date getDate()
  {
    return this.date;
  }

  public int getIndex()
  {
    return this.index;
  }

  public String getLineName()
  {
    return this.lineName;
  }

  public String getName()
  {
    return this.name;
  }

  public String getStationGPS()
  {
    return this.stationGPS;
  }

  public String getType()
  {
    return this.type;
  }

  public int hashCode()
  {
    if (this.name == null);
    for (int i = 0; ; i = this.name.hashCode())
      return i + 31;
  }

  public void setAlias(String paramString)
  {
    this.alias = paramString;
  }

  public void setDate(Date paramDate)
  {
    this.date = paramDate;
  }

  public void setIndex(int paramInt)
  {
    this.index = paramInt;
  }

  public void setLineName(String paramString)
  {
    this.lineName = paramString;
  }

  public void setName(String paramString)
  {
    this.name = paramString;
  }

  public void setStationGPS(String paramString)
  {
    this.stationGPS = paramString;
  }

  public void setType(String paramString)
  {
    this.type = paramString;
  }

  public String toString()
  {
    return "车站名称:" + this.name;
  }
}
