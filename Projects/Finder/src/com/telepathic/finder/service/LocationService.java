/**
 * Copyright (C) 2012 Telepathic LTD. All Rights Reserved.
 */

package com.telepathic.finder.service;

import com.telepathic.finder.util.ClientLog;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class LocationService extends Service {

    private static final String TAG = "LocationService";

    private static final int TWO_MINUTES = 1000 * 60 * 2;

    private LocationManager mLocationManager;

    private Location mBestLocation;

    private LocationProvider mLocationProvider;

    LocationListener mLocationListener = new LocationListener() {
        /**
         *  Called when a new location is found by the location provider.
         */
        public void onLocationChanged(Location location) {
            Log.d(TAG, "Received new location: " + location.getLatitude() + ", " + location.getLongitude());
            setBestLocation(location);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}

      };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mLocationProvider = LocationProvider.getInstance(getApplicationContext());

        boolean isGpsProviderEnabled = false;
        try {
            isGpsProviderEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isGpsProviderEnabled) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
        }

        boolean isNetworkProviderEnabled = false;
        try {
            isNetworkProviderEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (isNetworkProviderEnabled) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
        }

        Location lastGpsLocation = null;
        if (isGpsProviderEnabled ) {
            lastGpsLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (ClientLog.DEBUG && lastGpsLocation != null) {
                ClientLog.debug(TAG, "last location from gps: " + lastGpsLocation.getLatitude() + ", " + lastGpsLocation.getLongitude());
            }
        }

        Location lastNetworkLocation = null;
        if (isNetworkProviderEnabled) {
            lastNetworkLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (ClientLog.DEBUG && lastNetworkLocation != null) {
                Log.d(TAG, "last location from network: " + lastNetworkLocation.getLatitude() + ", " + lastNetworkLocation.getLongitude());
            }
        }

        if (isBetterLocation(lastGpsLocation, lastNetworkLocation)) {
            setBestLocation(lastGpsLocation);
        } else {
            setBestLocation(lastNetworkLocation);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        return super.onStartCommand(intent, flags, startId);
    }

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
   protected boolean isBetterLocation(Location location, Location currentBestLocation) {
       if (currentBestLocation == null) {
           // A new location is always better than no location
           return true;
       }
       // Check whether the new location fix is newer or older
       long timeDelta = location.getTime() - currentBestLocation.getTime();
       boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
       boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
       boolean isNewer = timeDelta > 0;

       // If it's been more than two minutes since the current location, use the new location
       // because the user has likely moved
       if (isSignificantlyNewer) {
           return true;
       // If the new location is more than two minutes older, it must be worse
       } else if (isSignificantlyOlder) {
           return false;
       }

       // Check whether the new location fix is more or less accurate
       int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
       boolean isLessAccurate = accuracyDelta > 0;
       boolean isMoreAccurate = accuracyDelta < 0;
       boolean isSignificantlyLessAccurate = accuracyDelta > 200;

       // Check if the old and new location are from the same provider
       boolean isFromSameProvider = isSameProvider(location.getProvider(),
               currentBestLocation.getProvider());

       // Determine location quality using a combination of timeliness and accuracy
       if (isMoreAccurate) {
           return true;
       } else if (isNewer && !isLessAccurate) {
           return true;
       } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
           return true;
       }
       return false;
   }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    @Override
    public void onDestroy() {
        mLocationManager.removeUpdates(mLocationListener);
    }

    private void setBestLocation(Location location) {
        mBestLocation = location;
        mLocationProvider.notifyLocationUpdate(mBestLocation);
    }
}
