package com.telepathic.finder.app;

import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost.TabSpec;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.telepathic.finder.R;

public class MainActivity extends SherlockFragmentActivity {

    //    private TextView mTvSearchKey;
    private FragmentTabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupView();
    }

    private void setupView(){
        //        mTvSearchKey = (TextView) findViewById(R.id.search_key);
        mTabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        addTab(getString(R.string.bus_location), BusLocationFragment.class, R.drawable.ic_tab_location);
        addTab(getString(R.string.card_records), BusCardRecordFragment.class, R.drawable.ic_tab_card);
        addTab(getString(R.string.bus_stations), BusStationFragment.class, R.drawable.ic_tab_station);

    }

    private void addTab(String tag, Class<?> clazz, int iconResId) {
        TabSpec spec = mTabHost.newTabSpec(tag);
        View v = LayoutInflater.from(this).inflate(R.layout.tab_host_spec, null);
        ((ImageView)v.findViewById(R.id.tab_name)).setImageResource(iconResId);
        spec.setIndicator(v);
        mTabHost.addTab(spec, clazz, null);
    }

}
