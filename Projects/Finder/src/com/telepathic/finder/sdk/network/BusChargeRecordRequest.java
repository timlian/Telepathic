package com.telepathic.finder.sdk.network;

import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;

import com.telepathic.finder.sdk.BusLineRoute;
import com.telepathic.finder.sdk.ChargeRecordsListener;

import android.util.Log;

public class BusChargeRecordRequest extends RPCRequest {
    private static final String METHOD_NAME = "getConsumerRecords";
    
    private static final String KEY_CARD_ID = "cardID";
    private static final String KEY_COUNT = "count";
    
    private ChargeRecordsListener mListener;
    
    public BusChargeRecordRequest(String cardId, String count, ChargeRecordsListener listener) {
        super(METHOD_NAME);
        addParameter(KEY_CARD_ID, cardId);
        addParameter(KEY_COUNT, count);
        mListener = listener;
    }
    
    @Override
    public void onRequestComplete(Object response, String errorMessage) {
        if (errorMessage != null) {
            if (mListener != null) {
                mListener.onError(errorMessage);
            }
            return ;
        }
        if (response instanceof SoapObject) {
            process((SoapObject) response);
        } else if (response instanceof SoapFault) {
            
        } else {
            throw new RuntimeException("Unknown Exception!!!");
        }
    }
    
    private void process(SoapObject result) {
        SoapObject resultObject = (SoapObject) result.getProperty("getConsumerRecordsResult");
        if (resultObject != null) {
            resultObject = (SoapObject) resultObject.getProperty("diffgram");
            if (resultObject != null) {
                resultObject = (SoapObject) resultObject.getProperty("NewDataSet");
                if (resultObject != null) {
                    mListener.onSuccess(resultObject.toString());
                }
            }
        }
    }

}
