package com.telepathic.finder.sdk.network;

import java.io.IOException;

import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import com.telepathic.finder.util.ClientLog;

import android.util.Log;

public class SoapMessageSender {
    private static final String TAG = "SoapMessageSender";

    private static final String HOST_ADDRESS = "http://client.10628106.com:4800/TrafficService.asmx";

    private static int CONNECTION_TIME_OUT = 1000 * 30;

    private NetWorkAdapter mNetWorkAdapter;

    public SoapMessageSender(NetWorkAdapter netWorkAdapter) {
        mNetWorkAdapter = netWorkAdapter;
    }

    public void sendMessage(RPCRequest request) throws IOException, XmlPullParserException {
        if (request == null) {
            throw new IllegalArgumentException("SoapMessageSender.sendMessage: request parameter is NULL!");
        }
        SoapObject rpcMessage = request.getSoapMessage();
        if (ClientLog.DEBUG) {
            Log.d(TAG, "Sent Request: " + rpcMessage.toString());
        }
        SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(110);
        soapEnvelope.bodyOut = rpcMessage;
        soapEnvelope.dotNet  = true;
        soapEnvelope.setOutputSoapObject(rpcMessage);
        HttpTransportSE localHttpTransportSE = new HttpTransportSE(HOST_ADDRESS, CONNECTION_TIME_OUT);
        localHttpTransportSE.call(request.getSoapAction(), soapEnvelope);
        if (ClientLog.DEBUG) {
            Log.d(TAG, "Received Response: " + soapEnvelope.bodyIn.toString());
        }
        mNetWorkAdapter.onRequestComplete(soapEnvelope.bodyIn);
      }
}
