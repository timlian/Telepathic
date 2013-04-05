package com.telepathic.finder.app;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.telepathic.finder.R;
import com.telepathic.finder.sdk.ICompletionListener;
import com.telepathic.finder.sdk.ITrafficService;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXProgramStep;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXTransferProgram;
import com.telepathic.finder.util.Utils;

public class BusTransferFragment extends SherlockFragment {

    private static final int CUSTOM_DIALOG_ID_START = 100;

    private static final int BUS_TRANSFER_SEARCH_DLG = CUSTOM_DIALOG_ID_START + 1;

    private MainActivity mActivity;

    private AutoCompleteTextView mStartStation;

    private AutoCompleteTextView mEndStation;

    private ListView mTransferList;

    private ImageButton mBtnQueryTransfer;

    private ITrafficService mTrafficService;

    private Dialog mDialog;

    private String mKeyWord;

    private ProgressBar mStartStationProgressBar, mEndStationProgressBar;

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

        setupView();
    }

    private void setupView() {
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
                    Toast.makeText(mActivity, R.string.no_station_name_tips, Toast.LENGTH_SHORT).show();
                } else {
                    showDialog(BUS_TRANSFER_SEARCH_DLG);
                    getBusTransferRoute(startStationName, endStationName);
                    Utils.hideSoftKeyboard(mActivity, mEndStation);
                }
            }
        });

        mTransferList = (ListView)getView().findViewById(R.id.transfer_list);

        mStartStationProgressBar = (ProgressBar)getView().findViewById(R.id.start_station_progressbar);
        mEndStationProgressBar = (ProgressBar)getView().findViewById(R.id.end_station_progressbar);
    }

    private void getBusTransferRoute(String startStation, String endStation) {
        mTrafficService.getBusTransferRoute(startStation, endStation, new ICompletionListener() {
            @Override
            public void onSuccess(Object result) {
                dismissWaittingDialog();
                ArrayList<KXTransferProgram> transferList = (ArrayList<KXTransferProgram>)result;
                TransferSchemeAdapter adapter = new TransferSchemeAdapter(transferList);
                mTransferList.setAdapter(adapter);
            }

            @Override
            public void onFailure(int errorCode, String errorText) {
                dismissWaittingDialog();
                String reason = Utils.getErrorMessage(getResources(), errorCode, errorText);
                String description = getString(R.string.get_bus_transfer_failed, reason);
                Toast.makeText(mActivity, description, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getSuggestionStations(final AutoCompleteTextView stationEditText) {
        if (mKeyWord.length() >= 2) {
            if (stationEditText == mStartStation) {
                mStartStationProgressBar.setVisibility(View.VISIBLE);
            } else if (stationEditText == mEndStation) {
                mEndStationProgressBar.setVisibility(View.VISIBLE);
            }
            mTrafficService.queryStationName(mKeyWord, new ICompletionListener() {

                @Override
                public void onSuccess(Object result) {
                    ArrayList<String> stations = (ArrayList<String>)result;
                    if (stations != null && stations.size() > 0) {
                        if (!(stations.size() == 1 && stations.get(0).equals(mKeyWord))) {
                            StationNameAdapter adapter = new StationNameAdapter(stations);
                            stationEditText.setAdapter(adapter);
                            stationEditText.showDropDown();
                        }
                    }
                    if (stationEditText == mStartStation) {
                        mStartStationProgressBar.setVisibility(View.GONE);
                    } else if (stationEditText == mEndStation) {
                        mEndStationProgressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(int errorCode, String errorText) {
                    if (stationEditText == mStartStation) {
                        mStartStationProgressBar.setVisibility(View.GONE);
                    } else if (stationEditText == mEndStation) {
                        mEndStationProgressBar.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    private void showDialog(int id) {
        mDialog = null;
        switch (id) {
            case BUS_TRANSFER_SEARCH_DLG:
                ProgressDialog prgDlg = new ProgressDialog(mActivity);
                prgDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                prgDlg.setMessage(getResources().getString(R.string.find_transfer_information));
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

    private class TransferSchemeAdapter extends BaseAdapter {

        private ArrayList<KXTransferProgram> mTransferList;

        public TransferSchemeAdapter(ArrayList<KXTransferProgram> transferList) {
            mTransferList = transferList;
        }

        @Override
        public int getCount() {
            return mTransferList.size();
        }

        @Override
        public Object getItem(int position) {
            return mTransferList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            KXTransferProgram program = mTransferList.get(position);
            if (convertView == null) {
                convertView = mActivity.getLayoutInflater().inflate(R.layout.bus_transfer_scheme_item, null);
                TransferProgramHolder holder = new TransferProgramHolder();
                holder.mTransferTitle = (TextView)convertView.findViewById(R.id.transfer_title);
                holder.mTransferDetail = (TextView)convertView.findViewById(R.id.transfer_detail);
                convertView.setTag(holder);
            }
            bindView(program, convertView);
            return convertView;
        }

        private void bindView(KXTransferProgram program, View view) {
            TransferProgramHolder holder = (TransferProgramHolder)view.getTag();
            String id = program.getProgramId();
            String transferCount = program.getTransferTime();
            StringBuilder transferInfo = new StringBuilder();
            StringBuilder transferDetail = new StringBuilder();
            List<KXProgramStep> steps = program.getSteps();
            for (int i=0; i < steps.size(); i++) {
                KXProgramStep step = steps.get(i);
                if (i == 0) {
                    transferInfo.append(step.getLineName());
                    transferDetail.append(mActivity.getString(R.string.transfer_scheme_detail, step.getSource(), step.getLineName(), step.getDestination()));
                } else {
                    transferInfo.append(mActivity.getResources().getString(R.string.transfer_arrow) + step.getLineName());
                    transferDetail.append("<br/>" + mActivity.getString(R.string.transfer_scheme_detail, step.getSource(), step.getLineName(), step.getDestination()));
                }
            }
            String title = mActivity.getString(R.string.transfer_scheme_title, id, transferCount, transferInfo.toString());
            String detail = transferDetail.toString();
            holder.mTransferTitle.setText(title);
            holder.mTransferDetail.setText(Html.fromHtml(detail));
        }

        private class TransferProgramHolder {
            TextView mTransferTitle;
            TextView mTransferDetail;
        }
    }


}
