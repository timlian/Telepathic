package com.telepathic.finder.network.ksoap.message.server;

import android.util.Log;
import java.io.PrintStream;
import java.util.Hashtable;
import org.ksoap2.serialization.SoapObject;

public abstract class ServerMessage
{
  private String code;
  public SoapObject content;
  private String msg;

  public String getCode()
  {
    return this.code;
  }

  public String getMsg()
  {
    return this.msg;
  }

  public abstract void processContent();

  public void setContent(SoapObject paramSoapObject)
  {
     //Log.d("SoapParse", paramSoapObject.toString());
    try
    {
      SoapObject resultObject = (SoapObject) paramSoapObject.getProperty("getBusLineRouteResult");
      if (resultObject != null) {
          resultObject = (SoapObject) resultObject.getProperty("diffgram");
          if (resultObject != null) {
              resultObject = (SoapObject) resultObject.getProperty("NewDataSet");
              if (resultObject != null) {
                  for(int i = 0; i < resultObject.getPropertyCount(); i++) {
                      SoapObject route = (SoapObject) resultObject.getProperty(i);
                      Log.d("SoapParse", route.toString());
                  }
              }
          }
      }
      SoapObject localSoapObject1 = (SoapObject)((SoapObject)((SoapObject)paramSoapObject.getProperty(0)).getProperty(1)).getProperty(0);
      this.content = localSoapObject1;
      SoapObject localSoapObject2 = (SoapObject)localSoapObject1.getProperty(0);
      this.code = localSoapObject2.getProperty("code").toString();
      this.msg = localSoapObject2.getProperty("msg").toString();
      if (!"200".equals(this.code.trim()))
      {
        System.err.println("code:" + this.code + ",msg:" + this.msg);
        throw new RuntimeException(this.msg);
      }
    }
    catch (Exception localException)
    {
      //while (true)
        System.err.println("服务器出错了,服务器返回的数据格式不正确:" + localException.getMessage());
      //processContent();
    }
  }

  public class MainPart
  {
    private Hashtable<String, String> list = new Hashtable();

    public MainPart()
    {
    }

        protected String getContent(String paramString) {
            String str = (String) this.list.get(paramString);
            if (str == null) {
                str = "";
            } else {
                if ("anyType{}".equals(str)) {
                    str = "";
                    Log.w("ServerMessage", "getContent equals anyType{}");
                }
            }
            return str;
        }

    public void setContent(String paramString1, String paramString2)
    {
      this.list.put(paramString1, paramString2);
    }
  }
}
