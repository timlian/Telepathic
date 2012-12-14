package com.telepathic.finder.network.ksoap.message;

import java.util.Hashtable;

public abstract class AbstractMessage
{
  protected Hashtable<String, String> body = new Hashtable();

  protected void addContent(String paramString, int paramInt)
  {
    if (paramString == null) {
      System.err.println("参数名不能为null");
    }else {
      this.body.put(paramString, String.valueOf(paramInt));
    }
  }

  protected void addContent(String paramString1, String paramString2)
  {
    if (paramString1 == null) {
      throw new RuntimeException("参数名不能为null");
    }
    if (paramString2 == null) {
      System.err.println("请确定参数值是否需要为null,如果为null,则不会传发送此参数,如果值为空，请用\"\"!");
    } else {
      this.body.put(paramString1, paramString2);
    }
  }

  protected String getContent(String paramString)
  {
    return (String)this.body.get(paramString);
  }

  public Hashtable<String, String> getParameters()
  {
    return this.body;
  }
}
