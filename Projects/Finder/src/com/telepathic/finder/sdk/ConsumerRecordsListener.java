package com.telepathic.finder.sdk;

import java.util.ArrayList;

public interface ConsumerRecordsListener {

    public void onSuccess(ArrayList<ConsumerRecord> consumerRecords);

    public void onError(String errorMessage);
}
