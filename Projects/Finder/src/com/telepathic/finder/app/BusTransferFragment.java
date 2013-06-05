package com.telepathic.finder.app;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.telepathic.finder.R;
import com.telepathic.finder.sdk.ICompletionListener;
import com.telepathic.finder.sdk.ITrafficService;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXProgramStep;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXTransferProgram;
import com.telepathic.finder.util.UmengEvent;
import com.telepathic.finder.util.Utils;
import com.umeng.analytics.MobclickAgent;

public class BusTransferFragment extends SherlockFragment {
    private static final String TAG = BusTransferFragment.class.getSimpleName();

    private static final int CUSTOM_DIALOG_ID_START = 100;

    private static final int BUS_TRANSFER_SEARCH_DLG = CUSTOM_DIALOG_ID_START + 1;

    private MainActivity mActivity;

    private AutoCompleteTextView mStartStation;

    private AutoCompleteTextView mEndStation;

    private ListView mTransferList;

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        Utils.debug(TAG,
                "onCreateOptionsMenu: " + Utils.formatTime(new Date(System.currentTimeMillis())));
        // Inflate the options menu from XML
        inflater.inflate(R.menu.menu_bus_transfer, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.get_bus_transfer:
                String startStationName = mStartStation.getText().toString().trim();
                String endStationName = mEndStation.getText().toString().trim();
                getBusTransfer(startStationName, endStationName);
                return true;
            case R.id.about:
                MobclickAgent.onEvent(mActivity, UmengEvent.OTHER_ABOUT);
                startActivity(new Intent(mActivity, AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupView() {
        mStartStation = (AutoCompleteTextView)getView().findViewById(R.id.start_station);
        mStartStation.setThreshold(2);
        mStartStation.addTextChangedListener(new StationNameWatcher(mStartStation));

        mEndStation = (AutoCompleteTextView)getView().findViewById(R.id.end_station);
        mEndStation.setThreshold(2);
        mEndStation.addTextChangedListener(new StationNameWatcher(mEndStation));

        mTransferList = (ListView)getView().findViewById(R.id.transfer_list);

        mStartStationProgressBar = (ProgressBar)getView().findViewById(R.id.start_station_progressbar);
        mEndStationProgressBar = (ProgressBar)getView().findViewById(R.id.end_station_progressbar);
    }

    private void getBusTransfer(String startStationName, String endStationName) {
        if (TextUtils.isEmpty(startStationName) || TextUtils.isEmpty(endStationName)) {
            Toast.makeText(mActivity, R.string.no_station_name_tips, Toast.LENGTH_SHORT).show();
        } else {
            MobclickAgent.onEvent(mActivity, UmengEvent.TRANSFER_START, startStationName);
            MobclickAgent.onEvent(mActivity, UmengEvent.TRANSFER_END, endStationName);
            MobclickAgent.onEvent(mActivity, UmengEvent.TRANSFER_FROM_TO, startStationName + "-" + endStationName);
            showDialog(BUS_TRANSFER_SEARCH_DLG);
            getBusTransferRoute(Utils.completeStationName(startStationName), Utils.completeStationName(endStationName));
            Utils.hideSoftKeyboard(mActivity, mEndStation);
        }
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

        private ArrayList<View> mItemViewList = new ArrayList<View>();

        private boolean init = true;

        public TransferSchemeAdapter(ArrayList<KXTransferProgram> transferList) {
            mTransferList = transferList;
            for(int i = 0; i < transferList.size(); i++) {
                mItemViewList.add(null);
            }
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
            View itemView = mItemViewList.get(position);
            if (itemView == null){
                itemView = mActivity.getLayoutInflater().inflate(R.layout.bus_transfer_scheme_item, null);
                TransferProgramHolder holder = new TransferProgramHolder();
                holder.mTransferId = (TextView)itemView.findViewById(R.id.transfer_id);
                holder.mTransferTitle = (TextView)itemView.findViewById(R.id.transfer_title);
                holder.mExtend = (ImageView)itemView.findViewById(R.id.transfer_extend);
                holder.mTransferContent = (LinearLayout)itemView.findViewById(R.id.transfer_content);
                holder.mTransferStart = (TextView)itemView.findViewById(R.id.transfer_start_station);
                holder.mTransferStepsLayout = (LinearLayout)itemView.findViewById(R.id.transfer_steps);
                holder.mTransferEnd = (TextView)itemView.findViewById(R.id.transfer_end_station);
                itemView.setTag(holder);
                mItemViewList.set(position, itemView);
            }
            bindView(program, itemView, position);
            return itemView;
        }

        private void bindView(KXTransferProgram program, View view, int position) {
            final TransferProgramHolder holder = (TransferProgramHolder)view.getTag();
            String id = program.getProgramId();
            String transferCount = program.getTransferTime();
            StringBuilder transferInfo = new StringBuilder();
            List<KXProgramStep> steps = program.getSteps();
            holder.mTransferStepsLayout.removeAllViews();
            for (int i=0; i < steps.size(); i++) {
                KXProgramStep step = steps.get(i);
                if (i == 0) {
                    transferInfo.append(step.getLineName());
                } else {
                    transferInfo.append(mActivity.getResources().getString(R.string.transfer_arrow) + step.getLineName());
                }
                View transferStepView = mActivity.getLayoutInflater().inflate(R.layout.transfer_step_item, null);
                TextView stepInfo = (TextView)transferStepView.findViewById(R.id.transfer_step);
                String stepContent = mActivity.getString(R.string.transfer_step_content, step.getLineName(), step.getDestination());
                stepInfo.setText(Html.fromHtml(stepContent));
                holder.mTransferStepsLayout.addView(transferStepView);
            }
            String title;
            if (transferCount.equals("0")) {
                title = getString(R.string.direct_title, transferInfo.toString());
            } else {
                title = getString(R.string.transfer_scheme_title, transferCount, transferInfo.toString());
            }
            holder.mTransferId.setText(id);
            holder.mTransferTitle.setText(title);
            holder.mTransferStart.setText(steps.get(0).getSource());
            holder.mTransferEnd.setText(steps.get(steps.size()-1).getDestination());

            holder.mExtend.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (holder.mTransferContent.getVisibility() == View.GONE) {
                        holder.mTransferContent.setVisibility(View.VISIBLE);
                        holder.mExtend.setImageResource(R.drawable.contract_arrow);
                    } else if (holder.mTransferContent.getVisibility() == View.VISIBLE) {
                        holder.mTransferContent.setVisibility(View.GONE);
                        holder.mExtend.setImageResource(R.drawable.extend_arrow);
                    }
                }
            });

            if (init && position == 0) {
                init = false;
                holder.mTransferContent.setVisibility(View.VISIBLE);
                holder.mExtend.setImageResource(R.drawable.contract_arrow);
            }
        }

        private class TransferProgramHolder {
            TextView mTransferId;
            TextView mTransferTitle;
            ImageView mExtend;
            LinearLayout mTransferContent;
            TextView mTransferStart;
            LinearLayout mTransferStepsLayout;
            TextView mTransferEnd;
        }
    }


}
