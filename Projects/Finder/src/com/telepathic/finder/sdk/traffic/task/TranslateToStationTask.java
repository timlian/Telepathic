package com.telepathic.finder.sdk.traffic.task;

import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusStation;
import com.telepathic.finder.sdk.traffic.request.GetBusStationRequest;
import com.telepathic.finder.sdk.traffic.request.RequestCallback;
import com.telepathic.finder.sdk.traffic.request.RequestExecutor;

public class TranslateToStationTask extends BaseTask<KXBusStation> {
    private final String mLineNumber;
    private final String mGpsNumber;

    public TranslateToStationTask(String lineNumber, String gpsNumber) {
        super("TranslateToStationTask");
        mLineNumber = lineNumber;
        mGpsNumber = gpsNumber;
    }

    @Override
    protected void doWork() {
        GetBusStationRequest request = new GetBusStationRequest(mLineNumber, mGpsNumber);
        final TaskResult<KXBusStation> taskResult = new TaskResult<KXBusStation>();
        RequestExecutor.execute(request, new RequestCallback() {
            @Override
            public void onSuccess(Object result) {
                taskResult.setResult((KXBusStation)result);
                setTaskResult(taskResult);
            }

            @Override
            public void onError(int errorCode, String errorMessage) {
                taskResult.setErrorCode(errorCode);
                taskResult.setErrorMessage(errorMessage);
                setTaskResult(taskResult);
            }
        });
    }

}
