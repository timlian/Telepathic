package com.telepathic.finder.sdk.traffic.network;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.telepathic.finder.util.Utils;

public class NetworkManager {
    private static final String TAG = "NetWorkAdapter";

    private static final String TRAFFIC_SERVICE_URI = "http://client.10628106.com:4800/TrafficService.asmx";

    private static int CONNECTION_TIME_OUT = 1000 * 30;

    private ConcurrentLinkedQueue<RPCBaseRequest> mRequestQueue;

    private HandlerThread mThread;
    private Handler mRequestHandler;

    private Runnable mHanRunnable = new Runnable() {
        @Override
        public void run() {
            RPCBaseRequest request = mRequestQueue.poll();
            if (request != null) {
                send(request);
            }
            mRequestHandler.postDelayed(this, 500);
        }
    };

    public NetworkManager() {
        mRequestQueue  = new ConcurrentLinkedQueue<RPCBaseRequest>();

        mThread = new HandlerThread("Request Handler Thread");
        mThread.start();
        mRequestHandler = new Handler(mThread.getLooper());
        mRequestHandler.post(mHanRunnable);
    }

    public void execute(final RPCBaseRequest request) {
        if (request != null) {
            mRequestQueue.add(request);
        }
    }

    public void cancel(){
        mThread.interrupt();
    }

    private void send(final RPCBaseRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is NULL!");
        }
        SoapObject rpcMessage = request.getSoapMessage();
        Utils.debug(TAG, "Sent Request: " + rpcMessage.toString());
        SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(110);
        soapEnvelope.bodyOut = rpcMessage;
        soapEnvelope.dotNet  = true;
        soapEnvelope.setOutputSoapObject(rpcMessage);
        HttpTransportSE localHttpTransportSE = new HttpTransportSE(TRAFFIC_SERVICE_URI, CONNECTION_TIME_OUT);
        String errorMessage = null;
        try {
            localHttpTransportSE.call(request.getSoapAction(), soapEnvelope);
            if (soapEnvelope.bodyIn != null) {
            	Utils.debug(TAG, "Received Response: " + soapEnvelope.bodyIn.toString());
            }
        } catch (Exception e) {
            errorMessage = e.getLocalizedMessage();
            if (errorMessage == null) {
                errorMessage = e.toString();
            }
            Log.e(TAG, "send() - " + errorMessage);

        }
        onRequestComplete(request, soapEnvelope.bodyIn, errorMessage);
    }

    private void onRequestComplete(RPCBaseRequest request, Object response, String error) {
        request.onResponse(response, error);
        if (!request.isComplete()) {
            execute(request);
        }
    }

}
