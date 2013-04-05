package com.telepathic.finder.sdk.traffic.request;

import java.util.ArrayList;

import org.ksoap2.serialization.SoapObject;

public class GetStationNameRequest extends RPCBaseRequest {

    private static final String METHOD_NAME = "stationFuzzyQuery";
    private static final String KEY_PAGE_INDEX = "pageIndex";
    private static final String KEY_PAGE_SIZE = "pageSize";
    private static final String KEY_QUERY = "query";

    private static final int PAGE_INDEX = 1;
    private static final int PAGE_SIZE = 100;
    private static final int MAX_STATION_NUM = PAGE_SIZE;

    //response keys
    private static final String KEY_TOTAL_NUMBER = "totalNum";
    private static final String KEY_STATION_NAME = "station";

    public GetStationNameRequest(String query) {
        super(METHOD_NAME);
        addParameter(KEY_PAGE_INDEX, PAGE_INDEX);
        addParameter(KEY_PAGE_SIZE, PAGE_SIZE);
        addParameter(KEY_QUERY, query);
    }

    @Override
    void handleError(int errorCode, String errorMessage) {
        if (mCallback != null) {
            mCallback.onError(errorCode, errorMessage);
        }
    }

    @Override
    void handleResponse(SoapObject dataSet) {
        int totalNum = 0;
        SoapObject dataEntry = null;
        if (dataSet != null && dataSet.getPropertyCount() >= 1) {
            dataEntry = (SoapObject)dataSet.getProperty(0);
            totalNum = Integer.parseInt((dataEntry.getPrimitivePropertyAsString(KEY_TOTAL_NUMBER)));
            if (totalNum > MAX_STATION_NUM) {
                totalNum = MAX_STATION_NUM;
            }
        }
        ArrayList<String> stationNames = new ArrayList<String>();
        for(int idx = 0; idx < totalNum; idx++) {
            dataEntry = (SoapObject)dataSet.getProperty(idx);
            String name = dataEntry.getPrimitivePropertyAsString(KEY_STATION_NAME);
            stationNames.add(name);
        }
        if (mCallback != null) {
            mCallback.onSuccess(stationNames);
        }
    }

}
