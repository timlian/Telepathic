package com.telepathic.finder.sdk.traffic.request;

import java.io.IOException;

import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import com.telepathic.finder.sdk.traffic.provider.ITrafficData;
import com.telepathic.finder.util.Utils;

public class RequestExecutor {
    private static final String TAG = RequestExecutor.class.getSimpleName();
    private static final String TRAFFIC_SERVICE_URI = "http://client.10628106.com:4800/TrafficService.asmx";

    private static final int CONNECITON_TIME_OUT[] = {5, 10, 20};
    private static final int MAX_RETRY_COUNT = CONNECITON_TIME_OUT.length;

    public static void execute(RPCBaseRequest request, RequestCallback callback) {
        request.setCallback(callback);
        Object response = null;
        for (int count = 0; count < MAX_RETRY_COUNT; count++) {
            try {
                response = sendRequest(request, count);
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

    // performance test version
    public static void execute(Context context, RPCBaseRequest request, RequestCallback callback) {
        ContentResolver resolver = context.getContentResolver();
        request.setCallback(callback);
        Object response = null;
        for (int count = 0; count < MAX_RETRY_COUNT; count++) {
            ContentValues values = new ContentValues();
            values.put(ITrafficData.KuaiXinData.NetworkPerformance.REQUEST_NAME, request.getName());
            values.put(ITrafficData.KuaiXinData.NetworkPerformance.REQUEST_ID, request.getId());
            values.put(ITrafficData.KuaiXinData.NetworkPerformance.RETRY, count);
            long startTime = 0;
            long endTime = 0;
            long interval = 0;
            try {
                startTime = System.currentTimeMillis();
                response = sendRequest(request, count);
                endTime = System.currentTimeMillis();
                interval = endTime - startTime;
                values.put(ITrafficData.KuaiXinData.NetworkPerformance.STATUS, "success");
                values.put(ITrafficData.KuaiXinData.NetworkPerformance.TIME, interval);
                resolver.insert(ITrafficData.KuaiXinData.NetworkPerformance.CONTENT_URI, values);
                break;
            } catch (Exception e) {
                endTime = System.currentTimeMillis();
                interval = endTime - startTime;
                values.put(ITrafficData.KuaiXinData.NetworkPerformance.STATUS, "failed");
                values.put(ITrafficData.KuaiXinData.NetworkPerformance.ERROR, e.getClass().getName());
                values.put(ITrafficData.KuaiXinData.NetworkPerformance.TIME, interval);
                resolver.insert(ITrafficData.KuaiXinData.NetworkPerformance.CONTENT_URI, values);
            }
        }
        if (response != null) {
            Utils.debug(TAG,"Received Response: " + response.toString());
            request.onResponse(response);
        } else {
            Utils.debug(TAG, "Send request:" + request + " failed.");
        }
    }

    private static Object sendRequest(RPCBaseRequest request, int retryCount) throws IOException, XmlPullParserException {
        SoapObject rpcMessage = request.getSoapMessage();
        Utils.debug(TAG, "Sent Request: " + rpcMessage.toString());
        SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(110);
        soapEnvelope.bodyOut = rpcMessage;
        soapEnvelope.dotNet  = true;
        soapEnvelope.setOutputSoapObject(rpcMessage);
        HttpTransportSE localHttpTransportSE = new HttpTransportSE(TRAFFIC_SERVICE_URI, CONNECITON_TIME_OUT[retryCount]);
        localHttpTransportSE.call(request.getSoapAction(), soapEnvelope);
        if (soapEnvelope.bodyIn == null) {
            throw new RuntimeException("Response is NULL.");
        }
        return soapEnvelope.bodyIn;
    }

}
