package com.telepathic.finder.sdk.traffic.request;

import java.io.IOException;

import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import com.telepathic.finder.util.Utils;

public class RequestExecutor {
    private static final String TAG = RequestExecutor.class.getSimpleName();
    private static final String TRAFFIC_SERVICE_URI = "http://client.10628106.com:4800/TrafficService.asmx";
    
    private static final int CONNECTION_TIME_OUT = 1000 * 30;
    private static final int MAX_RETRY_COUNT = 5;

	public static void execute(RPCBaseRequest request, RequestCallback callback) {
		request.setCallback(callback);
		Object response = null;
		for (int count = 0; count < MAX_RETRY_COUNT; count++) {
			try {
				response = sendRequest(request);
				break;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (response != null) {
			Utils.debug(TAG,"Received Response: " + response.toString());
	    	request.onResponse(response);
		} else {
			Utils.debug(TAG, "Send request:" + request + " failed.");
		}
	}

    private static Object sendRequest(RPCBaseRequest request) throws IOException, XmlPullParserException {
        SoapObject rpcMessage = request.getSoapMessage();
        Utils.debug(TAG, "Sent Request: " + rpcMessage.toString());
        SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(110);
        soapEnvelope.bodyOut = rpcMessage;
        soapEnvelope.dotNet  = true;
        soapEnvelope.setOutputSoapObject(rpcMessage);
        HttpTransportSE localHttpTransportSE = new HttpTransportSE(TRAFFIC_SERVICE_URI, CONNECTION_TIME_OUT);
		localHttpTransportSE.call(request.getSoapAction(), soapEnvelope);
		if (soapEnvelope.bodyIn == null) {
			throw new RuntimeException("Response is NULL.");
		}
	    return soapEnvelope.bodyIn;
    }

}
