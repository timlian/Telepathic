
package com.telepathic.finder.app;

import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
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
import com.telepathic.finder.sdk.ICompletionListener;
import com.telepathic.finder.sdk.ITrafficService;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusLine;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusLine.Direction;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusRoute;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusStationLines;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData.KuaiXinData;
import com.telepathic.finder.util.Utils;

public class BusStationFragment extends SherlockFragment {
    private static final String TAG = "BusStationFragment";

    private MainActivity mActivity;

    private LinearLayout mLlBusLines;

    private TextView mTvStationName;

    private TextView mTvStationGpsNumber;

    private LinearLayout mLlNoItem;

    private RelativeLayout mLlStationInfo;

    private ITrafficService mTrafficService;

    private ProgressDialog mWaitingDialog;

    private SearchView mSearchView;

    private KuaiXinDataCache mDataCache;

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
        if (mDataCache == null) {
            mDataCache = new KuaiXinDataCache(mActivity);
        }
        setupView();
        FinderApplication app = (FinderApplication)mActivity.getApplication();
        mTrafficService = app.getTrafficService();
        KXBusStationLines stationLines = mDataCache.getLastStationLines();
        if (stationLines != null) {
        	showStationLines(stationLines);
        }
    }

    @Override
    public void onStart() {
        ActionBar actionBar = getSherlockActivity().getSupportActionBar();
        actionBar.setTitle(R.string.bus_stations);
        super.onStart();
    }

    private void setupView() {
        mLlBusLines = (LinearLayout)getView().findViewById(R.id.bus_line_list);
        mTvStationName = (TextView)getView().findViewById(R.id.bus_station_name);
        mTvStationGpsNumber = (TextView)getView().findViewById(R.id.bus_station_gps_number);
        mLlNoItem = (LinearLayout)getView().findViewById(R.id.no_item_tips);
        mLlStationInfo = (RelativeLayout)getView().findViewById(R.id.station_info);
        mWaitingDialog = createWaitingDialog();
    }

    private void showStationLines(KXBusStationLines stationLines) {
        mTvStationName.setText(stationLines.getName());
        mTvStationGpsNumber.setText(mActivity.getString(R.string.gps_number,
                stationLines.getGpsNumber()));
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
        holder.lvStationNameList.setAdapter(new StationsAdapter(busRoute.getStations(),
                stationLines.getName()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_cache:
                Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle(R.string.confirm_clean_cache_title)
                        .setMessage(R.string.confirm_clean_cache_message)
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Utils.copyAppDatabaseFiles(mActivity.getPackageName());
                                        deleteAllStations();
                                        getSuggestions(""); // reset the
                                                            // suggestions
                                    }
                                }).setNegativeButton(android.R.string.cancel, null);
                builder.create().show();
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
        SearchManager manager = (SearchManager)this.getSherlockActivity().getSystemService(
                Context.SEARCH_SERVICE);
        SearchableInfo info = manager.getSearchableInfo(this.getSherlockActivity()
                .getComponentName());
        mSearchView.setSearchableInfo(info);
        mSearchView.setQueryHint(getResources().getText(R.string.station_number_hint));
        mSearchView.setInputType(InputType.TYPE_CLASS_NUMBER);
        mSearchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mSearchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String keyword) {
                String gpsNumber = keyword;
                if (Utils.isValidGpsNumber(gpsNumber)) {
                    Utils.hideSoftKeyboard(mActivity.getApplicationContext(), mSearchView);
                    searchStationLines(gpsNumber);
                } else {
                    Toast.makeText(mActivity, R.string.invalid_gps_number, Toast.LENGTH_SHORT)
                            .show();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (mActivity == null) {
                    return false;
                }
                getSuggestions(newText);
                return true;
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
        mSearchView.setOnQueryTextFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String queryText = mSearchView.getQuery().toString();
                if (hasFocus && !TextUtils.isEmpty(queryText)) {
                    getSuggestions(queryText);
                    mSearchView.setQuery(queryText, false);
                }
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void getSuggestions(String queryText) {
        Cursor cursor = mDataCache.queryBusStations(queryText);
        String[] from = new String[] {
                KuaiXinData.BusStation.GPS_NUMBER, KuaiXinData.BusStation.NAME
        };
        int[] to = new int[] {
                R.id.station_gps_number, R.id.station_name
        };
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(mActivity,
                R.layout.station_gps_number_item, cursor, from, to, 0);
        mSearchView.setSuggestionsAdapter(adapter);
    }

    private class StationsAdapter extends BaseAdapter {
        private List<String> mBusStations;

        private String mCurStationName;

        public StationsAdapter(List<String> busStations, String curStationName) {
            mBusStations = busStations;
            mCurStationName = curStationName;
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
            if (busStation.equals(mCurStationName)) {
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

    private void searchStationLines(String gpsNumber) {
        KXBusStationLines stationLines = mDataCache.getStationLines(gpsNumber);
        if (stationLines != null) {
            showStationLines(stationLines);
            return;
        }
        mWaitingDialog.show();
        mTrafficService.getBusStationLines(gpsNumber, new ICompletionListener() {
            @Override
            public void onSuccess(Object result) {
                mTvStationName.requestFocusFromTouch();
                dismissWaittingDialog();
                KXBusStationLines lines = (KXBusStationLines)result;
                if (lines != null) {
                    showStationLines(lines);
                }
            }

            @Override
            public void onFailure(int errorCode, String errorText) {
                dismissWaittingDialog();
                Utils.debug(TAG, "get bus station lines failed: " + errorText);
                String reason = Utils.getErrorMessage(getResources(), errorCode, errorText);
                String description = getString(R.string.get_bus_card_records_failed, reason);
                Toast.makeText(mActivity, description, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteAllStations() {
        ContentResolver resolver = mActivity.getContentResolver();
        int rows = resolver.delete(ITrafficData.KuaiXinData.BusStation.CONTENT_URI, null, null);
        Utils.debug(TAG, "delete rows: " + rows);
    }

    private void dismissWaittingDialog() {
        if (mWaitingDialog != null && mWaitingDialog.isShowing()) {
            mWaitingDialog.dismiss();
        }
    }
}
