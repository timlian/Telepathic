package com.telepathic.finder.app;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.telepathic.finder.R;
import com.telepathic.finder.util.Utils;

public class MainActivity extends SherlockFragmentActivity {
	private static final String TAG = "MainActivity";
	
    private static final String TAG_LOCATION_FRAGMENT      = "Location";
    private static final String TAG_CARD_INFO_FRAGMENT     = "cardInfo";
    private static final String TAG_STATION_LINES_FRAGMENT = "stationLines";
    
    private Dialog mExitDialog;
    
    private Fragment mLocationFragment = new BusLocationFragment();
    private Fragment mCardFragment = new BusCardRecordFragment();
    private Fragment mStationFragment = new BusStationFragment();
    private FragmentManager mFragmentManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mFragmentManager = getSupportFragmentManager();
        
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, mLocationFragment);
        fragmentTransaction.commit();
    }

    public void navigateToLocation(View v) {
    	FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, mLocationFragment);
        transaction.addToBackStack(TAG_LOCATION_FRAGMENT);
        transaction.commit();
    }
    
    public void navigateToCardInfo(View v) {
    	View mapView = mLocationFragment.getView();
		if (mapView != null) {
			mapView.setVisibility(View.INVISIBLE);
		}
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, mCardFragment);
        transaction.addToBackStack(TAG_CARD_INFO_FRAGMENT);
        transaction.commit();
    }
    
    public void navigateToStationLines(View v) {
    	View mapView = mLocationFragment.getView();
		if (mapView != null) {
			mapView.setVisibility(View.INVISIBLE);
		}
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, mStationFragment);
        transaction.addToBackStack(TAG_STATION_LINES_FRAGMENT);
        transaction.commit();
    }
    
    private Dialog createDialog() {
    	 Builder exitDlgBuilder = new Builder(this)
         .setTitle(R.string.confirm_exit_title)
         .setMessage(R.string.confirm_exit_message)
         .setPositiveButton(android.R.string.ok, new OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 MainActivity.this.finish();
             }
         })
         .setNegativeButton(android.R.string.cancel, null);
         return exitDlgBuilder.create();
    }

    @Override
    public void onBackPressed() {
    	if (mFragmentManager.getBackStackEntryCount() > 0) {
    		super.onBackPressed();
    	} else {
    		if (mExitDialog == null) {
        		mExitDialog = createDialog();
    		}
        	mExitDialog.show();
    	}
    }
    
    private void debugStackEntry() {
    	int count = mFragmentManager.getBackStackEntryCount();
    	if (count == 0) {
    		Utils.debug(TAG, "Empty stack.");
    	}
    	for(int i = 0; i < count; i++) {
    		Utils.debug(TAG, "#" + i + ": " + mFragmentManager.getBackStackEntryAt(i).getName());
    		//mFragmentManager.
    	}
    }
    
}
