
package com.telepathic.finder.app;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.actionbarsherlock.widget.SearchView.OnSuggestionListener;
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

public class BusStationFragment extends SherlockFragment {

    private MainActivity mActivity;

    private static final String TAG = BusStationFragment.class.getSimpleName();

    private static final int HISTORY_LOADER_ID = 3000;

    private static final String[] STATION_LINES_PROJECTION = {
            KuaiXinData.BusStation.NAME, KuaiXinData.BusStation.GPS_NUMBER,
            KuaiXinData.BusRoute.LINE_NUMBER, KuaiXinData.BusRoute.DIRECTION,
            KuaiXinData.BusRoute.START_TIME, KuaiXinData.BusRoute.END_TIME,
            KuaiXinData.BusRoute.STATIONS
    };

    private static final String[] BUS_STATIONS_PROJECTION = {
            KuaiXinData.BusStation._ID, KuaiXinData.BusStation.GPS_NUMBER,
            KuaiXinData.BusStation.NAME
    };

    private static final int IDX_NAME = 0;

    private static final int IDX_GPS_NUMBER = 1;

    private static final int IDX_LINE_NUMBER = 2;

    private static final int IDX_DIRECTION = 3;

    private static final int IDX_START_TIME = 4;

    private static final int IDX_END_TIME = 5;

    private static final int IDX_STATIONS = 6;

    private static final String SORT_ORDER;
    static {
        StringBuilder builder = new StringBuilder();
        builder.append(KuaiXinData.BusStation.LAST_UPDATE_TIME).append(" DESC ")
                .append("LIMIT 0,20");
        SORT_ORDER = builder.toString();
    }

    private Cursor mStationsLinesCursor;

    // private EditText mEtStationId;
    private LinearLayout mLlBusLines;

    private TextView mTvStationName;

    private LinearLayout mLlNoItem;

    private RelativeLayout mLlStationInfo;

    private KXBusStationLines mStationLines;

    private ITrafficService mTrafficService;

    private MessageDispatcher mMessageDispatcher;

    private ProgressDialog mWaitingDialog;

    private SearchView mSearchView;

    private List<String> mStationGpsNumbers;

    private List<KXBusStation> mRecentStations;

    private IMessageHandler mMessageHandler = new IMessageHandler() {
        @Override
        public int what() {
            return ITrafficeMessage.GET_BUS_STATION_LINES_DONE;
        }

        @Override
        public void handleMessage(Message msg) {
            mWaitingDialog.cancel();
            mTvStationName.requestFocusFromTouch();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bus_station, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (MainActivity)getSherlockActivity();
        mStationGpsNumbers = new ArrayList<String>();
        mRecentStations = new ArrayList<KXBusStation>();
        setupView();
        FinderApplication app = (FinderApplication)mActivity.getApplication();
        mTrafficService = app.getTrafficService();
        mMessageDispatcher = app.getMessageDispatcher();
        mActivity.getSupportLoaderManager().initLoader(HISTORY_LOADER_ID, null,
                new BusStationLinesLoaderCallback());
    }

    @Override
    public void onStart() {
        mMessageDispatcher.add(mMessageHandler);
        ActionBar actionBar = getSherlockActivity().getSupportActionBar();
        actionBar.setTitle(R.string.bus_stations);
        super.onStart();
    }

    @Override
    public void onStop() {
        mMessageDispatcher.remove(mMessageHandler);
        super.onStop();
    }

    private void setupView() {
        mLlBusLines = (LinearLayout)getView().findViewById(R.id.bus_line_list);
        mTvStationName = (TextView)getView().findViewById(R.id.bus_station_name);
        mLlNoItem = (LinearLayout)getView().findViewById(R.id.no_item_tips);
        mLlStationInfo = (RelativeLayout)getView().findViewById(R.id.station_info);
        mWaitingDialog = createWaitingDialog();
    }

    private void initBusLines(KXBusStationLines stationLines) {
        mTvStationName.setText(stationLines.getName());
        if (stationLines.getAllBusLines().size() > 0) {
            mLlBusLines.removeAllViews();
            DisplayMetrics dm = new DisplayMetrics();
            mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
            int dip5 = Utils.dip2px(mActivity, 5);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dm.widthPixels / 2
                    - (2 * dip5), LayoutParams.MATCH_PARENT);
            lp.setMargins(dip5, dip5, dip5, dip5);
            for (int index = 0; index < stationLines.getAllBusLines().size(); index++) {
                mLlBusLines.addView(getView(index, stationLines), lp);
            }
            mLlNoItem.setVisibility(View.GONE);
            mLlStationInfo.setVisibility(View.VISIBLE);
            mTvStationName.requestFocusFromTouch();
        }
    }

    private ProgressDialog createWaitingDialog() {
        ProgressDialog prgDlg = new ProgressDialog(mActivity);
        prgDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        prgDlg.setMessage(getResources().getString(R.string.find_station_information));
        prgDlg.setIndeterminate(true);
        prgDlg.setOnCancelListener(null);
        return prgDlg;
    }

    public View getView(int position, KXBusStationLines stationLines) {
        View convertView = mActivity.getLayoutInflater().inflate(R.layout.bus_station_item, null);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_cache:
                // TODO: Need implement
                return true;
            case R.id.about:
                startActivity(new Intent(mActivity, AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the options menu from XML
        inflater.inflate(R.menu.menu_bus_station, menu);

        // Get the SearchView and set the searchable configuration
        mSearchView = (SearchView)menu.findItem(R.id.search_bus_station).getActionView();
        mSearchView.setQueryHint(getResources().getText(R.string.station_number_hint));
        mSearchView.setInputType(InputType.TYPE_CLASS_NUMBER);
        mSearchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                final String gpsNumber = query;
                if (Utils.isValidGpsNumber(gpsNumber)) {
                    KXBusStationLines stationLines = getStationLines(gpsNumber);
                    Utils.hideSoftKeyboard(mActivity.getApplicationContext(), mSearchView);
                    if (stationLines == null) {
                        mTrafficService.getBusStationLines(gpsNumber);
                        mWaitingDialog.show();
                    } else {
                        mStationLines = stationLines;
                        initBusLines(stationLines);
                    }
                } else {
                    Toast.makeText(mActivity, "invalid gps number: " + gpsNumber,
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText != null && !newText.equals("") && mActivity != null) {
                    Cursor cursor = getBusStations(newText);
                    String[] from = new String[] {
                            KuaiXinData.BusStation.GPS_NUMBER, KuaiXinData.BusStation.NAME
                    };
                    int[] to = new int[] {
                            R.id.station_gps_number, R.id.station_name
                    };
                    SimpleCursorAdapter adapter = new SimpleCursorAdapter(mActivity,
                            R.layout.station_gps_number_item, cursor, from, to, 0);
                    mSearchView.setSuggestionsAdapter(adapter);
                    return true;
                } else {
                    return false;
                }
            }
        });
        mSearchView.setOnSuggestionListener(new OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = (Cursor)mSearchView.getSuggestionsAdapter().getItem(position);
                int suggestionIndex = cursor.getColumnIndex(KuaiXinData.BusStation.GPS_NUMBER);

                mSearchView.setQuery(cursor.getString(suggestionIndex), true);
                return true;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
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
                convertView = mActivity.getLayoutInflater().inflate(R.layout.bus_station_list_item,
                        null);
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
            return new CursorLoader(mActivity.getContext(),
                    ITrafficData.KuaiXinData.BusStationLines.CONTENT_URI, STATION_LINES_PROJECTION,
                    null, null, SORT_ORDER);
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

                } while (data.moveToNext());
                mRecentStations = stations;
                mStationGpsNumbers.clear();
                for (KXBusStation station : mRecentStations) {
                    mStationGpsNumbers.add(station.getGpsNumber());
                }
            }
            showLastStationLines();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mStationsLinesCursor = null;
        }

    }

    private Cursor getBusStations(String keyword) {
        ContentResolver resolver = mActivity.getContentResolver();
        String sortOrder = KuaiXinData.BusStation.GPS_NUMBER + " ASC ";
        String selection = KuaiXinData.BusStation.GPS_NUMBER + " LIKE ?";
        String[] args = new String[] {
            keyword + "%"
        };
        Cursor cursor = resolver.query(KuaiXinData.BusStation.CONTENT_URI, BUS_STATIONS_PROJECTION,
                selection, args, sortOrder);
        Utils.printCursorContent(TAG, cursor);
        return cursor;
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
            } while (cursor.moveToNext());
        }
        return stationLines;
    }
}
