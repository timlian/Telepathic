package com.telepathic.finder.sdk;

public interface BusLineListener {

    public void onSuccess(BusLineRoute route);

    public void onError(String errorMessage);
}
