package com.telepathic.finder.app;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
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

        actionBar.addTab(actionBar.newTab().setText("locations").setTabListener(new BusLocationTabListener(new BusLocationFragment())));
        actionBar.addTab(actionBar.newTab().setText("records").setTabListener(new BusCardRecordTabListener(new BusCardRecordFragment())));
        actionBar.addTab(actionBar.newTab().setText("stations").setTabListener(new BusStationTabListener(new BusStationFragment())));
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);

        // Get the SearchView and set the searchable configuration
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setQueryHint("find something");
        searchView.setInputType(InputType.TYPE_CLASS_TEXT);
        searchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                //                mTvSearchKey.setText(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // TODO Auto-generated method stub
                return false;
            }
        });
        return true;
    }

    private class BusLocationTabListener implements ActionBar.TabListener {
        private BusLocationFragment mFragment;

        public BusLocationTabListener(BusLocationFragment fragment) {
            mFragment = fragment;
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

    private class BusCardRecordTabListener implements ActionBar.TabListener {
        private BusCardRecordFragment mFragment;

        public BusCardRecordTabListener(BusCardRecordFragment fragment) {
            mFragment = fragment;
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

    private class BusStationTabListener implements ActionBar.TabListener {
        private BusStationFragment mFragment;

        public BusStationTabListener(BusStationFragment fragment) {
            mFragment = fragment;
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
