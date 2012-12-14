package com.telepathic.finder.sdk.network;

import org.ksoap2.serialization.SoapObject;

abstract class RPCRequest {

    private static final String NAMESPACE = "http://tempuri.org/";

    private SoapObject mRpc;

    public RPCRequest(String name) {
        mRpc = new SoapObject(NAMESPACE, name);
    }

    protected void addParameter(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("参数名不能为null");
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

    abstract void onResponse(Object result);
}
