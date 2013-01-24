
package com.telepathic.finder.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.telepathic.finder.R;
import com.telepathic.finder.view.HorizontialListView;

public class BusStationActivity extends Activity {
    private HorizontialListView mBusLineList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bus_station);
        setupView();
    }

    private void setupView(){
        mBusLineList = (HorizontialListView)findViewById(R.id.bus_line_list);
        mBusLineList.setAdapter(new BusLineAdapter());
    }

    private class BusLineAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            return null;
        }

    }

}
