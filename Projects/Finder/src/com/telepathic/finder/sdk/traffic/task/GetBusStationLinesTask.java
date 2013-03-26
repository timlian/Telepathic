package com.telepathic.finder.sdk.traffic.task;

import java.util.List;

import com.telepathic.finder.sdk.traffic.request.GetBusStationLinesRequest;
import com.telepathic.finder.sdk.traffic.request.RequestCallback;
import com.telepathic.finder.sdk.traffic.request.RequestExecutor;
import com.telepathic.finder.sdk.traffic.request.GetBusStationLinesRequest.StationLines;

public class GetBusStationLinesTask extends BaseTask<List<StationLines>> {
    private final String mStationName;

    public GetBusStationLinesTask(String stationName) {
        super("GetBusStationLinesTask");
        mStationName = stationName;
    }

    @Override
    protected void doWork() {
        GetBusStationLinesRequest request = new GetBusStationLinesRequest(mStationName);
        final TaskResult<List<StationLines>> taskResult = new TaskResult<List<StationLines>>();
        RequestExecutor.execute(request, new RequestCallback() {
            @Override
            public void onSuccess(Object result) {
                taskResult.setResult((List<StationLines>)result);
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
