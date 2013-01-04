
package com.telepathic.finder.app;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.telepathic.finder.R;

public class TabHomeActivity extends TabActivity {

    private TabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_home);
        mTabHost = getTabHost();

        setupTabWidget();
    }

    private void addTab(String tag, Class clazz){
        Intent intent = new Intent();
        intent.setClass(TabHomeActivity.this, clazz);

        TabSpec spec = mTabHost.newTabSpec(tag);
        View v = LayoutInflater.from(this).inflate(R.layout.tab_widget_layout, null);
        ((TextView)v.findViewById(R.id.tab_name)).setText(tag);
        spec.setIndicator(v);
        //        spec.setIndicator(tag, d);
        spec.setContent(intent);
        mTabHost.addTab(spec);
    }

    private void setupTabWidget(){
        addTab(getString(R.string.bus_location), BusLocationActivity.class);
        addTab(getString(R.string.bus_test), ConsumerRecordsActivity.class);
    }

}
