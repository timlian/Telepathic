
package com.telepathic.finder.app;

import java.util.ArrayList;

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
import com.telepathic.finder.sdk.ITrafficService;
import com.telepathic.finder.sdk.traffic.entity.BusLine;
import com.telepathic.finder.sdk.traffic.entity.BusStation;
import com.telepathic.finder.sdk.traffic.entity.BusLine.Direction;
import com.telepathic.finder.util.Utils;

public class BusStationActivity extends Activity {
    private EditText mEtStationId;
    private Button mBtnFindStation;
    private LinearLayout mLlBusLines;
    private TextView mTvStationName;
    private LinearLayout mLlNoItem;
    private LinearLayout mLlStationInfo;
    private BusAndStationInfo mBusAndStationInfo;
    private ITrafficService mTrafficService;
    private MessageDispatcher mMessageDispatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bus_station);
        Utils.copyAppDatabaseFiles(getPackageName());
        setupView();
        FinderApplication app = (FinderApplication)getApplication();
        mTrafficService = app.getTrafficService();
        mMessageDispatcher = app.getMessageDispatcher();
        mMessageDispatcher.add(null);
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
            if (mEtStationId.getText().toString().equals("111")) {
            	Utils.copyAppDatabaseFiles(getPackageName());
            } else if (mEtStationId.getText().toString().equals("222")){
            	mTrafficService.translateToStation("");
            }
            else {
            	test();
            }
        }
    }

    private void test() {
    	//mTrafficService.getBusStationLines("50022");
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

    private BusLine getBusLineInfo(String busNumber) {
        BusLine busLine = null;
        if (busNumber.equals("504")) {
            ArrayList<BusStation> route = new ArrayList<BusStation>();
            for(String station : testBus504Down) {
                route.add(new BusStation(station));
            }
            busLine = new BusLine();
            busLine.setLineNumber(busNumber);
            busLine.setStartTime(testBus504Time[0]);
            busLine.setEndTime(testBus504Time[1]);
            busLine.addRoute(Direction.DOWN, route);
        } else if (busNumber.equals("501")) {
        	ArrayList<BusStation> route = new ArrayList<BusStation>();
            for(String station : testBus501Up) {
                route.add(new BusStation(station));
            }
            busLine = new BusLine();
            busLine.setLineNumber(busNumber);
            busLine.setStartTime(testBus501Time[0]);
            busLine.setEndTime(testBus501Time[1]);
            busLine.addRoute(Direction.UP, route);
        }
        // Test data end
        return busLine;
    }

    private Direction getDirection(String gpsNumber) {
        //TODO: Get the bus direction on this station
        // Test data start
        if (gpsNumber.equals("50020")) {
            return Direction.DOWN;
        } else if (gpsNumber.equals("50020")) {
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
        BusLine busLineInfo = getBusLineInfo(busNumber);
        bindView(busLineInfo, convertView, info);
        return convertView;
    }

    private void bindView(BusLine busLine, View view, BusAndStationInfo busAndStationInfo) {
        BusInfoHolder holder = (BusInfoHolder)view.getTag();
        holder.tvBusNumber.setText(busLine.getLineNumber());
        holder.tvStartingTime.setText(getString(R.string.starting_time, busLine.getStartTime()));
        holder.tvEndingTime.setText(getString(R.string.ending_time, busLine.getEndTime()));
        ArrayList<BusStation> route = busLine.getRoute(Direction.UP);
        if (route == null) {
        	route = busLine.getRoute(Direction.DOWN);
        }
        if (route != null) {
        	holder.lvStationNameList.setAdapter(new StationsAdapter(route));
        }
    }

    private class StationsAdapter extends BaseAdapter {
        private ArrayList<BusStation> mBusStations;

        public StationsAdapter(ArrayList<BusStation> busStations) {
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
            String busStation = mBusStations.get(position).getName();
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
