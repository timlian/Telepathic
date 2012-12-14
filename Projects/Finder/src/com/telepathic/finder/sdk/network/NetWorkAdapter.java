package com.telepathic.finder.sdk.network;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.xmlpull.v1.XmlPullParserException;

import android.os.Handler;
import android.os.HandlerThread;

public class NetWorkAdapter {
    private static final String TAG = "NetWorkAdapter";

    private ConcurrentLinkedQueue<RPCRequest> mRequestQueue;
    private SoapMessageSender mSoapMessageSender;

    private HandlerThread mThread;
    private Handler mRequestHandler;

    private Runnable mHanRunnable = new Runnable() {
        @Override
        public void run() {
            RPCRequest request = mRequestQueue.poll();
            if (request != null) {
                sendRequest(request);
            }
            mRequestHandler.postDelayed(this, 500);
        }
    };

    public NetWorkAdapter() {
        mRequestQueue  = new ConcurrentLinkedQueue<RPCRequest>();

        mSoapMessageSender = new SoapMessageSender();

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

    private void sendRequest(final RPCRequest request) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mSoapMessageSender.sendMessage(request);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
