package com.telepathic.finder.sdk.network;

import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;

abstract class RPCRequest {

    private static final String NAMESPACE = "http://tempuri.org/";

    private static final String KEY_DIFF_GRAM = "diffgram";
    private static final String KEY_NEW_DATA_SET = "NewDataSet";
    private static final String KEY_DOCUMENT_ELEMENT = "DocumentElement";

    protected static final String KEY_ERROR_CODE = "code";
    protected static final String KEY_ERROR_MESSAGE = "msg";

    protected static final int NO_ERROR = 200;

    private SoapObject mRpc;

    public RPCRequest(String name) {
        mRpc = new SoapObject(NAMESPACE, name);
    }

    protected abstract String getResponseName();

    protected abstract void handleError(String errorMessage);

    protected abstract void handleResponse(SoapObject dataSet);

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
        final SoapObject response = (SoapObject) result.getProperty(getResponseName());
        if (response != null) {
            final SoapObject diffGram = (SoapObject) response.getProperty(KEY_DIFF_GRAM);
            if (diffGram != null) {
                SoapObject dataSet = null;
                try {
                    dataSet = (SoapObject) diffGram.getProperty(KEY_NEW_DATA_SET);
                } catch (RuntimeException e) {
                    dataSet = (SoapObject) diffGram.getProperty(KEY_DOCUMENT_ELEMENT);
                }
                if (dataSet != null) {
                    final SoapObject firstDataEntry = (SoapObject) dataSet.getProperty(0);
                    final String errorCode = firstDataEntry.getPrimitivePropertyAsString(KEY_ERROR_CODE);
                    final String errorMessage = firstDataEntry.getPrimitivePropertyAsString(KEY_ERROR_MESSAGE);
                    if (NO_ERROR == Integer.parseInt(errorCode)) {
                        if (isValidDataEntry(firstDataEntry)) {
                            handleResponse(dataSet);
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

    private static boolean isValidDataEntry(SoapObject dataEntry) {
        boolean isValid = false;
        if (dataEntry.getPropertyCount() <= 2) {
            return isValid;
        }
        Object propertyObject = dataEntry.getProperty(0);
        if (propertyObject instanceof SoapPrimitive) {
            isValid = true;
            return isValid;
        }
        for(int idx = 0; idx < dataEntry.getPropertyCount() - 2; idx++) {
            propertyObject = dataEntry.getProperty(idx);
            if (((SoapObject)propertyObject).getPropertyCount() > 0) {
                isValid = true;
                break;
            }
        }
        return isValid;
    }

}
