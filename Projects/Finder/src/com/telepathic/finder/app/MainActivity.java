package com.telepathic.finder.app;

import java.util.ArrayList;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.telepathic.finder.R;

public class MainActivity extends SherlockFragmentActivity {

    private static final String TAG_LOCATION_FRAGMENT      = "Location";
    private static final String TAG_CARD_INFO_FRAGMENT     = "cardInfo";
    private static final String TAG_STATION_LINES_FRAGMENT = "stationLines";

    private LinearLayout mTabLocation;
    private LinearLayout mTabCard;
    private LinearLayout mTabStation;
    private ActionBar mActionBar;
    private Dialog mExitDialog;
    
    private FragmentManager mFragmentManager;
    
    private ArrayList<SwitchHandler> mSwitchHandlers = new ArrayList<SwitchHandler>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActionBar = getSupportActionBar();
        mFragmentManager = getSupportFragmentManager();
        
        mTabLocation = (LinearLayout)findViewById(R.id.tab_location);
        mTabCard = (LinearLayout)findViewById(R.id.tab_card);
        mTabStation = (LinearLayout)findViewById(R.id.tab_station);

        initSwitchHandlers();
        navigateToLocation(mTabLocation);
    }

	public void navigateToLocation(View v) {
		setSelected(v);
		mActionBar.setTitle(R.string.bus_location);
		showFragment(TAG_LOCATION_FRAGMENT);
	}

    public void navigateToCardInfo(View v) {
        setSelected(v);
        mActionBar.setTitle(R.string.card_records);
        showFragment(TAG_CARD_INFO_FRAGMENT);
    }

    public void navigateToStationLines(View v) {
        setSelected(v);
        mActionBar.setTitle(R.string.bus_stations);
        showFragment(TAG_STATION_LINES_FRAGMENT);
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

    private void setSelected(View v) {
        mTabLocation.setSelected(false);
        mTabCard.setSelected(false);
        mTabStation.setSelected(false);
        v.setSelected(true);
    }

    @Override
    public void onBackPressed() {
        if (mExitDialog == null) {
            mExitDialog = createDialog();
        }
        mExitDialog.show();
    }
    
    private interface SwitchHandler {
    	/**
    	 * Called when the fragment switch
    	 * @param tag 
    	 */
    	void onSwitch(String tag);
    }
    
    private void initSwitchHandlers() {
    	mSwitchHandlers.add(new SwitchHandler() {
			@Override
			public void onSwitch(String tag) {
				FragmentTransaction transaction = mFragmentManager.beginTransaction();
				Fragment locationFragment = mFragmentManager.findFragmentByTag(TAG_LOCATION_FRAGMENT);
				if (TAG_LOCATION_FRAGMENT.equals(tag)) {
					if (locationFragment == null) {
						locationFragment = new BusLocationFragment();
						transaction.add(R.id.fragment_container, locationFragment, TAG_LOCATION_FRAGMENT);
					} else {
						transaction.show(locationFragment);
					}
				} else {
					if (locationFragment != null) {
						transaction.hide(locationFragment);
					}
				}
				transaction.commit();
			}
		});
        
        mSwitchHandlers.add(new SwitchHandler() {
			@Override
			public void onSwitch(String tag) {
				FragmentTransaction transaction = mFragmentManager.beginTransaction();
				Fragment cardRecordsFragment = mFragmentManager.findFragmentByTag(TAG_CARD_INFO_FRAGMENT);
				if (TAG_CARD_INFO_FRAGMENT.equals(tag)) {
					if (cardRecordsFragment == null) {
						cardRecordsFragment = new BusCardRecordFragment();
						transaction.add(R.id.fragment_container, cardRecordsFragment, TAG_CARD_INFO_FRAGMENT);
					} else {
						transaction.show(cardRecordsFragment);
					}
				} else {
					if (cardRecordsFragment != null) {
						transaction.hide(cardRecordsFragment);
					}
				}
				transaction.commit();
			}
		});
        
        mSwitchHandlers.add(new SwitchHandler() {
			@Override
			public void onSwitch(String tag) {
				FragmentTransaction transaction = mFragmentManager.beginTransaction();
				Fragment stationLinesFragment = mFragmentManager.findFragmentByTag(TAG_STATION_LINES_FRAGMENT);
				if (TAG_STATION_LINES_FRAGMENT.equals(tag)) {
					if (stationLinesFragment == null) {
						stationLinesFragment = new BusStationFragment();
						transaction.add(R.id.fragment_container, stationLinesFragment, TAG_STATION_LINES_FRAGMENT);
					} else {
						transaction.show(stationLinesFragment);
					}
				} else {
					if (stationLinesFragment != null) {
						transaction.hide(stationLinesFragment);
					}
				}
				transaction.commit();
			}
		});
    }
    
    private void showFragment(String tag) {
    	for(SwitchHandler handler : mSwitchHandlers) {
    		handler.onSwitch(tag);
    	}
    }
    
}
