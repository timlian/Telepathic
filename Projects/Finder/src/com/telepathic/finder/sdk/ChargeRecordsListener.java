package com.telepathic.finder.sdk;

import java.util.ArrayList;

public interface ChargeRecordsListener {

    public void onSuccess(ArrayList<ConsumerRecord> consumerRecords);

    public void onError(String errorMessage);
}
