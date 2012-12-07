package com.telepathic.finder.service;

import java.util.ArrayList;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

public class LocationProvider {
    
    private Context mContext;
    
    private ArrayList<LocationUpdateListener> mListeners;
    
    private ActivityManager mActivityManager;
    
    private static LocationProvider mProvider;
    
    public interface LocationUpdateListener {
        
        public void onLocationUpdate(Location location);
    }
    
    private LocationProvider(Context context) { 
        mContext = context;
        mListeners = new ArrayList<LocationProvider.LocationUpdateListener>();
        mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
    }
    
    public void notifyLocationUpdate(Location location) {
        for(LocationUpdateListener listener : mListeners) {
            listener.onLocationUpdate(location);
        }
    }
    
    public static synchronized LocationProvider getInstance(Context context) {
        if (mProvider == null) {
            mProvider = new LocationProvider(context);
        }
        return mProvider;
    }
    
    public void registerListener(LocationUpdateListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
        if (!isServiceRunning() && mListeners.size() > 0) {
            startService();
        }
    }
    
    public void unregisterListener(LocationUpdateListener listener) {
        if (mListeners.contains(listener)) {
            mListeners.remove(listener);
        }
        if (isServiceRunning() && mListeners.size() == 0) {
            stopService();
        }
    }
    
    private void startService() {
        Intent intent = new Intent(mContext, LocationService.class);
        mContext.startService(intent);
    }
    
    private void stopService() {
        Intent intent = new Intent(mContext, LocationService.class);
        mContext.stopService(intent);
    }
    
    private boolean isServiceRunning() {
        ArrayList<RunningServiceInfo> runningServiceInfos = (ArrayList<RunningServiceInfo>)mActivityManager.getRunningServices(1024);
        for (int i = 0; i < runningServiceInfos.size(); i++) {
            if (runningServiceInfos.get(i).service.getClassName().equals("com.telepathic.finder.service.LocationService")) {
                return true;
            }
        }
        return false;
    }
}
