package com.telepathic.finder.app;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
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
import com.telepathic.finder.R;
import com.telepathic.finder.sdk.ICompletionListener;
import com.telepathic.finder.sdk.ITrafficService;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusLine;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusRoute;
import com.telepathic.finder.util.UmengEvent;
import com.telepathic.finder.util.Utils;
import com.umeng.analytics.MobclickAgent;

public class BusLineFragment extends SherlockFragment {

    private static final String TAG = BusLineFragment.class.getSimpleName();

    private static final int CUSTOM_DIALOG_ID_START = 100;

    private static final int BUS_LINE_SEARCH_DLG = CUSTOM_DIALOG_ID_START + 1;

    private MainActivity mActivity;

    private SearchView mSearchView;

    private LinearLayout mLlBusRouteList;

    private TextView mTvLineNumber;

    private TextView mTvLineStartEndStations;

    private LinearLayout mLlNoItem;

    private RelativeLayout mRlBusLineInfo;

    private ITrafficService mTrafficService;

    private Dialog mDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        mActivity = (MainActivity)activity;
        super.onAttach(activity);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupView();
        FinderApplication app = (FinderApplication)mActivity.getApplication();
        mTrafficService = app.getTrafficService();
    }

    private void setupView() {
        mLlBusRouteList = (LinearLayout)getView().findViewById(R.id.bus_route_list);
        mTvLineNumber = (TextView)getView().findViewById(R.id.bus_line_number);
        mTvLineStartEndStations = (TextView)getView().findViewById(R.id.bus_line_start_end_stations);
        mLlNoItem = (LinearLayout)getView().findViewById(R.id.no_item_tips);
        mRlBusLineInfo = (RelativeLayout)getView().findViewById(R.id.bus_line_info);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        return inflater.inflate(R.layout.fragment_bus_line, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        Utils.debug(TAG,
                "onCreateOptionsMenu: " + Utils.formatTime(new Date(System.currentTimeMillis())));
        // Inflate the options menu from XML
        inflater.inflate(R.menu.menu_bus_line, menu);

        // Get the SearchView and set the searchable configuration
        mSearchView = (SearchView)menu.findItem(R.id.search_bus_line).getActionView();

        SearchManager manager = (SearchManager)this.getSherlockActivity().getSystemService(
                Context.SEARCH_SERVICE);
        SearchableInfo info = manager.getSearchableInfo(this.getSherlockActivity()
                .getComponentName());
        mSearchView.setSearchableInfo(info);
        mSearchView.setQueryHint(getResources().getText(R.string.bus_number_hint));

        mSearchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mSearchView.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String lineNumber = query.toUpperCase();
                if (Utils.isValidBusLineNumber(lineNumber)) {
                    Utils.hideSoftKeyboard(mActivity, mSearchView);
                    showDialog(BUS_LINE_SEARCH_DLG);
                    getBusLine(lineNumber);
                } else {
                    Toast.makeText(mActivity, R.string.invalid_line_number, Toast.LENGTH_LONG)
                    .show();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        });
        EditText searchEditText = (EditText)mSearchView.findViewById(R.id.abs__search_src_text);
        if (searchEditText != null && !Utils.hasSpecialInputMethod(mActivity)) {
            searchEditText.setEms(10);
            searchEditText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        } else {
            mSearchView.setInputType(InputType.TYPE_CLASS_TEXT);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void showDialog(int id) {
        mDialog = null;
        switch (id) {
            case BUS_LINE_SEARCH_DLG:
                ProgressDialog prgDlg = new ProgressDialog(mActivity);
                prgDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                prgDlg.setMessage(getResources().getString(R.string.find_bus_route));
                prgDlg.setIndeterminate(true);
                prgDlg.setOnCancelListener(null);
                mDialog = prgDlg;
                break;
            default:
                break;
        }
        if (mDialog != null) {
            mDialog.show();
        }
    }

    private void dismissWaittingDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    private void getBusLine(String lineNumber) {
        MobclickAgent.onEvent(mActivity, UmengEvent.LINE_LINE, lineNumber);
        mTrafficService.getBusLine(lineNumber, new ICompletionListener() {

            @Override
            public void onSuccess(Object result) {
                mRlBusLineInfo.requestFocusFromTouch();
                dismissWaittingDialog();
                KXBusLine busLine = (KXBusLine)result;

                if (busLine != null) {
                    showBusLine(busLine);
                }
            }

            @Override
            public void onFailure(int errorCode, String errorText) {
                Utils.debug(TAG, "Search bus line failed: " + errorText);
                dismissWaittingDialog();
                String reason = Utils.getErrorMessage(getResources(), errorCode, errorText);
                String description = getString(R.string.search_bus_line_failed, reason);
                Toast.makeText(mActivity, description, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showBusLine(KXBusLine busLine) {
        mTvLineNumber.setText(busLine.getLineNumber());
        if (busLine.getAllRoutes().size() > 0) {
            mTvLineStartEndStations.setText(getStartEndStations(busLine));
            mLlBusRouteList.removeAllViews();
            DisplayMetrics dm = new DisplayMetrics();
            mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
            int dip5 = Utils.dip2px(mActivity, 5);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dm.widthPixels / 2
                    - (2 * dip5), LayoutParams.MATCH_PARENT);
            lp.setMargins(dip5, dip5, dip5, dip5);
            for (int index = 0; index < busLine.getAllRoutes().size(); index++) {
                mLlBusRouteList.addView(getView(busLine.getAllRoutes().get(index)), lp);
            }
            mLlNoItem.setVisibility(View.GONE);
            mRlBusLineInfo.setVisibility(View.VISIBLE);
            mRlBusLineInfo.requestFocusFromTouch();
        }
    }

    private String getStartEndStations(KXBusLine busLine) {
        String startEndStations = "";
        KXBusRoute route = busLine.getAllRoutes().get(0);
        if (route != null) {
            List<String> stations = route.getStations();
            if (stations.size() > 2) {
                startEndStations = mActivity.getString(R.string.start_end_stations, stations.get(0), stations.get(stations.size() - 1));
            }
        }
        return startEndStations;
    }

    /**
     * Get the bus route view
     */
    private View getView(KXBusRoute busRoute) {
        View convertView = mActivity.getLayoutInflater().inflate(R.layout.bus_route_item, null);
        BusRouteHolder holder = new BusRouteHolder();
        holder.tvStartTime = (TextView)convertView.findViewById(R.id.starting_time);
        holder.tvEndTime = (TextView)convertView.findViewById(R.id.ending_time);
        holder.lvStationNameList = (ListView)convertView.findViewById(R.id.station_name_list);
        convertView.setTag(holder);
        bindView(busRoute, convertView);
        return convertView;
    }

    /**
     * Bind the data with the view
     */
    private void bindView(KXBusRoute busRoute, View view) {
        BusRouteHolder holder = (BusRouteHolder)view.getTag();
        holder.tvStartTime.setText(getString(R.string.starting_time, busRoute.getStartTime()));
        holder.tvEndTime.setText(getString(R.string.ending_time, busRoute.getEndTime()));
        holder.lvStationNameList.setAdapter(new StationsAdapter(busRoute.getStations()));
    }

    private class BusRouteHolder {
        TextView tvStartTime;
        TextView tvEndTime;
        ListView lvStationNameList;
    }

    private class StationsAdapter extends BaseAdapter {
        private List<String> mBusStations;

        public StationsAdapter(List<String> busStations) {
            mBusStations = busStations;
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
        }

        private class StationNameHolder {
            TextView tvBusStationName;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(mActivity, AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
