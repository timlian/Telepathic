package com.telepathic.finder.sdk.network;

import java.util.concurrent.ConcurrentLinkedQueue;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import com.telepathic.finder.util.ClientLog;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

public class NetWorkAdapter {
    private static final String TAG = "NetWorkAdapter";
    
    private static final String TRAFFIC_SERVICE_URI = "http://client.10628106.com:4800/TrafficService.asmx";

    private static int CONNECTION_TIME_OUT = 1000 * 30;

    private ConcurrentLinkedQueue<RPCRequest> mRequestQueue;

    private HandlerThread mThread;
    private Handler mRequestHandler;

    private Runnable mHanRunnable = new Runnable() {
        @Override
        public void run() {
            RPCRequest request = mRequestQueue.poll();
            if (request != null) {
                send(request);
            }
            mRequestHandler.postDelayed(this, 500);
        }
    };

    public NetWorkAdapter() {
        mRequestQueue  = new ConcurrentLinkedQueue<RPCRequest>();

        mThread = new HandlerThread("Request Handler Thread");
        mThread.start();
        mRequestHandler = new Handler(mThread.getLooper());
        mRequestHandler.post(mHanRunnable);

    }

    public void execute(final RPCRequest request) {
        if (request != null) {
            mRequestQueue.add(request);
        }
    }

    private void send(final RPCRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request is NULL!");
        }
        SoapObject rpcMessage = request.getSoapMessage();
        if (ClientLog.DEBUG) {
            Log.d(TAG, "Sent Request: " + rpcMessage.toString());
        }
        SoapSerializationEnvelope soapEnvelope = new SoapSerializationEnvelope(110);
        soapEnvelope.bodyOut = rpcMessage;
        soapEnvelope.dotNet  = true;
        soapEnvelope.setOutputSoapObject(rpcMessage);
        HttpTransportSE localHttpTransportSE = new HttpTransportSE(TRAFFIC_SERVICE_URI, CONNECTION_TIME_OUT);
        String errorMessage = null;
        try {
            localHttpTransportSE.call(request.getSoapAction(), soapEnvelope);
            if (ClientLog.DEBUG && soapEnvelope.bodyIn != null) {
                Log.d(TAG, "Received Response: " + soapEnvelope.bodyIn.toString());
            }
        } catch (Exception e) {
            errorMessage = e.getLocalizedMessage();
            if (errorMessage == null) {
                errorMessage = "Unknown Error!!!";
            }
            Log.e(TAG, "send() - " + errorMessage);
            
        }
        request.onRequestComplete(soapEnvelope.bodyIn, errorMessage);
      }
}
