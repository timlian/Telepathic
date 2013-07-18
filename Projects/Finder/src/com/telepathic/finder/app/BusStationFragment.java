
package com.telepathic.finder.app;

import java.util.ArrayList;
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
import android.database.MatrixCursor;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXStationLines;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData.KuaiXinData;
import com.telepathic.finder.util.UmengEvent;
import com.telepathic.finder.util.Utils;
import com.umeng.analytics.MobclickAgent;

public class BusStationFragment extends SherlockFragment {
    private static final String TAG = "BusStationFragment";

    private static final String NUMBER_EXPRESSION = "\\d+";

    private static final String FAKE_GPS_NUMBER = "fake";

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

    private AlertDialog mStationSelectDlg;

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

    private void setupView() {
        mLlBusLines = (LinearLayout)getView().findViewById(R.id.bus_line_list);
        mTvStationName = (TextView)getView().findViewById(R.id.bus_station_name);
        mTvStationGpsNumber = (TextView)getView().findViewById(R.id.bus_station_gps_number);
        mLlNoItem = (LinearLayout)getView().findViewById(R.id.no_item_tips);
        mLlStationInfo = (RelativeLayout)getView().findViewById(R.id.station_info);
        if (mWaitingDialog == null) {
            mWaitingDialog = createWaitingDialog();
        }
    }

    private void showStationLines(KXBusStationLines stationLines) {
        mTvStationName.setText(stationLines.getName());
        String gpsNumber = mActivity.getString(R.string.gps_number, stationLines.getGpsNumber());
        mTvStationGpsNumber.setText(gpsNumber);
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
            mLlStationInfo.requestFocusFromTouch();
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
        holder.llBusLineTime = (LinearLayout)convertView.findViewById(R.id.bus_line_time);
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
        if (busRoute != null) {
            String startTime = busRoute.getStartTime();
            String endTime = busRoute.getEndTime();
            holder.tvStartingTime.setText(getString(R.string.starting_time, startTime));
            holder.tvEndingTime.setText(getString(R.string.ending_time, endTime));
            if (TextUtils.isEmpty(startTime) || TextUtils.isEmpty(endTime)) {
                holder.llBusLineTime.setVisibility(View.GONE);
            }
            holder.lvStationNameList.setAdapter(new StationsAdapter(busRoute.getStations(),
                    stationLines.getName()));
        } else {
            holder.tvStartingTime.setText(getString(R.string.starting_time, getString(R.string.unknown)));
            holder.tvEndingTime.setText(getString(R.string.ending_time, getString(R.string.unknown)));
            Toast.makeText(mActivity, R.string.station_line_data_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_cache:
                Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle(R.string.confirm_clean_cache_title)
                .setMessage(R.string.confirm_clean_bus_station_cache)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Utils.copyAppDatabaseFiles(mActivity.getPackageName());
                        deleteAllStations();
                        getSuggestions2(""); // reset the
                        // suggestions
                    }
                }).setNegativeButton(R.string.cancel, null);
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
        menu.clear();
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
        mSearchView.setInputType(InputType.TYPE_CLASS_TEXT);
        mSearchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mSearchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String keyword) {
                Utils.hideSoftKeyboard(mActivity.getApplicationContext(), mSearchView);
                if(isNumber(keyword)) {
                    if (Utils.isValidGpsNumber(keyword)) {
                        searchStationLines(keyword, true);
                    } else {
                        Toast.makeText(mActivity, R.string.invalid_gps_number, Toast.LENGTH_SHORT)
                        .show();
                    }
                } else {
                    searchStationLines(keyword, false);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (mActivity == null) {
                    return false;
                }
                if (isNumber(newText) || newText.equals("")) {
                    getSuggestions2(newText);
                } else {
                    getSuggestions(newText);
                }
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
                int gpsNumberIndex = cursor.getColumnIndex(KuaiXinData.BusStation.GPS_NUMBER);
                int stationNameIndex = cursor.getColumnIndex(KuaiXinData.BusStation.NAME);

                String query = cursor.getString(gpsNumberIndex);
                if (query.equals(FAKE_GPS_NUMBER)) {
                    query = cursor.getString(stationNameIndex);
                }
                mSearchView.setQuery(query, true);
                return true;
            }
        });
        EditText searchEditText = (EditText)mSearchView.findViewById(R.id.abs__search_src_text);
        if (searchEditText != null) {
            searchEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    String queryText = mSearchView.getQuery().toString();
                    if (hasFocus) {
                        mSearchView.setQuery(queryText, false);
                    }
                }
            });
        } else {
            mSearchView.setOnQueryTextFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    String queryText = mSearchView.getQuery().toString();
                    if (hasFocus) {
                        mSearchView.setQuery(queryText, false);
                    }
                }
            });
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void getSuggestions2(String queryText) {
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

    private void getSuggestions(final String queryText) {
        if (!TextUtils.isEmpty(queryText) && queryText.length() >= 2) {
            mTrafficService.queryStationName(queryText, new ICompletionListener() {

                @Override
                public void onSuccess(Object result) {
                    ArrayList<String> stationNames = (ArrayList<String>) result;
                    String[] columnNames = {"_id", "gps_number", "name"};
                    MatrixCursor cursor = new MatrixCursor(columnNames);
                    String[] suggestionEntry = new String[3];
                    for(int i = 0; i < stationNames.size(); i++) {
                        suggestionEntry[0] = String.valueOf(i);
                        suggestionEntry[1] = FAKE_GPS_NUMBER;
                        suggestionEntry[2] = stationNames.get(i);
                        cursor.addRow(suggestionEntry);
                    }
                    String[] from = new String[] {
                            KuaiXinData.BusStation.NAME
                    };
                    int[] to = new int[] {
                            R.id.station_name
                    };
                    SimpleCursorAdapter adapter = new SimpleCursorAdapter(mActivity,
                            R.layout.station_name_item, cursor, from, to, 0);
                    mSearchView.setSuggestionsAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onFailure(int errorCode, String errorText) {
                    // TODO Auto-generated method stub

                }
            });
        }

    }

    private boolean isNumber(String keyword) {
        if(keyword.matches(NUMBER_EXPRESSION)) {
            return true;
        } else {
            return false;
        }
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
        LinearLayout llBusLineTime;

        TextView tvBusNumber;

        TextView tvStartingTime;

        TextView tvEndingTime;

        ListView lvStationNameList;
    }

    private void searchStationLines(String stationNameOrGpsNumber, boolean isGpsNumber) {
        if (isGpsNumber) {
            MobclickAgent.onEvent(mActivity, UmengEvent.STATION_GPS_NUMBER, stationNameOrGpsNumber);
            KXBusStationLines stationLines = mDataCache.getStationLines(stationNameOrGpsNumber);
            if (stationLines != null) {
                showStationLines(stationLines);
                return;
            }
        } else {
            stationNameOrGpsNumber = Utils.completeStationName(stationNameOrGpsNumber);
            MobclickAgent.onEvent(mActivity, UmengEvent.STATION_NAME, stationNameOrGpsNumber);
        }
        mWaitingDialog.show();
        mTrafficService.getBusStationLines(stationNameOrGpsNumber, new ICompletionListener() {
            @Override
            public void onSuccess(Object result) {
                mLlStationInfo.requestFocusFromTouch();
                dismissWaittingDialog();
                List<KXStationLines> stationLinesList = (List<KXStationLines>)result;
                if (stationLinesList != null && stationLinesList.size() > 0) {
                    if (stationLinesList.size() == 1) {
                        KXStationLines stationLine = stationLinesList.get(0);
                        KXBusStationLines busStationLine = mDataCache.getStationLines(stationLine.getGpsNumber());
                        showStationLines(busStationLine);
                    } else {
                        showSelectStationLineDlg(stationLinesList);
                    }
                }
            }

            @Override
            public void onFailure(int errorCode, String errorText) {
                dismissWaittingDialog();
                Utils.debug(TAG, "get bus station lines failed: " + errorText);
                String reason = Utils.getErrorMessage(getResources(), errorCode, errorText);
                String description = getString(R.string.get_bus_station_lines_failed, reason);
                Toast.makeText(mActivity, description, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showSelectStationLineDlg(final List<KXStationLines> stationLineList) {
        final int stationCount = stationLineList.size();
        final String[] stations = new String[stationCount];
        for (int idx = 0; idx < stationCount; idx++) {
            String gpsNumber = stationLineList.get(idx).getGpsNumber();
            String stationName = stationLineList.get(idx).getName();
            stations[idx] = gpsNumber + " - " + stationName;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        final String titleText = getString(R.string.select_a_station);
        builder.setTitle(titleText).setSingleChoiceItems(stations, 0, null)
        .setOnCancelListener(null)
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                final int selectedPosition = ((AlertDialog)dialog).getListView()
                        .getCheckedItemPosition();
                final KXStationLines stationLine = stationLineList.get(selectedPosition);
                KXBusStationLines busStationLine = mDataCache.getStationLines(stationLine.getGpsNumber());
                showStationLines(busStationLine);
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        if (mStationSelectDlg != null && mStationSelectDlg.isShowing()) {
            mStationSelectDlg.dismiss();
        }
        mStationSelectDlg = builder.create();
        mStationSelectDlg.show();
    }

    private void deleteAllStations() {
        MobclickAgent.onEvent(mActivity, UmengEvent.STATION_CLEAR);
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
