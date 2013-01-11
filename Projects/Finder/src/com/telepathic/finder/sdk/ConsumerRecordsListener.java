package com.telepathic.finder.sdk;

public interface ConsumerRecordsListener {

    public void onSuccess(ConsumptionInfo dataInfo);

    public void onError(String errorMessage);
}
