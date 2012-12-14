package com.telepathic.finder.sdk;

public interface BusLocationListener {

    public void onSuccess(String lineNumber, String distance);

    public void onError(String errorMessage);

}
