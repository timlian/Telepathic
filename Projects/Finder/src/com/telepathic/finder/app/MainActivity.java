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
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.domob.android.ads.DomobAdEventListener;
import cn.domob.android.ads.DomobAdManager.ErrorCode;
import cn.domob.android.ads.DomobAdView;
import cn.domob.android.ads.DomobUpdater;

import com.telepathic.finder.R;
import com.telepathic.finder.util.UmengEvent;
import com.umeng.analytics.MobclickAgent;

public class MainActivity extends ActionBarActivity {

    private static final String TAG_LOCATION_FRAGMENT      = "Location";
    private static final String TAG_CARD_INFO_FRAGMENT     = "cardInfo";
    private static final String TAG_STATION_LINES_FRAGMENT = "stationLines";
    private static final String TAG_TRANSFER_FRAGMENT      = "Transfer";
    private static final String TAG_BUS_LINE_FRAGMENT      = "busLine";

    private static final String UMENG_PARAM_KEY = "NEED_UPDATE";

    private RelativeLayout mAdContainer;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private String[] mDrawerTitles;
    private int[] mDrawerIcons;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private LinearLayout mTabLocation;
    private LinearLayout mTabCard;
    private LinearLayout mTabStation;
    private LinearLayout mTabTransfer;
    private LinearLayout mTabBusLine;
    private ActionBar mActionBar;
    private Dialog mExitDialog;
    private int mPostion = 0;
    private int mPostionTemp = 0;

    private final int INVALID_POSITION = -1;

    private FragmentManager mFragmentManager;

    private boolean mIsQuitApp = false;

    private ArrayList<SwitchHandler> mSwitchHandlers = new ArrayList<SwitchHandler>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeButtonEnabled(true);
        setupDrawerView();
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.ic_navigation_drawer,
                R.string.drawer_open,
                R.string.drawer_close
                ) {
            @Override
            public void onDrawerClosed(View view) {
                mPostion = mPostionTemp;
                mActionBar.setTitle(mTitle);
                supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                mPostion = INVALID_POSITION;
                mActionBar.setTitle(mDrawerTitle);
                supportInvalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mFragmentManager = getSupportFragmentManager();

        mTabLocation = (LinearLayout)findViewById(R.id.tab_location);
        mTabCard = (LinearLayout)findViewById(R.id.tab_card);
        mTabStation = (LinearLayout)findViewById(R.id.tab_station);
        mTabTransfer = (LinearLayout)findViewById(R.id.tab_transfer);
        mTabBusLine = (LinearLayout)findViewById(R.id.tab_bus_line);

        initSwitchHandlers();
        setupAdView();
        if (savedInstanceState == null) {
            selectItem(0);
        }

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

    private void setupDrawerView() {
        mTitle = mDrawerTitle = getTitle();
        mDrawerTitles = getResources().getStringArray(R.array.drawer_arrays);
        mDrawerIcons = new int[]{R.drawable.ic_tab_location_selector,R.drawable.ic_tab_card_selector,R.drawable.ic_tab_station_selector,R.drawable.ic_tab_transfer_selector,R.drawable.ic_tab_line_selector};
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new DrawerAdapter(this, mDrawerIcons, mDrawerTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        mPostionTemp = position;
        switch (position) {
            case 0:
                navigateToLocation(mTabLocation);
                break;
            case 1:
                navigateToCardInfo(mTabCard);
                break;
            case 2:
                navigateToStationLines(mTabStation);
                break;
            case 3:
                navigateToTransfer(mTabTransfer);
                break;
            case 4:
                navigateToBusLine(mTabBusLine);
                break;
            default:
                break;
        }
        mDrawerList.setItemChecked(position, true);
        setTitle(mDrawerTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        mActionBar.setTitle(mTitle);
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
                mIsQuitApp = true;
                MainActivity.this.finish();
                dialog.dismiss();
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

    class DrawerAdapter extends BaseAdapter{
        private LayoutInflater inflater;
        private String[] texts;
        private int[] icons;
        private Context context;

        public DrawerAdapter(Context ctx, int[] icons, String[] texts) {
            super();
            this.texts = texts;
            this.icons = icons;
            this.context = ctx;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return texts.length;
        }

        @Override
        public Object getItem(int position) {
            return texts[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = inflater.inflate(R.layout.drawer_item, null);
            ImageView icon = (ImageView) convertView
                    .findViewById(R.id.icon);
            TextView text = (TextView) convertView
                    .findViewById(R.id.text);
            icon.setBackgroundResource(icons[position]);
            text.setText(texts[position]);
            return convertView;
        }
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mDrawerToggle.onOptionsItemSelected(item)) {
                    return true;
                }
            case R.id.about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);;
        hideMenuItems(menu, !drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    private void hideMenuItems(Menu menu, boolean visible){
        for(int i = 0; i < menu.size(); i++){
            menu.getItem(i).setVisible(visible);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        if (!mIsQuitApp) {
            overridePendingTransition(R.anim.push_right_in, R.anim.back_window_back);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

}
