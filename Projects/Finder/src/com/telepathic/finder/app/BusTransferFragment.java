package com.telepathic.finder.app;

import java.util.ArrayList;
import java.util.List;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.telepathic.finder.R;
import com.telepathic.finder.sdk.ICompletionListener;
import com.telepathic.finder.sdk.ITrafficService;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXProgramStep;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXTransferProgram;

public class BusTransferFragment extends SherlockFragment {
    private MainActivity mActivity;

    private AutoCompleteTextView mStartStation;

    private AutoCompleteTextView mEndStation;

    private ImageButton mBtnQueryTransfer;

    private ITrafficService mTrafficService;

    private String mKeyWord;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bus_transfer, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (MainActivity)getSherlockActivity();

        FinderApplication app = (FinderApplication)mActivity.getApplication();
        mTrafficService = app.getTrafficService();

        mStartStation = (AutoCompleteTextView)getView().findViewById(R.id.start_station);
        mStartStation.setThreshold(2);
        mStartStation.addTextChangedListener(new StationNameWatcher(mStartStation));

        mEndStation = (AutoCompleteTextView)getView().findViewById(R.id.end_station);
        mEndStation.setThreshold(2);
        mEndStation.addTextChangedListener(new StationNameWatcher(mEndStation));

        mBtnQueryTransfer = (ImageButton)getView().findViewById(R.id.query_transfer);
        mBtnQueryTransfer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String startStationName = mStartStation.getText().toString().trim();
                String endStationName = mEndStation.getText().toString().trim();
                if (TextUtils.isEmpty(startStationName) || TextUtils.isEmpty(endStationName)) {
                    //TODO: Need add resource
                    Toast.makeText(mActivity, "Please input the station name", Toast.LENGTH_SHORT);
                } else {
                    mTrafficService.getBusTransferRoute(startStationName, endStationName, new ICompletionListener() {
                        @Override
                        public void onSuccess(Object result) {
                            ArrayList<KXTransferProgram> transferList = (ArrayList<KXTransferProgram>)result;
                            for (KXTransferProgram program : transferList) {
                                String id = program.getProgramId();
                                String time = program.getTransferTime();
                                List<KXProgramStep> steps = program.getSteps();
                                Log.d("debug", "id = " + id);
                                Log.d("debug", "time = " + time);
                                for (KXProgramStep step : steps) {
                                    Log.d("debug", "====from = " + step.getSource());
                                    Log.d("debug", "====by = " + step.getLineName());
                                    Log.d("debug", "====to = " + step.getDestination());
                                }
                            }
                        }

                        @Override
                        public void onFailure(int errorCode, String errorText) {
                            // TODO Auto-generated method stub

                        }
                    });
                }
            }
        });
    }

    private void getSuggestionStations(final AutoCompleteTextView stationEditText) {
        if (mKeyWord.length() >= 2) {
            mTrafficService.queryStationName(mKeyWord, new ICompletionListener() {

                @Override
                public void onSuccess(Object result) {
                    ArrayList<String> stations = (ArrayList<String>)result;
                    if (stations != null && stations.size() > 0) {
                        for(String station : stations) {
                            Log.d("debug", "lisy=======station = " + station);
                        }
                        StationNameAdapter adapter = new StationNameAdapter(stations);
                        stationEditText.setAdapter(adapter);
                        stationEditText.showDropDown();
                    }

                }

                @Override
                public void onFailure(int errorCode, String errorText) {
                    // TODO Auto-generated method stub
                }
            });
        }
    }

    private class StationNameWatcher implements TextWatcher {
        private AutoCompleteTextView mStationEditText;

        public StationNameWatcher(AutoCompleteTextView etStation){
            mStationEditText = etStation;
        }

        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mKeyWord = s.toString();
            getSuggestionStations(mStationEditText);
        }
    }

    private class StationNameAdapter implements ListAdapter,Filterable {

        private ArrayList<String> mStationsList;

        private MyFilter mFilter;

        public StationNameAdapter(ArrayList<String> list) {
            mStationsList = list;
        }

        @Override
        public int getCount() {
            return mStationsList.size();
        }

        @Override
        public Object getItem(int position) {
            return mStationsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String stationName = mStationsList.get(position);
            if (convertView == null) {
                convertView = mActivity.getLayoutInflater().inflate(R.layout.station_transfer_suggestion_item, null);
                StationNameHolder holder = new StationNameHolder();
                holder.mStation = (TextView)convertView.findViewById(R.id.station_transfer_name_item);
                convertView.setTag(holder);
            }
            bindView(stationName, convertView);
            return convertView;
        }

        private void bindView(String busStation, View view) {
            StationNameHolder holder = (StationNameHolder)view.getTag();
            String highlightBusStation = busStation.replace(mKeyWord, "<font color=\"#0099CC\">" + mKeyWord +"</font>");
            holder.mStation.setText(Html.fromHtml(highlightBusStation));
        }

        private class StationNameHolder {
            TextView mStation;
        }

        @Override
        public Filter getFilter() {
            if (mFilter == null) {
                mFilter = new MyFilter();
            }
            return mFilter;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return mStationsList.isEmpty();
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            // TODO Auto-generated method stub

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            // TODO Auto-generated method stub

        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        private class MyFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults result = new FilterResults();
                ArrayList<String> strsTemp = new ArrayList<String>();
                if (null != constraint && constraint.length() > 0) {
                    for (String station : mStationsList) {
                        if (station.toLowerCase().contains(constraint.toString()
                                .toLowerCase())) {
                            strsTemp.add(station);
                        }
                    }
                    result.values = strsTemp;
                    result.count = strsTemp.size();
                }
                return result;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                ArrayList<String> tempList = (ArrayList<String>)results.values;
                mStationsList = tempList;
            }

        }

    }


}
