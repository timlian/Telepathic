package com.telepathic.finder.sdk.traffic.request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ksoap2.serialization.SoapObject;

import android.R.integer;

public class GetBusStationLinesRequest extends RPCBaseRequest {
    private static final String METHOD_NAME = "getBusStationLines";
    // parameter keys
    private static final String KEY_BUS_STATION = "busStation";
    private static final String KEY_PAGE_INDEX = "pageIndex";
    private static final String KEY_PAGE_SIZE = "pageSize";
    //response keys
    private static final String KEY_STATION_GPS = "stationGPS";
    private static final String KEY_LINE_NAME = "lineName";
    private static final String KEY_STATION = "station";

    public static class StationLines {
    	private String mName;
    	private String mGpsNumber;
    	private List<String> mLines;
    	private String[] mDirections;
    	private List<Integer> mIndices;
    	
    	private StationLines(String name, String gpsNumber, List<String> lines) {
    		mName = name;
    		mGpsNumber = gpsNumber;
    		mLines = lines;
    		mDirections = new String[lines.size()];
    	}
    	
    	public String getGpsNumber() {
    		return mGpsNumber;
    	}
    	
    	public List<String> getLines() {
    		return Collections.unmodifiableList(mLines);
    	}
    	
    	public void setDirection(String lineNumber, String direction) {
    		int index = find(lineNumber);
    		if (index != -1) {
    			mDirections[index] = direction;
    		}
    	}
    	
    	public void setStationIndex(String lineNumber, Integer index) {
    		int pos = find(lineNumber);
    		if (pos != -1) {
    			mIndices.set(pos, index);
    		}
    	}
    	
    	public int getStationIndex(String lineNumber) {
    		int index = -1;
    		int pos = find(lineNumber);
    		if (pos != -1) {
    			index = mIndices.get(pos);
    		}
    		return index;
    	}
    	
    	public String getDirection(String lineNumber) {
    		String direction = null;
    		int index = find(lineNumber);
    		if (index != -1) {
    			direction = mDirections[index];
    		}
    		return direction;
    	}
    	
    	public String getName() {
    		return mName;
    	}
    	
		private int find(String lineNumber) {
			int index = -1;
			for (int idx = 0; idx < mLines.size(); idx++) {
				if (mLines.get(idx).equals(lineNumber)) {
					index = idx;
				}
			}
			return index;
		}
    }
    
    public GetBusStationLinesRequest(String statinName) {
         super(METHOD_NAME);
         addParameter(KEY_BUS_STATION, statinName);
         addParameter(KEY_PAGE_INDEX, "1");
         addParameter(KEY_PAGE_SIZE, "30");
    }

    @Override
    void handleError(int errorCode, String errorMessage) {
    	if (mCallback != null) {
    		mCallback.onError(errorCode, errorMessage);
    	}
    }

    /*
     * Table1=anyType{station=新会展中心公交站; stationAlias=anyType{}; stationGPS=50022; lineName=298,115,84,118,102; totalNum=2; code=200; msg=成功; };
     * Table1=anyType{station=新会展中心公交站; stationAlias=anyType{}; stationGPS=50023; lineName=84,102,115,298,118; };
     */
    @Override
    void handleResponse(SoapObject newDataSet) {
        SoapObject dataEntry = null;
        final int count = newDataSet.getPropertyCount();
        ArrayList<StationLines> stationLines = new ArrayList<StationLines>();
        for (int idx = 0; idx < count; idx++) {
            dataEntry = (SoapObject) newDataSet.getProperty(idx);
            String stationName = dataEntry.getPrimitivePropertyAsString(KEY_STATION);
            String gpsNumber = dataEntry.getPrimitivePropertyAsString(KEY_STATION_GPS);
            String lineNames = dataEntry.getPrimitivePropertyAsString(KEY_LINE_NAME);
            stationLines.add(new StationLines(stationName, gpsNumber, Arrays.asList(lineNames.split(","))));
        }
        if (mCallback != null) {
        	mCallback.onSuccess(stationLines);
        }
    }
}
