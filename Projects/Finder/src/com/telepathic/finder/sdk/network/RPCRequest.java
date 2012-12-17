package com.telepathic.finder.sdk.network;

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

    protected void addParameter(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("The parameter key is null.");
        }
        mRpc.addProperty(key, value);
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

    abstract void onRequestComplete(Object result, String errorMessage);
}
