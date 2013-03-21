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
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.telepathic.finder.R;
import com.telepathic.finder.util.Utils;

public class MainActivity extends SherlockFragmentActivity {
    private static final String TAG = "MainActivity";

    private static final String TAG_LOCATION_FRAGMENT      = "Location";
    private static final String TAG_CARD_INFO_FRAGMENT     = "cardInfo";
    private static final String TAG_STATION_LINES_FRAGMENT = "stationLines";

    private LinearLayout mTabLocation, mTabCard, mTabStation;

    private Dialog mExitDialog;

    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTabLocation = (LinearLayout)findViewById(R.id.tab_location);
        mTabCard = (LinearLayout)findViewById(R.id.tab_card);
        mTabStation = (LinearLayout)findViewById(R.id.tab_station);

        mFragmentManager = getSupportFragmentManager();
        Fragment locationFragment = new BusLocationFragment();
        Fragment cardFragment = new BusCardRecordFragment();
        Fragment stationFragment = new BusStationFragment();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.add(R.id.fragment_container, stationFragment, TAG_STATION_LINES_FRAGMENT);
        transaction.add(R.id.fragment_container, cardFragment, TAG_CARD_INFO_FRAGMENT);
        transaction.add(R.id.fragment_container, locationFragment, TAG_LOCATION_FRAGMENT);
        transaction.hide(cardFragment);
        transaction.hide(stationFragment);
        transaction.show(locationFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        mTabLocation.setSelected(true);
    }

    public void navigateToLocation(View v) {
        setSelected(v);
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        Fragment fragment1 = mFragmentManager.findFragmentByTag(TAG_LOCATION_FRAGMENT);
        Fragment fragment2 = mFragmentManager.findFragmentByTag(TAG_CARD_INFO_FRAGMENT);
        Fragment fragment3 = mFragmentManager.findFragmentByTag(TAG_STATION_LINES_FRAGMENT);
        transaction.hide(fragment2);
        transaction.hide(fragment3);
        transaction.show(fragment1);
        transaction.addToBackStack(TAG_LOCATION_FRAGMENT);
        transaction.commit();
    }

    public void navigateToCardInfo(View v) {
        setSelected(v);
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        Fragment fragment1 = mFragmentManager.findFragmentByTag(TAG_LOCATION_FRAGMENT);
        Fragment fragment2 = mFragmentManager.findFragmentByTag(TAG_CARD_INFO_FRAGMENT);
        Fragment fragment3 = mFragmentManager.findFragmentByTag(TAG_STATION_LINES_FRAGMENT);
        transaction.hide(fragment1);
        transaction.hide(fragment3);
        transaction.show(fragment2);
        transaction.addToBackStack(TAG_LOCATION_FRAGMENT);
        transaction.commit();
    }

    public void navigateToStationLines(View v) {
        setSelected(v);
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        Fragment fragment1 = mFragmentManager.findFragmentByTag(TAG_LOCATION_FRAGMENT);
        Fragment fragment2 = mFragmentManager.findFragmentByTag(TAG_CARD_INFO_FRAGMENT);
        Fragment fragment3 = mFragmentManager.findFragmentByTag(TAG_STATION_LINES_FRAGMENT);
        transaction.hide(fragment1);
        transaction.hide(fragment2);
        transaction.show(fragment3);
        transaction.addToBackStack(TAG_LOCATION_FRAGMENT);
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

    private void setSelected(View v) {
        mTabLocation.setSelected(false);
        mTabCard.setSelected(false);
        mTabStation.setSelected(false);
        v.setSelected(true);
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
