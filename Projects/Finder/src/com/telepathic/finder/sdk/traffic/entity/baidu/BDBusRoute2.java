package com.telepathic.finder.sdk.traffic.entity.baidu;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mapapi.search.MKRoute;
import com.baidu.mapapi.search.MKStep;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class BDBusRoute2 extends MKRoute {

	private List<BDBusStation2> mStationList;
	private ArrayList<ArrayList<GeoPoint>> mPointList;
	
	public BDBusRoute2(List<BDBusStation2> stationList, ArrayList<ArrayList<GeoPoint>> pointList) {
		mStationList = stationList;
		mPointList = pointList;
	}
	
	@Override
	public ArrayList<ArrayList<GeoPoint>> getArrayPoints() {
		return mPointList;
	}
	
	@Override
	public int getDistance() {
		return 0;
	}
	
	@Override
	public GeoPoint getEnd() {
		if (mStationList == null || mStationList.size() == 0) {
			return null;
		}
		BDBusStation2 lastStation = mStationList.get(mStationList.size() -1);
		if (lastStation == null) {
			return null;
		}
		return lastStation.getPoint();
	}
	
	@Override
	public int getIndex() {
		return 0;
	}
	
	@Override
	public int getNumSteps() {
		if (mStationList == null) {
			return 0;
		}
		return mStationList.size();
	}
	
	@Override
	public int getRouteType() {
		return 3;
	}
	
	@Override
	public GeoPoint getStart() {
		if (mStationList == null || mStationList.size() == 0) {
			return null;
		}
		BDBusStation2 firstStation = mStationList.get(0);
		if (firstStation == null) {
			return null;
		}
		return firstStation.getPoint();
	}
	
	@Override
	public MKStep getStep(int index) {
		return mStationList.get(index);
	}
	
	@Override
	public String getTip() {
		return null;
	}
	
}
