package com.telepathic.finder.sdk.network;

import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;

abstract class RPCRequest {

    private static final String NAMESPACE = "http://tempuri.org/";

    protected static final String KEY_DIFF_GRAM = "diffgram";
    protected static final String KEY_NEW_DATA_SET = "NewDataSet";

    protected static final String KEY_ERROR_CODE = "code";
    protected static final String KEY_ERROR_MESSAGE = "msg";

    protected static final int NO_ERROR = 200;

    private SoapObject mRpc;

    public RPCRequest(String name) {
        mRpc = new SoapObject(NAMESPACE, name);
    }

    protected abstract String getResponseName();
    
    protected abstract void handleError(String errorMessage);
    
    protected abstract void handleResponse(SoapObject newDataSet);
    
    protected void addParameter(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("The parameter key is null.");
        }
        mRpc.addProperty(key, value);
    }

    protected void setParameter(String key, Object value) {
        Object property = mRpc.getProperty(key);
        if (property != null) {
            for(int idx = 0; idx < mRpc.getPropertyCount(); idx++) {
                if (property.equals(mRpc.getProperty(idx))) {
                    mRpc.setProperty(idx, value);
                    break;
                }
            }
        } else {
            mRpc.addProperty(key, value);
        }
    }

    public SoapObject getSoapMessage() {
        return mRpc;
    }

    public String getSoapAction() {
        return mRpc.getNamespace() + mRpc.getName();
    }

    public String getMethodName() {
        return mRpc.getName();
    }

    protected boolean isComplete() {
        return true;
    }
    
    public void onResponse(Object result, String errorMessage) {
    	 if (errorMessage != null) {
             handleError(errorMessage);
             return ;
         }
         if (result instanceof SoapObject) {
             process((SoapObject)result);
         } else if (result instanceof SoapFault) {

         } else {
             throw new RuntimeException("Unknown Exception!!!");
         }
    }
    
    private void process(SoapObject result) {
    	final SoapObject response = (SoapObject)result.getProperty(getResponseName());
    	 if (response != null) {
             final SoapObject diffGram = (SoapObject) response.getProperty(KEY_DIFF_GRAM);
             if (diffGram != null) {
                 final SoapObject newDataSet = (SoapObject) diffGram.getProperty(KEY_NEW_DATA_SET);
                 if (newDataSet != null) {
                     final SoapObject firstDataEntry = (SoapObject) newDataSet.getProperty(0);
                     final String errorCode = firstDataEntry.getPrimitivePropertyAsString(KEY_ERROR_CODE);
                     final String errorMessage = firstDataEntry.getPrimitivePropertyAsString(KEY_ERROR_MESSAGE);
                     if (NO_ERROR == Integer.parseInt(errorCode)) {
                     	if (firstDataEntry.getPropertyCount() > 2) {
                     		handleResponse(newDataSet);
                     	} else {
                     		handleError("No data.");
                     	}
                     } else {
                    	 handleError(errorMessage);
                     }
                 }
             }
    	 }
    }
    
}
