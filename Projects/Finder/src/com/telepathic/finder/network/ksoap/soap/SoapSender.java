package com.telepathic.finder.network.ksoap.soap;

import android.util.Log;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

public final class SoapSender {
  private int CONNECTION_TIME_OUT = 30000;
  private String connectionAddr;
  private String funname;
  private String namespace;
  private SoapObject rpc;

  public SoapSender(String paramString1, String paramString2, String paramString3)
  {
    this.funname = paramString2;
    setConnectionProperites(paramString1, paramString3);
    this.rpc = new SoapObject(this.namespace, this.funname);
  }

  public void addProperty(String paramString1, String paramString2)
  {
    this.rpc.addProperty(paramString1, paramString2);
  }

  public SoapObject getSoapObject()
  {
    return this.rpc;
  }

  public Object send() throws IOException, XmlPullParserException {
    SoapSerializationEnvelope localSoapSerializationEnvelope = new SoapSerializationEnvelope(110);
    localSoapSerializationEnvelope.bodyOut = this.rpc;
    Log.i("SoapSender", this.rpc.toString());
    localSoapSerializationEnvelope.dotNet = true;
    localSoapSerializationEnvelope.setOutputSoapObject(this.rpc);
    Log.v("SoapSender", "--SoapSender--send()");
    HttpTransportSE localHttpTransportSE = new HttpTransportSE(this.connectionAddr, this.CONNECTION_TIME_OUT);
    Log.v("SoapSender", "--SoapSender--send()--ht:" + localHttpTransportSE + ",envelope:" + localSoapSerializationEnvelope);
    Log.v("SoapSender", "--SoapSender--send()--namespace:" + this.namespace + this.funname);
    localHttpTransportSE.call(this.namespace + this.funname, localSoapSerializationEnvelope);
    return localSoapSerializationEnvelope.bodyIn;
  }

  public void setConnectionProperites(String paramString1, String paramString2) {
    this.namespace = paramString1;
    this.connectionAddr = paramString2;
  }

  public void setParemeters(Hashtable<String, String> paramHashtable) {
    Enumeration localEnumeration = paramHashtable.keys();
    while(localEnumeration.hasMoreElements()) {
        String str1 = (String)localEnumeration.nextElement();
        String str2 = (String)paramHashtable.get(str1);
        addProperty(str1, str2);
    }
  }
}
