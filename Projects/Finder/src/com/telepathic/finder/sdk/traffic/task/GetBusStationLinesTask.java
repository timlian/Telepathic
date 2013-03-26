package com.telepathic.finder.sdk.traffic.task;

import java.util.List;

import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXStationLines;
import com.telepathic.finder.sdk.traffic.request.GetBusStationLinesRequest;
import com.telepathic.finder.sdk.traffic.request.RequestCallback;
import com.telepathic.finder.sdk.traffic.request.RequestExecutor;

public class GetBusStationLinesTask extends BaseTask<List<KXStationLines>> {
    private final String mStationName;

    public GetBusStationLinesTask(String stationName) {
        super("GetBusStationLinesTask");
        mStationName = stationName;
    }

    @Override
    protected void doWork() {
        GetBusStationLinesRequest request = new GetBusStationLinesRequest(mStationName);
        final TaskResult<List<KXStationLines>> taskResult = new TaskResult<List<KXStationLines>>();
        RequestExecutor.execute(request, new RequestCallback() {
            @Override
            public void onSuccess(Object result) {
                taskResult.setResult((List<KXStationLines>)result);
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
