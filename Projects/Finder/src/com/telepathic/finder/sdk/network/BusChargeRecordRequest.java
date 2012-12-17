package com.telepathic.finder.sdk.network;

import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import com.telepathic.finder.sdk.ChargeRecordsListener;

public class BusChargeRecordRequest extends RPCRequest {
    private static final String METHOD_NAME = "getConsumerRecords";
    
    private static final String KEY_CARD_ID = "cardID";
    private static final String KEY_COUNT = "count";
    
    private static final String KEY_RESPONSE = "getConsumerRecordsResult";

    
    private ChargeRecordsListener mListener;
    
    public BusChargeRecordRequest(String cardId, String count, ChargeRecordsListener listener) {
        super(METHOD_NAME);
        addParameter(KEY_CARD_ID, cardId);
        addParameter(KEY_COUNT, count);
        mListener = listener;
    }
    
    @Override
    public void onRequestComplete(Object result, String errorMessage) {
        if (errorMessage != null) {
            if (mListener != null) {
                mListener.onError(errorMessage);
            }
            return ;
        }
        if (result instanceof SoapObject) {
            final SoapObject response = (SoapObject)((SoapObject)result).getProperty(KEY_RESPONSE);
            process(response);
        } else if (result instanceof SoapFault) {
            
        } else {
            throw new RuntimeException("Unknown Exception!!!");
        }
    }
    
    /* 
     * Charge records response data entry example:
     * 
     * {lineNumber=102; busNumber=031164; cardID=000101545529; consumerTime=2012-6-30 21:39:51; consumerCount=2; residualCount=6; code=200; msg=³É¹¦; }
     *
     */
    private void process(SoapObject response) {
        if (response != null) {
            final SoapObject diffGram = (SoapObject) response.getProperty(KEY_DIFF_GRAM);
            if (diffGram != null) {
                final SoapObject newDataSet = (SoapObject) diffGram.getProperty(KEY_NEW_DATA_SET);
                if (newDataSet != null) {
                    final SoapObject firstDataEntry = (SoapObject) newDataSet.getProperty(0);
                    final String errorCode = firstDataEntry.getPrimitivePropertyAsString(KEY_ERROR_CODE);
                    final String errorMessage = firstDataEntry.getPrimitivePropertyAsString(KEY_ERROR_MESSAGE);
                    if (NO_ERROR == Integer.parseInt(errorCode)) {
                        SoapObject dataEntry = null;
                        StringBuilder chargeRecords = new StringBuilder();
                        for(int i = 0; i < newDataSet.getPropertyCount(); i++) {
                            dataEntry = (SoapObject) newDataSet.getProperty(i);
                            // Todo: parse the charge record objects.
                            chargeRecords.append(dataEntry.toString() + "\n\n");
                        }
                        if (mListener != null) {
                            mListener.onSuccess(chargeRecords.toString());
                        }
                    } else {
                        if (mListener != null) {
                            mListener.onError(errorMessage);
                        }
                    }
                }
            }
        }
    }

}
