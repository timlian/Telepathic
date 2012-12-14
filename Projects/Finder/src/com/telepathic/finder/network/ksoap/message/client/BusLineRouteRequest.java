package com.telepathic.finder.network.ksoap.message.client;

import com.telepathic.finder.network.ksoap.entity.BusLineRoute;
import com.telepathic.finder.network.ksoap.message.ClientMessage;

public class BusLineRouteRequest extends ClientMessage
{
  private static final Class<BusLineRoute> ENTITY_CLASS = BusLineRoute.class;
  private static final String METHOD_NAME = "getBusLineRoute";

  public BusLineRouteRequest(String paramString)
  {
    addContent("busLine", paramString);
  }

  public Class<BusLineRoute> getEntityClass()
  {
    return ENTITY_CLASS;
  }

  public String getMethodName()
  {
    return "getBusLineRoute";
  }
}