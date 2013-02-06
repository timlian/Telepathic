
package com.telepathic.finder.app;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.telepathic.finder.R;
import com.telepathic.finder.sdk.traffic.BusLineInfo;
import com.telepathic.finder.sdk.traffic.BusLineInfo.Direction;
import com.telepathic.finder.util.Utils;

public class BusStationActivity extends Activity {
    private EditText mEtStationId;
    private Button mBtnFindStation;
    private LinearLayout mLlBusLines;
    private TextView mTvStationName;
    private LinearLayout mLlNoItem;
    private LinearLayout mLlStationInfo;
    private BusAndStationInfo mBusAndStationInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bus_station);
        Utils.copyAppDatabaseFiles(getPackageName());
        setupView();
    }

    private void setupView(){
        mLlBusLines = (LinearLayout)findViewById(R.id.bus_line_list);
        mTvStationName = (TextView)findViewById(R.id.bus_station_name);
        mEtStationId = (EditText)findViewById(R.id.station_id);
        mBtnFindStation = (Button)findViewById(R.id.find_bus_station);
        mLlNoItem = (LinearLayout)findViewById(R.id.no_item_tips);
        mLlStationInfo = (LinearLayout)findViewById(R.id.station_info);
    }

    public void onFindBusStationClicked(View v) {
        if (mBtnFindStation.equals(v)) {
            initBusLines(mEtStationId.getText().toString());
            Utils.hideSoftKeyboard(getApplicationContext(), mEtStationId);
        }
    }

    private void initBusLines(String GPSNumber) {
        ArrayList<String> busLines = getBusLines(GPSNumber);
        String stationName = getStationName(GPSNumber);
        mBusAndStationInfo = new BusAndStationInfo(GPSNumber, stationName, busLines);
        mTvStationName.setText(mBusAndStationInfo.getStationName());

        if(mBusAndStationInfo != null && mBusAndStationInfo.getBusLines().size() > 0) {
            mLlBusLines.removeAllViews();
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            int dip5 = Utils.dip2px(this, 5);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dm.widthPixels / 2 - (2*dip5), LayoutParams.MATCH_PARENT);
            lp.setMargins(dip5, dip5, dip5, dip5);
            for (int index = 0; index < mBusAndStationInfo.getBusLines().size(); index++) {
                mLlBusLines.addView(getView(index, mBusAndStationInfo), lp);
                mLlBusLines.addView(getView(index, mBusAndStationInfo), lp);
            }
            mLlNoItem.setVisibility(View.GONE);
            mLlStationInfo.setVisibility(View.VISIBLE);
        }
    }

    private class BusAndStationInfo{
        private String stationName;
        private ArrayList<String> busLines;
        private String stationGpsNumber;

        public BusAndStationInfo(String gpsNumber, String stationName, ArrayList<String> busLines) {
            this.stationGpsNumber = gpsNumber;
            this.stationName = stationName;
            this.busLines = busLines;
        }

        public String getStationName() {
            return stationName;
        }

        public String getStationGpsNumber() {
            return stationGpsNumber;
        }

        public ArrayList<String> getBusLines() {
            return busLines;
        }

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

    private String getStationName(String GPSNumber) {
        String stationName = "";
        //TODO: Get the station name by gps number
        // Test data start
        if(GPSNumber.equals("50020")) {
            stationName = testStationName;
        }
        // Test data end
        return stationName;
    }

    private BusLineInfo getBusLineInfo(String busNumber) {
        BusLineInfo busLineInfo = null;
        //TODO: Get the bus line info by bus number
        // Test data start
        if (busNumber.equals("504")) {
            HashMap<Direction,ArrayList<String>> busStations = new HashMap<Direction, ArrayList<String>>();
            ArrayList<String> stations = new ArrayList<String>();
            for(String station : testBus504Down) {
                stations.add(station);
            }
            busStations.put(Direction.DOWN, stations);
            busLineInfo = new BusLineInfo(busNumber, testBus504Time[0], testBus504Time[1], busStations);
        } else if (busNumber.equals("501")) {
            HashMap<Direction,ArrayList<String>> busStations = new HashMap<Direction, ArrayList<String>>();
            ArrayList<String> stations = new ArrayList<String>();
            for(String station : testBus501Up) {
                stations.add(station);
            }
            busStations.put(Direction.UP, stations);
            busLineInfo = new BusLineInfo(busNumber, testBus501Time[0], testBus501Time[1], busStations);
        }
        // Test data end
        return busLineInfo;
    }

    private Direction getDirection(String gpsNumber, String busNumber) {
        //TODO: Get the bus direction on this station
        // Test data start
        if (gpsNumber.equals("50020") && busNumber.equals("504")) {
            return Direction.DOWN;
        } else if (gpsNumber.equals("50020") && busNumber.equals("501")) {
            return Direction.UP;
        }
        return null;
        // Test data end
    }

    public View getView(int position, BusAndStationInfo info) {
        String busNumber = info.getBusLines().get(position);
        View convertView = getLayoutInflater().inflate(R.layout.bus_station_item, null);
        BusInfoHolder holder = new BusInfoHolder();
        holder.tvBusNumber = (TextView)convertView.findViewById(R.id.bus_number);
        holder.tvStartingTime = (TextView)convertView.findViewById(R.id.starting_time);
        holder.tvEndingTime = (TextView)convertView.findViewById(R.id.ending_time);
        holder.lvStationNameList = (ListView)convertView.findViewById(R.id.station_name_list);
        convertView.setTag(holder);
        BusLineInfo busLineInfo = getBusLineInfo(busNumber);
        bindView(busLineInfo, convertView, info);
        return convertView;
    }

    private void bindView(BusLineInfo busLineInfo, View view, BusAndStationInfo busAndStationInfo) {
        BusInfoHolder holder = (BusInfoHolder)view.getTag();
        holder.tvBusNumber.setText(busLineInfo.getBusNumber());
        holder.tvStartingTime.setText(getString(R.string.starting_time, busLineInfo.getStartingTime()));
        holder.tvEndingTime.setText(getString(R.string.ending_time, busLineInfo.getEndingTime()));
        holder.lvStationNameList.setAdapter(new StationsAdapter(busLineInfo.getBusStationsByDirection(getDirection(busAndStationInfo.stationGpsNumber,busLineInfo.getBusNumber()))));
    }

    private class StationsAdapter extends BaseAdapter {
        private ArrayList<String> mBusStations;

        public StationsAdapter(ArrayList<String> busStations) {
            this.mBusStations = busStations;
        }

        @Override
        public int getCount() {
            return mBusStations.size();
        }

        @Override
        public Object getItem(int position) {
            return mBusStations.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String busStation = mBusStations.get(position);
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.bus_station_list_item, null);
                StationNameHolder holder = new StationNameHolder();
                holder.tvBusStationName = (TextView)convertView.findViewById(R.id.bus_station_name);
                convertView.setTag(holder);
            }
            bindView(busStation, convertView);
            return convertView;
        }

        private void bindView(String busStation, View view) {
            StationNameHolder holder = (StationNameHolder)view.getTag();
            holder.tvBusStationName.setText(busStation);
            if (busStation.equals(mBusAndStationInfo.stationName)) {
                holder.tvBusStationName.setTextColor(Color.RED);
            } else {
                holder.tvBusStationName.setTextColor(Color.BLACK);
            }
        }

    }

    private static class StationNameHolder {
        TextView tvBusStationName;
    }

    private static class BusInfoHolder {
        TextView tvBusNumber;
        TextView tvStartingTime;
        TextView tvEndingTime;
        ListView lvStationNameList;
    }

    //TODO: Some test data, need to remove

    String[] testBusLines = new String[]{"501", "504"};
    String testStationName = "天府大道天华二路口站";
    String[] testBus504Time = {"6:30","19:30"};
    String[] testBus501Time = {"6:20","21:00"};
    String[] testBus504Down = {"华阳新希望大道站","音乐花园站","华阳大道一段站",
            "华阳绕城路口站","天府大道中段南站","天府大道天华二路口站","天府大道天华一路口站",
            "新会展中心南侧站","新会展中心东侧站","新会展中心北侧站","成都出口加工区站",
            "天府大道北段站","名都公园站","天府长城站","人南立交桥南站","人南立交桥北站",
            "倪家桥站","人民南路四段北站","省体育馆站","林荫街站","林荫中街站","磨子桥站"};
    String[] testBus501Up = {"华阳客运站","二江寺站","南湖公园站",
            "南阳盛世站","输气大厦站","老码头站","正北横街站","龙灯路口站",
            "七里村站","四合村站","华阳绕城路口站","天府大道中段南站",
            "天府大道天华二路口站","天府大道天华一路口站","天府大道中段中站",
            "地铁世纪城站","成都出口加工区站","天府大道北段站","名都公园站","府城大道中段中站",
            "益州大道府城大道口站","益州大道北段站","新南天地站","火车南站西路中站",
            "火车南站","航空路站","新希望路中站","棕树十街坊站","长荣路站","高攀路站","桂溪公交站"};

}
