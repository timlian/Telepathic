package com.telepathic.finder.sdk;

import java.util.ArrayList;

public interface BusLineListener {

    public void onSuccess(ArrayList<BusLineRoute> route);

    public void onError(String errorMessage);
}
