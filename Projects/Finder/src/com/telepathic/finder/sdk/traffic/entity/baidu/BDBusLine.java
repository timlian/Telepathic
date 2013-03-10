package com.telepathic.finder.sdk.traffic.entity.baidu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BDBusLine {
	 /**
     * The line number
     */
    private String mLineNumber;
    /**
     * The bus routes
     */
    private List<BDBusRoute> mRouteList = new ArrayList<BDBusRoute>();
    
    public BDBusLine(String lineNumber) {
    	mLineNumber = lineNumber;
    }
    
    public String getLineNumber() {
    	return mLineNumber;
    }
    
    public List<BDBusRoute> getRouteList() {
        return Collections.unmodifiableList(mRouteList);
    }
    
    public void addRoute(BDBusRoute route) {
    	if (!mRouteList.contains(route)) {
    		mRouteList.add(route);
    	}
    }
    
    public int getRouteCount() {
    	return mRouteList.size();
    }
    
    public BDBusRoute getRoute(int index) {
    	return mRouteList.get(index);
    }
}
