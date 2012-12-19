package com.telepathic.finder.sdk.network;

public class BusStationLinesRequest extends RPCRequest {
    private static final String METHOD_NAME = "getBusStationLines";
    
    public BusStationLinesRequest(String busStation) {
        super(METHOD_NAME);
    }
    @Override
    void onRequestComplete(Object result, String errorMessage) {
        // TODO Auto-generated method stub
        
    }
}
