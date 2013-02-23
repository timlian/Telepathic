
package com.telepathic.finder.app;

import java.util.List;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusStationLines;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData.KuaiXinData;
import com.telepathic.finder.util.Utils;

public class BusStationActivity extends BaseActivity {
    private static final String TAG = "BusStationActivity";
    private RelativeLayout mInputBar;
    private EditText mEtStationId;
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

    private IMessageHandler mMessageHandler = new IMessageHandler() {
        @Override
        public int what() {
            return ITrafficeMessage.GET_BUS_STATION_LINES_DONE;
        }

        @Override
        public void handleMessage(Message msg) {
            mWaitingDialog.cancel();
            KXBusStationLines stationLines = (KXBusStationLines)msg.obj;
            mBtnFindStation.setEnabled(true);
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
        mEtStationId = (EditText)findViewById(R.id.station_id);
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
                    mEtStationId.requestFocusFromTouch();
                    mInputBar.setVisibility(View.VISIBLE);
                    mShowHideInputBar.setImageResource(R.drawable.hide_input_bar_selector);
                }
            }
        });
        mWaitingDialog = createWaitingDialog();
    }

    public void onFindBusStationClicked(View v) {
        if (!mBtnFindStation.equals(v)) {
            return ;
        }
        String gpsNumber = mEtStationId.getText().toString();
        if (Utils.isValidGpsNumber(gpsNumber)) {
            Utils.hideSoftKeyboard(getApplicationContext(), mEtStationId);
            mTrafficService.getBusStationLines(gpsNumber);
            mWaitingDialog.show();
            mBtnFindStation.setEnabled(false);
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

}
