package com.telepathic.finder.sdk.network;

public class BusStationLinesRequest extends RPCRequest {
    private static final String METHOD_NAME = "getBusStationLines";

    private static final String KEY_BUS_STATION = "busStation";
    private static final String KEY_PAGE_INDEX = "pageIndex";
    private static final String KEY_PAGE_SIZE = "pageSize";

    public BusStationLinesRequest(String busStation, String pageIndex, String pageSize) {
        super(METHOD_NAME);
        addParameter(KEY_BUS_STATION, busStation);
        addParameter(KEY_PAGE_INDEX, pageIndex);
        addParameter(KEY_PAGE_SIZE, pageSize);
    }

    /*
     * Table1=anyType{station=新会展中心公交站; stationAlias=anyType{}; stationGPS=50022; lineName=298,115,84,118,102; totalNum=2; code=200; msg=成功; };
     * Table1=anyType{station=新会展中心公交站; stationAlias=anyType{}; stationGPS=50023; lineName=84,102,115,298,118; };
     */
    @Override
    void onResponse(Object result, String errorMessage) {
        // TODO Auto-generated method stub

    }
}
