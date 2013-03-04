package com.telepathic.finder.app;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.telepathic.finder.R;

public class MainActivity extends SherlockFragmentActivity {

    //    private TextView mTvSearchKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupView();
    }

    private void setupView(){
        //        mTvSearchKey = (TextView) findViewById(R.id.search_key);
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        actionBar.addTab(actionBar.newTab().setIcon(R.drawable.ic_tab_location).setTabListener(new BusTabListener<BusLocationFragment>(BusLocationFragment.class)));
        actionBar.addTab(actionBar.newTab().setIcon(R.drawable.ic_tab_card).setTabListener(new BusTabListener<BusCardRecordFragment>(BusCardRecordFragment.class)));
        actionBar.addTab(actionBar.newTab().setIcon(R.drawable.ic_tab_station).setTabListener(new BusTabListener<BusStationFragment>(BusStationFragment.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private class BusTabListener <T extends SherlockFragment> implements ActionBar.TabListener {
        private SherlockFragment mFragment;

        public BusTabListener(Class<T> clazz) {
            mFragment = (SherlockFragment)SherlockFragment.instantiate(MainActivity.this, clazz.getName());
        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            ft = MainActivity.this.getSupportFragmentManager().beginTransaction();
            ft.add(android.R.id.content, mFragment, mFragment.getTag());
            ft.commit();
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            ft = MainActivity.this.getSupportFragmentManager().beginTransaction();
            ft.remove(mFragment);
            ft.commit();
        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
            // TODO Auto-generated method stub

        }

    }

}
