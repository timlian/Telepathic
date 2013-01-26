
package com.telepathic.finder.app;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.telepathic.finder.R;
import com.telepathic.finder.util.Utils;
import com.telepathic.finder.view.HorizontialListView;

public class BusStationActivity extends Activity {
    private EditText mEtStationId;
    private Button mBtnFindStation;
    private HorizontialListView mHlvBusLine;
    private BusLineAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bus_station);
        Utils.copyAppDatabaseFiles(getPackageName());
        setupView();
    }

    private void setupView(){
        mHlvBusLine = (HorizontialListView)findViewById(R.id.bus_line_list);
        mAdapter = new BusLineAdapter();
        mHlvBusLine.setAdapter(mAdapter);
        mEtStationId = (EditText)findViewById(R.id.station_id);
        mBtnFindStation = (Button)findViewById(R.id.find_bus_station);
    }

    public void onFindBusStationClicked(View v) {
        if (mBtnFindStation.equals(v)) {
            mAdapter.setGPSNumber(mEtStationId.getText().toString());
        }
    }
    private class BusLineAdapter extends BaseAdapter {
        private ArrayList<BusAndStationInfo> mBusAndStationList;
        //        private Adapter mAdapter;

        public BusLineAdapter() {
            mBusAndStationList = new ArrayList<BusAndStationInfo>();
        }
        public void setGPSNumber(String GPSNumber){
            ArrayList<String> busLines = getBusLines(GPSNumber);
            if (busLines != null && busLines.size()>0){
                for (String busNumber : busLines) {
                    mBusAndStationList.add(new BusAndStationInfo(busNumber, GPSNumber));
                }
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mBusAndStationList.size();
        }

        @Override
        public Object getItem(int arg0) {
            return mBusAndStationList.get(arg0);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            BusAndStationInfo busAndStation = mBusAndStationList.get(position);
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.bus_station_item, parent, false);
                BusInfoHolder holder = new BusInfoHolder();
                holder.tvBusNumber = (TextView)convertView.findViewById(R.id.bus_number);
                holder.tvStartTime = (TextView)convertView.findViewById(R.id.start_time);
                holder.tvEndTime = (TextView)convertView.findViewById(R.id.end_time);
                holder.lvStationNameList = (ListView)convertView.findViewById(R.id.station_name_list);
                convertView.setTag(holder);
            }
            bindView(busAndStation, convertView);
            return convertView;
        }

        private void bindView(BusAndStationInfo busAndStation, View view) {
            BusInfoHolder holder = (BusInfoHolder)view.getTag();
            holder.tvBusNumber.setText(busAndStation.getBusNumber());
            holder.tvStartTime.setText(getResources().getString(R.string.start_time));
            holder.tvEndTime.setText(getResources().getString(R.string.end_time));
            //            holder.lvStationNameList.setAdapter(new adapter(busAndStation.getBusNumber(), listener));
        }

        private ArrayList<String> getBusLines(String GpsNumber){
            ArrayList<String> busLines = new ArrayList<String>();
            //TODO: Get all bus lines by station gps number
            // Test data start
            if(GpsNumber.equals("50020")){
                for (String bus: testBusLines) {
                    busLines.add(bus);
                }
            }
            // Test data end
            return busLines;
        }

        private class BusAndStationInfo{
            private String busNumber;
            private String stationGpsNumber;

            public BusAndStationInfo(String busNumber, String stationGpsnumber) {
                this.busNumber = busNumber;
                this.stationGpsNumber = stationGpsnumber;
            }

            public String getBusNumber() {
                return busNumber;
            }

            public String getStationGpsNumber() {
                return stationGpsNumber;
            }

        }

    }

    private static class BusInfoHolder {
        TextView tvBusNumber;
        TextView tvStartTime;
        TextView tvEndTime;
        ListView lvStationNameList;
    }

    //TODO: Some test data, need to remove

    String[] testBusLines = new String[]{"501", "504"};

}
