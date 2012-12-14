package com.telepathic.finder.sdk.network;

import org.ksoap2.serialization.SoapObject;

import com.telepathic.finder.sdk.BusLineListener;
import com.telepathic.finder.sdk.BusLineRoute;

public class BusLineRouteRequest extends RPCRequest {
    
    private static final String METHOD_NAME = "getBusLineRoute";
    
    private BusLineListener mListener;
    
    public BusLineRouteRequest(String line, BusLineListener listener) {
        super(METHOD_NAME);
        addParameter("busLine", line);
        mListener = listener;
    }
    
    @Override
    public void onResponse(SoapObject result) {
        SoapObject resultObject = (SoapObject) result.getProperty("getBusLineRouteResult");
        if (resultObject != null) {
            resultObject = (SoapObject) resultObject.getProperty("diffgram");
            if (resultObject != null) {
                resultObject = (SoapObject) resultObject.getProperty("NewDataSet");
                if (resultObject != null) {
                    for(int i = 0; i < resultObject.getPropertyCount(); i++) {
                        BusLineRoute route = new BusLineRoute((SoapObject) resultObject.getProperty(i));
                        if (mListener != null && i == 0) {
                            mListener.onSuccess(route);
                        }
                    }
                }
            }
        }
    }
}
