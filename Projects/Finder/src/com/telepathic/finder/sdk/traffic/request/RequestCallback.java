package com.telepathic.finder.sdk.traffic.request;

public interface RequestCallback {

    void onError(int errorCode, String errorMessage);

    void onSuccess(Object result);
}
