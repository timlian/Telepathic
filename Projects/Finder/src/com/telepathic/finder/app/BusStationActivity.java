
package com.telepathic.finder.app;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.telepathic.finder.R;
import com.telepathic.finder.app.MessageDispatcher.IMessageHandler;
import com.telepathic.finder.sdk.ITrafficService;
import com.telepathic.finder.sdk.ITrafficeMessage;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusLine;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusLine.Direction;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusRoute;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusStation;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusStationLines;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData.KuaiXinData;
import com.telepathic.finder.util.Utils;

public class BusStationActivity extends BaseActivity {
    private static final String TAG = "BusStationActivity";
    
    private static final int HISTORY_LOADER_ID = 3000;
	
	private static final String[] STATION_LINES_PROJECTION = {
		KuaiXinData.BusStation.NAME,
		KuaiXinData.BusStation.GPS_NUMBER,
		KuaiXinData.BusRoute.LINE_NUMBER,
        KuaiXinData.BusRoute.DIRECTION,
        KuaiXinData.BusRoute.START_TIME,
        KuaiXinData.BusRoute.END_TIME,
        KuaiXinData.BusRoute.STATIONS
	};
	private static final int IDX_NAME = 0;
	private static final int IDX_GPS_NUMBER = 1 ;
	private static final int IDX_LINE_NUMBER = 2;
	private static final int IDX_DIRECTION = 3;
	private static final int IDX_START_TIME = 4;
	private static final int IDX_END_TIME = 5;
	private static final int IDX_STATIONS = 6;
	
	private static final String SORT_ORDER;
	static {
		StringBuilder builder = new StringBuilder();
		builder.append(KuaiXinData.BusStation.LAST_UPDATE_TIME)
		       .append(" DESC ")
		       .append("LIMIT 0,20");
		SORT_ORDER = builder.toString();
	}
	
	private Cursor mStationsLinesCursor;
	
    private RelativeLayout mInputBar;
    //private EditText mEtStationId;
    private Button mBtnFindStation;
    private LinearLayout mLlBusLines;
    private TextView mTvStationName;
    private LinearLayout mLlNoItem;
    private RelativeLayout mLlStationInfo;
    private ImageView mShowHideInputBar;
    private KXBusStationLines mStationLines;
    private ITrafficService mTrafficService;
    private MessageDispatcher mMessageDispatcher;
    private ProgressDialog mWaitingDialog;
    private AutoCompleteTextView mGpsNumber;
    private List<KXBusStation> mRecentStations;
    private StationGpsNumberAdapter mAdapter;

    private IMessageHandler mMessageHandler = new IMessageHandler() {
        @Override
        public int what() {
            return ITrafficeMessage.GET_BUS_STATION_LINES_DONE;
        }

        @Override
        public void handleMessage(Message msg) {
            mWaitingDialog.cancel();
            mBtnFindStation.setEnabled(true);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bus_station);
        mRecentStations = new ArrayList<KXBusStation>();
        mAdapter = new StationGpsNumberAdapter();
        setupView();
        FinderApplication app = (FinderApplication)getApplication();
        mTrafficService = app.getTrafficService();
        mMessageDispatcher = app.getMessageDispatcher();
        mMessageDispatcher.add(mMessageHandler);
        getSupportLoaderManager().initLoader(HISTORY_LOADER_ID, null, new BusStationLinesLoaderCallback());
    }

    @Override
    protected void onDestroy() {
        mMessageDispatcher.remove(mMessageHandler);
        super.onDestroy();
    }

    private void queryData() {
        KXBusStationLines stationLines = new KXBusStationLines();
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
            stationLines.setName(name);
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
                    KXBusLine busLine = new KXBusLine(cursor.getString(idxLineNumber));
                    KXBusRoute busRoute = new KXBusRoute();
                    busRoute.setStartTime(startTime);
                    busRoute.setEndTime(endTime);
                    busRoute.setStations(stationNames);
                    busRoute.setDirection(direction);
                    busLine.addRoute(busRoute);
                    stationLines.addBusLine(busLine);
                    stationLines.addLineDirection(lineNumber, direction);
                } while(cursor.moveToNext());
                mStationLines = stationLines;
                initBusLines(stationLines);
            }
            Utils.printCursorContent(TAG, cursor);
        }
    }

    private void setupView(){
        mInputBar = (RelativeLayout)findViewById(R.id.station_id_input_bar);
        mLlBusLines = (LinearLayout)findViewById(R.id.bus_line_list);
        mTvStationName = (TextView)findViewById(R.id.bus_station_name);
        mGpsNumber = (AutoCompleteTextView)findViewById(R.id.station_id);
        mBtnFindStation = (Button)findViewById(R.id.find_bus_station);
        mLlNoItem = (LinearLayout)findViewById(R.id.no_item_tips);
        mLlStationInfo = (RelativeLayout)findViewById(R.id.station_info);
        mShowHideInputBar = (ImageView)findViewById(R.id.show_hide_input_bar);
        mShowHideInputBar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mInputBar.getVisibility() == View.VISIBLE) {
                    mInputBar.setVisibility(View.GONE);
                    mShowHideInputBar.setImageResource(R.drawable.show_input_bar_selector);
                } else if (mInputBar.getVisibility() == View.GONE){
                	mGpsNumber.requestFocusFromTouch();
                	mGpsNumber.showDropDown();
                    mInputBar.setVisibility(View.VISIBLE);
                    mShowHideInputBar.setImageResource(R.drawable.hide_input_bar_selector);
                }
            }
        });
        mWaitingDialog = createWaitingDialog();
        mGpsNumber.setAdapter(mAdapter);
    }

    public void onSearchClicked(View v) {
    	Utils.hideSoftKeyboard(getApplicationContext(), mGpsNumber);
        if (!mBtnFindStation.equals(v)) {
            return ;
        }
        String gpsNumber = mGpsNumber.getText().toString();
        if (Utils.isValidGpsNumber(gpsNumber)) {
        	KXBusStationLines stationLines = getStationLines(gpsNumber);
        	if (stationLines == null) {
	            mTrafficService.getBusStationLines(gpsNumber);
	            mWaitingDialog.show();
	            mBtnFindStation.setEnabled(false);
        	} else {
        		mStationLines = stationLines;
        		initBusLines(stationLines);
        	}
        } else {
            Toast.makeText(this, "invalid gps number: " + gpsNumber, Toast.LENGTH_SHORT).show();
        }
    }

    private void initBusLines(KXBusStationLines stationLines) {
        mTvStationName.setText(stationLines.getName());
        if(stationLines.getAllBusLines().size() > 0) {
            mLlBusLines.removeAllViews();
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            int dip5 = Utils.dip2px(this, 5);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dm.widthPixels / 2 - (2*dip5), LayoutParams.MATCH_PARENT);
            lp.setMargins(dip5, dip5, dip5, dip5);
            for (int index = 0; index < stationLines.getAllBusLines().size(); index++) {
                mLlBusLines.addView(getView(index, stationLines), lp);
            }
            mInputBar.setVisibility(View.GONE);
            mLlNoItem.setVisibility(View.GONE);
            mLlStationInfo.setVisibility(View.VISIBLE);
        }
    }

    private ProgressDialog createWaitingDialog() {
        ProgressDialog prgDlg = new ProgressDialog(this);
        prgDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        prgDlg.setMessage(getResources().getString(R.string.find_station_information));
        prgDlg.setIndeterminate(true);
        prgDlg.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mBtnFindStation.setEnabled(true);
            }
        });
        return prgDlg;
    }

    public View getView(int position, KXBusStationLines stationLines) {
        View convertView = getLayoutInflater().inflate(R.layout.bus_station_item, null);
        BusInfoHolder holder = new BusInfoHolder();
        holder.tvBusNumber = (TextView)convertView.findViewById(R.id.bus_number);
        holder.tvStartingTime = (TextView)convertView.findViewById(R.id.starting_time);
        holder.tvEndingTime = (TextView)convertView.findViewById(R.id.ending_time);
        holder.lvStationNameList = (ListView)convertView.findViewById(R.id.station_name_list);
        convertView.setTag(holder);
        KXBusLine busLine = stationLines.getAllBusLines().get(position);
        bindView(busLine, convertView, stationLines);
        return convertView;
    }

    private void bindView(KXBusLine busLine, View view, KXBusStationLines stationLines) {
    	Direction lineDirection = stationLines.getLineDirection(busLine.getLineNumber());
    	KXBusRoute busRoute = busLine.getRoute(lineDirection);
        BusInfoHolder holder = (BusInfoHolder)view.getTag();
        holder.tvBusNumber.setText(busLine.getLineNumber());
        holder.tvStartingTime.setText(getString(R.string.starting_time, busRoute.getStartTime()));
        holder.tvEndingTime.setText(getString(R.string.ending_time, busRoute.getEndTime()));
        holder.lvStationNameList.setAdapter(new StationsAdapter(busRoute.getStations()));
    }

    private class StationsAdapter extends BaseAdapter {
        private List<String> mBusStations;

        public StationsAdapter(List<String> busStations) {
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
            if (busStation.equals(mStationLines.getName())) {
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
    
    private class BusStationLinesLoaderCallback implements LoaderCallbacks<Cursor> {
    	
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			return new CursorLoader(getContext(),
					ITrafficData.KuaiXinData.BusStationLines.CONTENT_URI,
					STATION_LINES_PROJECTION, null, null, SORT_ORDER);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        	mStationsLinesCursor = data;
        	if (data != null && data.moveToFirst()) {
        		KXBusStation preStation = null;
        		KXBusStation curStation = null;
        		ArrayList<KXBusStation> stations = new ArrayList<KXBusStation>();
        		do {
        			curStation = new KXBusStation();
        			curStation.setName(data.getString(IDX_NAME));
        			curStation.setGpsNumber(data.getString(IDX_GPS_NUMBER));
        			if (preStation != null) {
        				if (preStation.getGpsNumber().equals(curStation.getGpsNumber()) == false) {
        					stations.add(curStation);
        					preStation = curStation;
        				}
        			} else {
        				stations.add(curStation);
        				preStation = curStation;
        			}
        			
        		} while(data.moveToNext());
        		mRecentStations = stations;
        	}
        	mAdapter.notifyDataSetChanged();
        	showLastStationLines();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        	mStationsLinesCursor = null;
        }
        
    }
    
    private void showLastStationLines() {
    	Cursor cursor = mStationsLinesCursor;
    	if (cursor != null && cursor.moveToFirst()) {
    		String lastStationGpsNumber = cursor.getString(IDX_GPS_NUMBER);
    		KXBusStationLines stationLines = getStationLines(lastStationGpsNumber);
    		if (stationLines != null) {
    			mStationLines = stationLines;
    			initBusLines(mStationLines);
    		}
    	}
    }
    
    private KXBusStationLines getStationLines(String gpsNumber) {
    	KXBusStationLines stationLines = null;
    	Cursor cursor = mStationsLinesCursor;
    	Utils.printCursorContent(TAG, cursor);
    	if (cursor != null && cursor.moveToFirst()) {
    		do {
    			if (gpsNumber.equals(cursor.getString(IDX_GPS_NUMBER))) {
    				if (stationLines == null) {
    					stationLines = new KXBusStationLines();
    					stationLines.setName(cursor.getString(IDX_NAME));
        				stationLines.setGpsNumber(cursor.getString(IDX_GPS_NUMBER));
    				}
    				String lineNumber = cursor.getString(IDX_LINE_NUMBER);
    				Direction direction = Direction.fromString(cursor.getString(IDX_DIRECTION));
    				KXBusLine busLine = new KXBusLine(lineNumber);
                    KXBusRoute busRoute = new KXBusRoute();
                    busRoute.setStartTime(cursor.getString(IDX_START_TIME));
                    busRoute.setEndTime(cursor.getString(IDX_END_TIME));
                    busRoute.setStations(cursor.getString(IDX_STATIONS).split(","));
                    busRoute.setDirection(direction);
                    busLine.addRoute(busRoute);
                    stationLines.addBusLine(busLine);
                    stationLines.addLineDirection(lineNumber, direction);
    			}
    		} while(cursor.moveToNext());
    	}
    	return stationLines;
    }
    
    private static class StationItem {
    	TextView mNameText;
    	TextView mGpsNumberText;
    }
    
    private class StationGpsNumberAdapter extends ArrayAdapter<KXBusStation> {
    	private LayoutInflater mInflater;
    	
		public StationGpsNumberAdapter() {
			super(BusStationActivity.this, R.layout.station_gps_number_item,
					mRecentStations);
			mInflater = getLayoutInflater();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
                convertView = mInflater.inflate(R.layout.station_gps_number_item, parent, false);
                StationItem holder = new StationItem();
                holder.mNameText = (TextView) convertView.findViewById(R.id.station_name);
                holder.mGpsNumberText = (TextView) convertView.findViewById(R.id.station_gps_number);
                convertView.setTag(holder);
            }
			KXBusStation busStation = mRecentStations.get(position);
            StationItem stationItem = (StationItem)convertView.getTag();
            stationItem.mNameText.setText(busStation.getName());
            stationItem.mGpsNumberText.setText(busStation.getGpsNumber());
            return convertView;
		}

    }

}
