package com.telepathic.finder.app;

import java.util.ArrayList;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import cn.domob.android.ads.DomobAdEventListener;
import cn.domob.android.ads.DomobAdManager.ErrorCode;
import cn.domob.android.ads.DomobAdView;
import cn.domob.android.ads.DomobUpdater;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.telepathic.finder.R;
import com.telepathic.finder.util.UmengEvent;
import com.umeng.analytics.MobclickAgent;

public class MainActivity extends SherlockFragmentActivity {

    private static final String TAG_LOCATION_FRAGMENT      = "Location";
    private static final String TAG_CARD_INFO_FRAGMENT     = "cardInfo";
    private static final String TAG_STATION_LINES_FRAGMENT = "stationLines";
    private static final String TAG_TRANSFER_FRAGMENT      = "Transfer";
    private static final String TAG_BUS_LINE_FRAGMENT      = "busLine";

    private static final String UMENG_PARAM_KEY = "NEED_UPDATE";

    private RelativeLayout mAdContainer;

    private LinearLayout mTabLocation;
    private LinearLayout mTabCard;
    private LinearLayout mTabStation;
    private LinearLayout mTabTransfer;
    private LinearLayout mTabBusLine;
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
        mTabTransfer = (LinearLayout)findViewById(R.id.tab_transfer);
        mTabBusLine = (LinearLayout)findViewById(R.id.tab_bus_line);

        initSwitchHandlers();
        setupAdView();
        navigateToLocation(mTabLocation);

        String needUpdate = MobclickAgent.getConfigParams(this, UMENG_PARAM_KEY);

        if (needUpdate.equalsIgnoreCase(Boolean.toString(true))) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    // Use Domob SDK to check update
                    DomobUpdater.checkUpdate(MainActivity.this, getString(R.string.publisher_id));
                }
            }, 3000);
        }
    }

    private void setupAdView() {
        mAdContainer = (RelativeLayout)findViewById(R.id.ad_container);
        DomobAdView adView320x50 = new DomobAdView(this, getString(R.string.publisher_id), getString(R.string.InlinePPID), DomobAdView.INLINE_SIZE_320X50);
        adView320x50.setAdEventListener(new DomobAdEventListener() {

            @Override
            public void onDomobLeaveApplication(DomobAdView arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onDomobAdReturned(DomobAdView arg0) {
                MobclickAgent.onEvent(MainActivity.this, UmengEvent.OTHER_AD_SHOW);
            }

            @Override
            public Context onDomobAdRequiresCurrentContext() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void onDomobAdOverlayPresented(DomobAdView arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onDomobAdOverlayDismissed(DomobAdView arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onDomobAdFailed(DomobAdView arg0, ErrorCode arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onDomobAdClicked(DomobAdView arg0) {
                MobclickAgent.onEvent(MainActivity.this, UmengEvent.OTHER_AD_CLICK);
            }
        });
        adView320x50.setKeyword("traffic");
        mAdContainer.addView(adView320x50);
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

    public void navigateToTransfer(View v) {
        setSelected(v);
        mActionBar.setTitle(R.string.bus_transfer);
        showFragment(TAG_TRANSFER_FRAGMENT);
    }

    public void navigateToBusLine(View v) {
        setSelected(v);
        mActionBar.setTitle(R.string.bus_line);
        showFragment(TAG_BUS_LINE_FRAGMENT);
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
        mTabTransfer.setSelected(false);
        mTabBusLine.setSelected(false);
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

        mSwitchHandlers.add(new SwitchHandler() {
            @Override
            public void onSwitch(String tag) {
                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                Fragment transferFragment = mFragmentManager.findFragmentByTag(TAG_TRANSFER_FRAGMENT);
                if (TAG_TRANSFER_FRAGMENT.equals(tag)) {
                    if (transferFragment == null) {
                        transferFragment = new BusTransferFragment();
                        transaction.add(R.id.fragment_container, transferFragment, TAG_TRANSFER_FRAGMENT);
                    } else {
                        transaction.show(transferFragment);
                    }
                } else {
                    if (transferFragment != null) {
                        transaction.hide(transferFragment);
                    }
                }
                transaction.commit();
            }
        });

        mSwitchHandlers.add(new SwitchHandler() {
            @Override
            public void onSwitch(String tag) {
                FragmentTransaction transaction = mFragmentManager.beginTransaction();
                Fragment transferFragment = mFragmentManager.findFragmentByTag(TAG_BUS_LINE_FRAGMENT);
                if (TAG_BUS_LINE_FRAGMENT.equals(tag)) {
                    if (transferFragment == null) {
                        transferFragment = new BusLineFragment();
                        transaction.add(R.id.fragment_container, transferFragment, TAG_BUS_LINE_FRAGMENT);
                    } else {
                        transaction.show(transferFragment);
                    }
                } else {
                    if (transferFragment != null) {
                        transaction.hide(transferFragment);
                    }
                }
                transaction.commit();
            }
        });
    }

    private void showFragment(String tag) {
        supportInvalidateOptionsMenu();
        for(SwitchHandler handler : mSwitchHandlers) {
            handler.onSwitch(tag);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

}
