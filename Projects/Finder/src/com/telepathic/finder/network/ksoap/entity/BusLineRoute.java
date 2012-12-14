package com.telepathic.finder.network.ksoap.entity;

import android.util.Log;
import com.telepathic.finder.network.ksoap.message.server.ServerMessage;
import com.telepathic.finder.network.ksoap.message.server.ServerMessage.MainPart;
import org.ksoap2.serialization.SoapObject;

public class BusLineRoute extends ServerMessage
{
  private Line[] mainParts;

  public Line[] getLines()
  {
    return this.mainParts;
  }

  // ERROR //
  public void processContent()
  {
    // Byte code:
    //   0: aload_0
    //   1: getfield 16 com/infinite/bus/ksoap/entity/BusLineRoute:content  Lorg/ksoap2/serialization/SoapObject;
    //   4: invokevirtual 29    org/ksoap2/serialization/SoapObject:getPropertyCount    ()I
    //   7: istore_1
    //   8: new 31  java/util/ArrayList
    //   11: dup
    //   12: invokespecial 32   java/util/ArrayList:<init>  ()V
    //   15: astore_2
    //   16: iconst_0
    //   17: istore_3
    //   18: iconst_0
    //   19: istore 4
    //   21: iload 4
    //   23: iload_1
    //   24: if_icmplt +34 -> 58
    //   27: aload_2
    //   28: invokevirtual 36   java/util/ArrayList:iterator    ()Ljava/util/Iterator;
    //   31: astore 17
    //   33: aload_0
    //   34: aload_2
    //   35: invokevirtual 39   java/util/ArrayList:size    ()I
    //   38: anewarray 41   com/infinite/bus/ksoap/entity/BusLineRoute$Line
    //   41: putfield 20    com/infinite/bus/ksoap/entity/BusLineRoute:mainParts    [Lcom/infinite/bus/ksoap/entity/BusLineRoute$Line;
    //   44: iconst_0
    //   45: istore 18
    //   47: aload 17
    //   49: invokeinterface 47 1 0
    //   54: ifne +194 -> 248
    //   57: return
    //   58: aload_0
    //   59: invokevirtual 53   java/lang/Object:getClass   ()Ljava/lang/Class;
    //   62: pop
    //   63: new 41 com/infinite/bus/ksoap/entity/BusLineRoute$Line
    //   66: dup
    //   67: aload_0
    //   68: invokespecial 56   com/infinite/bus/ksoap/entity/BusLineRoute$Line:<init>  (Lcom/infinite/bus/ksoap/entity/BusLineRoute;)V
    //   71: astore 6
    //   73: aload_0
    //   74: getfield 16    com/infinite/bus/ksoap/entity/BusLineRoute:content  Lorg/ksoap2/serialization/SoapObject;
    //   77: iload 4
    //   79: invokevirtual 60   org/ksoap2/serialization/SoapObject:getProperty (I)Ljava/lang/Object;
    //   82: checkcast 25   org/ksoap2/serialization/SoapObject
    //   85: astore 7
    //   87: aload 7
    //   89: invokevirtual 29   org/ksoap2/serialization/SoapObject:getPropertyCount    ()I
    //   92: istore 8
    //   94: ldc 62
    //   96: aload 7
    //   98: invokevirtual 66   org/ksoap2/serialization/SoapObject:toString    ()Ljava/lang/String;
    //   101: invokestatic 72   android/util/Log:v  (Ljava/lang/String;Ljava/lang/String;)I
    //   104: pop
    //   105: iconst_0
    //   106: istore 10
    //   108: aconst_null
    //   109: astore 11
    //   111: iload 10
    //   113: iload 8
    //   115: if_icmplt +20 -> 135
    //   118: iload_3
    //   119: ifeq +10 -> 129
    //   122: aload_2
    //   123: aload 6
    //   125: invokevirtual 76  java/util/ArrayList:add (Ljava/lang/Object;)Z
    //   128: pop
    //   129: iinc 4 1
    //   132: goto -111 -> 21
    //   135: aload 7
    //   137: iload 10
    //   139: invokevirtual 60  org/ksoap2/serialization/SoapObject:getProperty (I)Ljava/lang/Object;
    //   142: checkcast 78  org/ksoap2/serialization/SoapPrimitive
    //   145: astore 15
    //   147: new 80    org/ksoap2/serialization/PropertyInfo
    //   150: dup
    //   151: invokespecial 81  org/ksoap2/serialization/PropertyInfo:<init>    ()V
    //   154: astore 13
    //   156: aload 7
    //   158: iload 10
    //   160: aload 13
    //   162: invokevirtual 85  org/ksoap2/serialization/SoapObject:getPropertyInfo (ILorg/ksoap2/serialization/PropertyInfo;)V
    //   165: aload 13
    //   167: getfield 89   org/ksoap2/serialization/PropertyInfo:name  Ljava/lang/String;
    //   170: ifnull +46 -> 216
    //   173: aload 13
    //   175: getfield 89   org/ksoap2/serialization/PropertyInfo:name  Ljava/lang/String;
    //   178: ldc 91
    //   180: invokevirtual 96  java/lang/String:equals (Ljava/lang/Object;)Z
    //   183: ifne +33 -> 216
    //   186: aload 13
    //   188: getfield 89   org/ksoap2/serialization/PropertyInfo:name  Ljava/lang/String;
    //   191: ldc 98
    //   193: invokevirtual 96  java/lang/String:equals (Ljava/lang/Object;)Z
    //   196: ifne +20 -> 216
    //   199: aload 6
    //   201: aload 13
    //   203: getfield 89   org/ksoap2/serialization/PropertyInfo:name  Ljava/lang/String;
    //   206: aload 15
    //   208: invokevirtual 99  org/ksoap2/serialization/SoapPrimitive:toString ()Ljava/lang/String;
    //   211: invokevirtual 103 com/infinite/bus/ksoap/entity/BusLineRoute$Line:setContent  (Ljava/lang/String;Ljava/lang/String;)V
    //   214: iconst_1
    //   215: istore_3
    //   216: iinc 10 1
    //   219: aload 13
    //   221: astore 11
    //   223: goto -112 -> 111
    //   226: astore 12
    //   228: aload 11
    //   230: astore 13
    //   232: aload 12
    //   234: invokevirtual 106 java/lang/Exception:printStackTrace ()V
    //   237: ldc 62
    //   239: ldc 108
    //   241: invokestatic 72   android/util/Log:v  (Ljava/lang/String;Ljava/lang/String;)I
    //   244: pop
    //   245: goto -29 -> 216
    //   248: aload_0
    //   249: getfield 20   com/infinite/bus/ksoap/entity/BusLineRoute:mainParts    [Lcom/infinite/bus/ksoap/entity/BusLineRoute$Line;
    //   252: iload 18
    //   254: aload 17
    //   256: invokeinterface 112 1 0
    //   261: checkcast 41  com/infinite/bus/ksoap/entity/BusLineRoute$Line
    //   264: aastore
    //   265: iinc 18 1
    //   268: goto -221 -> 47
    //   271: astore 12
    //   273: goto -41 -> 232
    //
    // Exception table:
    //   from   to  target  type
    //   135    156 226 java/lang/Exception
    //   156    214 271 java/lang/Exception
  }

  public class Line extends ServerMessage.MainPart
  {
    private Station[] stations;

    public Line()
    {
      super();
    }

    public String getCloseOffTime()
    {
      SoapObject localSoapObject = (SoapObject)BusLineRoute.this.content.getProperty(0);
      try
      {
        String str1 = null;
        String str2 = localSoapObject.getProperty("closeOffTime").toString();
        str1 = str2;
        return str1;
      }
      catch (Exception localException)
      {
          return null;
      }
    }

    public String getDepartureTime()
    {
      SoapObject localSoapObject = (SoapObject)BusLineRoute.this.content.getProperty(0);
      try
      {
        String str1 = null;
        String str2 = localSoapObject.getProperty("departureTime").toString();
        str1 = str2;
        return str1;
      }
      catch (Exception localException)
      {
        return null;
      }
    }

    public String getLineName()
    {
      try
      {
        String str1 = null;
        String str2 = getContent("lineName");
        str1 = str2;
        return str1;
      }
      catch (Exception localException)
      {
        return null;
          
      }
    }

    public String[] getStationAliases()
    {
      return getContent("stationAliases").split(",");
    }

    public String[] getStationGPS()
    {
      String str = getContent("stationGPS");
      if (str != null);
      for (String[] arrayOfString = str.split(","); ; arrayOfString = new String[0])
        return arrayOfString;
    }

    public String[] getStationNames()
    {
      String str = getContent("stations");
      Log.v("BusLineRoute", "BusLineRoute_tempStations:" + str);
      if ((str != null) && (str.length() != 0));
      for (String[] arrayOfString = str.split(","); ; arrayOfString = new String[0])
        return arrayOfString;
    }

    public Station[] getStations()
    {
      if (this.stations == null)
        this.stations = new Station[getStationNames().length];
      for (int i = 0; ; i++)
      {
        if (i >= this.stations.length)
          return this.stations;
        this.stations[i] = new Station();
        this.stations[i].setAlias(getStationAliases()[i]);
        this.stations[i].setLineName(getLineName());
        this.stations[i].setName(getStationNames()[i]);
      }
    }

    public String getType()
    {
      try
      {
        String str1 = null;
        String str2 = getContent("type");
        str1 = str2;
        return str1;
      }
      catch (Exception localException)
      {
        return null;
      }
    }

    public boolean isCircle()
    {
        // Tim: it's not clear now
//      int i = 1;
//      if (BusLineRoute.this.getLines().length > i)
//        if ((BusLineRoute.this.getLines()[i].getStations().length != 0) && (BusLineRoute.this.getLines()[i].getStations() != null))
//          break label59;
//      while (true)
//      {
//        return i;
//        if (BusLineRoute.this.getLines().length != i)
//          label59: int j = 0;
//      }
        return false;
    }
  }
}
