package com.telepathic.finder.sdk.traffic.entity;

import java.util.ArrayList;
import java.util.HashMap;

import com.telepathic.finder.sdk.traffic.entity.BusLine.Direction;

public class BusStationLines {
	/**
	 * The station name
	 */
	private String mStationName;
	/**
	 * The gps number of the station
	 */
	private String mGpsNumber;
	/**
	 * The bus lines 
	 */
	private ArrayList<BusLine> mBusLines = new ArrayList<BusLine>();
	/**
	 * The line route map
	 */
	private HashMap<String, Direction> mLineRouteMap = new HashMap<String, Direction>();
	
	public String getStationName() {
		return mStationName;
	}
	
	public void setStationName(String stationName) {
		mStationName = stationName;
	}
	
	public String getGpsNumber() {
		return mGpsNumber;
	}
	
	public void setGpsNumber(String gpsNumber) {
		mGpsNumber = gpsNumber;
	}
	
	public ArrayList<BusLine> getBusLines() {
		return mBusLines;
	}
	
	public BusLine getBusLine(String lineNumber) {
		BusLine result = null;
		for (BusLine line : mBusLines) {
			if (line.getLineNumber().equals(lineNumber)) {
				result = line;
			}
		}
		return result;
	}
	
	public void setBusLine(BusLine line) {
		if (!mBusLines.contains(line)) {
			mBusLines.add(line);
		}
	}
	
	public void setLineRoute(String lineNumber, Direction direction) {
		mLineRouteMap.put(lineNumber, direction);
	}
	
	public HashMap<String, Direction> getLineRouteMap() {
		return mLineRouteMap;
	}
	
	public Direction getRouteDirection(String lineNumber) {
		return mLineRouteMap.get(lineNumber);
	}

}
