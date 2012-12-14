package com.telepathic.finder.sdk.network;

import org.ksoap2.SoapFault;
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

    private int getErrorCode(SoapObject returnInfo) {
        int retCode = -1;
        String errCode = returnInfo.getPrimitivePropertyAsString("code");
        String errMessage = returnInfo.getPrimitivePropertyAsString("msg");
        retCode = Integer.parseInt(errCode);
        if (retCode != 200 && mListener != null) {
            mListener.onError(errMessage);
        }
        return retCode;
    }
    
    private void process(SoapObject result) {
        SoapObject resultObject = (SoapObject) result.getProperty("getBusLineRouteResult");
        if (resultObject != null) {
            resultObject = (SoapObject) resultObject.getProperty("diffgram");
            if (resultObject != null) {
                resultObject = (SoapObject) resultObject.getProperty("NewDataSet");
                if (resultObject != null) {
                    if (getErrorCode((SoapObject) resultObject.getProperty(0)) == 200) {
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
    
    @Override
    public void onResponse(Object result) {
        if (result instanceof SoapObject) {
            process((SoapObject) result);
        } else if (result instanceof SoapFault) {
            
        } else {
            throw new RuntimeException("Unknown Exception!!!");
        }
    }
}
