
package com.telepathic.finder.app;

import java.util.ArrayList;
import java.util.StringTokenizer;

import android.R.interpolator;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
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
import android.widget.Toast;

import com.telepathic.finder.R;
import com.telepathic.finder.app.MessageDispatcher.IMessageHandler;
import com.telepathic.finder.sdk.ITrafficService;
import com.telepathic.finder.sdk.ITrafficeMessage;
import com.telepathic.finder.sdk.traffic.entity.BusLine;
import com.telepathic.finder.sdk.traffic.entity.BusStation;
import com.telepathic.finder.sdk.traffic.entity.BusLine.Direction;
import com.telepathic.finder.sdk.traffic.entity.BusStationLines;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData.KuaiXinData;
import com.telepathic.finder.util.Utils;

public class BusStationActivity extends Activity {
	private static final String TAG = "BusStationActivity";
    private EditText mEtStationId;
    private Button mBtnFindStation;
    private LinearLayout mLlBusLines;
    private TextView mTvStationName;
    private LinearLayout mLlNoItem;
    private LinearLayout mLlStationInfo;
    private BusStationLines mStationLines;
    private ITrafficService mTrafficService;
    private MessageDispatcher mMessageDispatcher;

    private IMessageHandler mMessageHandler = new IMessageHandler() {
		@Override
		public int what() {
			return ITrafficeMessage.GET_BUS_STATION_LINES_DONE;
		}
		
		@Override
		public void handleMessage(Message msg) {
			BusStationLines stationLines = (BusStationLines)msg.obj;
			mStationLines = stationLines;
			initBusLines(stationLines);
		}
	};
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bus_station);
        Utils.copyAppDatabaseFiles(getPackageName());
        setupView();
        FinderApplication app = (FinderApplication)getApplication();
        mTrafficService = app.getTrafficService();
        mMessageDispatcher = app.getMessageDispatcher();
        mMessageDispatcher.add(mMessageHandler);
        queryData();
    }

    private void queryData() {
    	BusStationLines stationLines = new BusStationLines();
    	ContentResolver resolver = getContentResolver();
    	StringBuilder sortOrder = new StringBuilder();
    	sortOrder.append(KuaiXinData.BusStation.LAST_UPDATE_TIME)
    			 .append(" DESC ")
    			 .append("LIMIT 0,1");
        Cursor cursor = resolver.query(KuaiXinData.BusStation.CONTENT_URI, null, null, null, sortOrder.toString());
        if (cursor != null && cursor.moveToFirst()) {
        	int idxName = cursor.getColumnIndex(KuaiXinData.BusStation.NAME);
        	int idxGpsNumber = cursor.getColumnIndex(KuaiXinData.BusStation.GPS_NUMBER);
        	String name = cursor.getString(idxName);
        	String gpsNumber = cursor.getString(idxGpsNumber);
        	stationLines.setStationName(name);
        	stationLines.setGpsNumber(gpsNumber);
        	cursor.close();
        	String[] projection = new String[] {
        			KuaiXinData.BusRoute.LINE_NUMBER,
        			KuaiXinData.BusRoute.DIRECTION,
        			KuaiXinData.BusRoute.START_TIME,
        			KuaiXinData.BusRoute.END_TIME,
        			KuaiXinData.BusRoute.STATIONS
        	};
        	String selection = KuaiXinData.BusStation.GPS_NUMBER + "=?";
        	String[] selectionArgs = new String[] { gpsNumber };
        	cursor = resolver.query(KuaiXinData.BusStationLines.CONTENT_URI, projection, selection, selectionArgs, null);
        	if (cursor != null && cursor.moveToFirst()) {
        		int idxLineNumber = cursor.getColumnIndex(KuaiXinData.BusRoute.LINE_NUMBER);
        		int idxDirection = cursor.getColumnIndex(KuaiXinData.BusRoute.DIRECTION);
        		int idxStartTime = cursor.getColumnIndex(KuaiXinData.BusRoute.START_TIME);
        		int idxEndTime = cursor.getColumnIndex(KuaiXinData.BusRoute.END_TIME);
        		int idxStations = cursor.getColumnIndex(KuaiXinData.BusRoute.STATIONS);
        		do {
        			String lineNumber = cursor.getString(idxLineNumber);
        			Direction direction = Direction.fromString(cursor.getString(idxDirection));
        			String startTime = cursor.getString(idxStartTime);
        			String endTime = cursor.getString(idxEndTime);
        			String[] stationNames = cursor.getString(idxStations).split(",");
        			BusLine line = new BusLine();
        			line.setLineNumber(cursor.getString(idxLineNumber));
        			line.setStartTime(startTime);
        			line.setEndTime(endTime);
        			ArrayList<BusStation> route = new ArrayList<BusStation>();
    				for(String station : stationNames) {
    					route.add(new BusStation(station));
    				}
    				line.addRoute(direction, route);
        			stationLines.setBusLine(line);
        			stationLines.setLineRoute(lineNumber, direction);
        		} while(cursor.moveToNext());
        		mStationLines = stationLines;
        		initBusLines(stationLines);
        	}
        	Utils.printCursorContent(TAG, cursor);
        }
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
        if (!mBtnFindStation.equals(v)) {
        	return ;
        }
		String gpsNumber = mEtStationId.getText().toString();
		if (Utils.isValidGpsNumber(gpsNumber)) {
			Utils.hideSoftKeyboard(getApplicationContext(), mEtStationId);
			mTrafficService.getBusStationLines(gpsNumber);
		} else {
			Toast.makeText(this, "invalid gps number: " + gpsNumber, Toast.LENGTH_SHORT).show();
		}
    }

    private void initBusLines(BusStationLines stationLines) {
        mTvStationName.setText(stationLines.getStationName());
        if(stationLines.getBusLines().size() > 0) {
            mLlBusLines.removeAllViews();
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            int dip5 = Utils.dip2px(this, 5);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dm.widthPixels / 2 - (2*dip5), LayoutParams.MATCH_PARENT);
            lp.setMargins(dip5, dip5, dip5, dip5);
            for (int index = 0; index < stationLines.getBusLines().size(); index++) {
                mLlBusLines.addView(getView(index, stationLines), lp);
            }
            mLlNoItem.setVisibility(View.GONE);
            mLlStationInfo.setVisibility(View.VISIBLE);
        }
    }

    public View getView(int position, BusStationLines stationLines) {
        View convertView = getLayoutInflater().inflate(R.layout.bus_station_item, null);
        BusInfoHolder holder = new BusInfoHolder();
        holder.tvBusNumber = (TextView)convertView.findViewById(R.id.bus_number);
        holder.tvStartingTime = (TextView)convertView.findViewById(R.id.starting_time);
        holder.tvEndingTime = (TextView)convertView.findViewById(R.id.ending_time);
        holder.lvStationNameList = (ListView)convertView.findViewById(R.id.station_name_list);
        convertView.setTag(holder);
        BusLine busLine = stationLines.getBusLines().get(position);//getBusLineInfo(busNumber);
        bindView(busLine, convertView, stationLines);
        return convertView;
    }

    private void bindView(BusLine busLine, View view, BusStationLines stationLines) {
        BusInfoHolder holder = (BusInfoHolder)view.getTag();
        holder.tvBusNumber.setText(busLine.getLineNumber());
        holder.tvStartingTime.setText(getString(R.string.starting_time, busLine.getStartTime()));
        holder.tvEndingTime.setText(getString(R.string.ending_time, busLine.getEndTime()));
        Direction direction = stationLines.getRouteDirection(busLine.getLineNumber());
        ArrayList<BusStation> route = busLine.getRoute(direction);
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
            if (busStation.equals(mStationLines.getStationName())) {
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

}
