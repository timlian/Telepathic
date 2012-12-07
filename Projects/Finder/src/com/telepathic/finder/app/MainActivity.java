package com.telepathic.finder.app;

import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.telepathic.finder.R;
import com.telepathic.finder.service.LocationProvider;
import com.telepathic.finder.service.LocationProvider.LocationUpdateListener;

public class MainActivity extends android.support.v4.app.FragmentActivity implements LocationUpdateListener {

    private static final String TAG = "MainActivity";
    
    private LocationProvider mLocationProvider;
    
    private int mPositionCount = 1;
    
    /**
     * Note that this may be null if the Google Play services APK is not available.
     */
    private GoogleMap mMap;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setUpMapIfNeeded();
        mLocationProvider = LocationProvider.getInstance(getApplicationContext());
        
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        mLocationProvider.registerListener(this);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        mLocationProvider.unregisterListener(this);
    }
    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView
     * MapView}) will show a prompt for the user to install/update the Google Play services APK on
     * their device.
     * <p>
     * A user can return to this Activity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the Activity may not have been
     * completely destroyed during this process (it is likely that it would only be stopped or
     * paused), {@link #onCreate(Bundle)} may not be called again so we should call this method in
     * {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(30, 104)).title("Tim Lian"));
    }
    
    /**
     * Change the camera position by animating the camera.
     */
    private void changeCamera(CameraUpdate update, CancelableCallback callback) {
        mMap.animateCamera(update, callback);
    }
    
    private void goToPosition(Location location) {
        if (checkReady()) {
            CameraPosition position = new CameraPosition.Builder().target(new LatLng(location.getLatitude(), location.getLongitude()))
                                          .zoom(15.5f)
                                          .bearing(0)
                                          .tilt(25)
                                          .build();
            changeCamera(CameraUpdateFactory.newCameraPosition(position), null);
            mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Telepathic - Position #" + mPositionCount++));
        }
        
    }
    
    /**
     * When the map is not ready the CameraUpdateFactory cannot be used. This should be called on
     * all entry points that call methods on the Google Maps API.
     */
    private boolean checkReady() {
        if (mMap == null) {
            Toast.makeText(this, R.string.map_not_ready, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onLocationUpdate(Location location) {
        goToPosition(location);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
