package com.telepathic.finder.sdk.traffic.task;

import java.util.ArrayList;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.mapapi.search.MKPoiResult;
import com.baidu.mapapi.search.MKSearch;
import com.telepathic.finder.util.Utils;

public class SearchBusLineTask extends BaseTask<ArrayList<MKPoiInfo>>{
    private static final String TAG = SearchBusLineTask.class.getSimpleName();
    private MKSearch mMapSearch;
    private final String mCity;
    private final String mLineNumber;
    private final Object mLock;

    public SearchBusLineTask(BMapManager manager, String city, String lineNumber) {
        super("SearchBusLineTask");
        mCity = city;
        mLineNumber = lineNumber;
        mMapSearch = new MKSearch();
        mMapSearch.init(manager, new PoiSearchListener());
        mLock = new Object();
    }

    @Override
    protected void doWork() {
        mMapSearch.poiSearchInCity(mCity, mLineNumber);
        synchronized (mLock) {
            try {
                mLock.wait();
            } catch (InterruptedException e) {
                Utils.debug(TAG, "doWork is interrupted.");
            }
        }
    }

    private class PoiSearchListener extends MKSearchListenerImpl {
        @Override
        public void onGetPoiResult(MKPoiResult res, int type, int error) {
        	ArrayList<MKPoiInfo> poiList = new ArrayList<MKPoiInfo>();
            if (error == 0 || res != null) {
                ArrayList<MKPoiInfo> allPois = res.getAllPoi();
                if (allPois != null && allPois.size() > 0) {
                    for (MKPoiInfo poiInfo : allPois) {
                        // poi类型:
                    	// 0：普通点，1：公交站，
                    	// 2：公交线路，3：地铁站，4：地铁线路
                        if (poiInfo.ePoiType == 2) {
                        	poiList.add(poiInfo);
                        }
                    }
                }
            }
            TaskResult<ArrayList<MKPoiInfo>> taskResult = new TaskResult<ArrayList<MKPoiInfo>>();
            taskResult.setErrorCode(error);
            taskResult.setResult(poiList);
            setTaskResult(taskResult);
            synchronized (mLock) {
                mLock.notifyAll();
            }
        }

    }
}
