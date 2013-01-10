package com.telepathic.finder.app;

import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.telepathic.finder.R;
import com.telepathic.finder.sdk.BusLineRoute;
import com.telepathic.finder.sdk.ConsumerRecord;
import com.telepathic.finder.sdk.ConsumerRecord.ConsumerType;
import com.telepathic.finder.sdk.ConsumerRecordsListener;
import com.telepathic.finder.sdk.TrafficService;
import com.telepathic.finder.util.Utils;

public class ConsumerRecordsActivity extends Activity {
    private static final String TAG = "TestActivity";
    private static final int DIALOG_WAITING = 2;
    private static final String CARD_ID_CACHE = "card_id_cache";

    private Button mSendButton;
    private AutoCompleteTextView mEditText;
    private ListView mRecordList;
    private ConsumerRecordsAdapter mListAdapter;

    private TrafficService mTrafficService;
    private BusLineRoute mBusLineRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consumer_records);

        mTrafficService = TrafficService.getTrafficService(null);

        mEditText = (AutoCompleteTextView) findViewById(R.id.searchkey);
        mSendButton = (Button) findViewById(R.id.search);
        mSendButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mTrafficService.getBusStationLines();
                String number = mEditText.getText().toString();
                if (number.length() == 8) {
                    SharedPreferences preferences = getSharedPreferences(CARD_ID_CACHE, MODE_PRIVATE);
                    preferences.edit().putString(number, number).commit();
                    mTrafficService.retrieveConsumerRecords(number, 30, new MyChargeRecordsListener());
                    mSendButton.setEnabled(false);
                    Utils.hideSoftKeyboard(getApplicationContext(), mEditText);
                    showDialog(DIALOG_WAITING);
                    refreshCardIDCache();
                } else {
                    mEditText.setError(getResources().getString(R.string.card_id_error_notice));
                }
            }
        });
        refreshCardIDCache();

        mListAdapter = new ConsumerRecordsAdapter();
        mRecordList = (ListView) findViewById(R.id.consumer_record_list);
        mRecordList.setAdapter(mListAdapter);
    }

    private void refreshCardIDCache(){
        SharedPreferences sharedPreferences = getSharedPreferences(CARD_ID_CACHE, MODE_PRIVATE);

        Map<String, ?> map = sharedPreferences.getAll();
        ArrayList<String> list = new ArrayList<String>();
        if (map.keySet() != null) {
            list.addAll(map.keySet());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ConsumerRecordsActivity.this,
                android.R.layout.simple_dropdown_item_1line, list);
        mEditText.setAdapter(adapter);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_WAITING) {
            ProgressDialog prgDlg = new ProgressDialog(this);
            prgDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            prgDlg.setMessage(getResources().getString(R.string.find_ic_card_records));
            prgDlg.setIndeterminate(true);
            prgDlg.setCancelable(false);
            return prgDlg;
        }
        return null;
    }

    private class MyChargeRecordsListener implements ConsumerRecordsListener {

        @Override
        public void onSuccess(final ArrayList<ConsumerRecord> consumerRecords) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSendButton.setEnabled(true);
                    mListAdapter.updateRecords(consumerRecords);
                    removeDialog(DIALOG_WAITING);
                }
            });

        }

        @Override
        public void onError(final String errorMessage) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSendButton.setEnabled(true);
                    removeDialog(DIALOG_WAITING);
                    Toast.makeText(ConsumerRecordsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            });

        }
    }

    private static class RecordItemHolder {
        TextView lineNumber;
        TextView busNumber;
        TextView consumerCount;
        TextView residualCount;
        TextView consumerTime;
        TextView residualAmount;
    }

    private class ConsumerRecordsAdapter extends BaseAdapter {
        private ArrayList<ConsumerRecord> mRecords;

        ConsumerRecordsAdapter() {
            mRecords = new ArrayList<ConsumerRecord>();
        }

        public void updateRecords(ArrayList<ConsumerRecord> records) {
            mRecords = records;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mRecords.size();
        }

        @Override
        public Object getItem(int position) {
            return mRecords.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ConsumerRecord record = mRecords.get(position);
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.consumer_record_item, parent, false);
                RecordItemHolder holder = new RecordItemHolder();
                holder.lineNumber = (TextView) convertView.findViewById(R.id.line_number);
                holder.busNumber = (TextView) convertView.findViewById(R.id.bus_number);
                holder.consumerCount = (TextView) convertView.findViewById(R.id.consumer_count);
                holder.residualCount = (TextView) convertView.findViewById(R.id.residual_count);
                holder.consumerTime = (TextView) convertView.findViewById(R.id.consumer_time);
                holder.residualAmount = (TextView) convertView.findViewById(R.id.residual_amount);
                convertView.setTag(holder);
            }
            bindView(position, record, convertView);
            return convertView;
        }

        private void bindView(int position, ConsumerRecord record, View view) {
            RecordItemHolder holder = (RecordItemHolder) view.getTag();
            holder.lineNumber.setText(getResources().getString(R.string.line_number) + record.getLineNumber());
            holder.busNumber.setText(getResources().getString(R.string.bus_number) + record.getBusNumber());
            if (record.getConsumerType() == ConsumerType.COUNT) {
                holder.consumerCount.setText(getResources().getString(R.string.consumer_count) + record.getConsumerCount());
            } else if (record.getConsumerType() == ConsumerType.ELECTRONIC_WALLET) {
                holder.consumerCount.setText(getResources().getString(R.string.consumer_amount) + record.getConsumerAmount());
            }
            holder.consumerTime.setText(Utils.formatDate(record.getConsumerTime()));
            if (position == 0) {
                holder.residualCount.setText(getResources().getString(R.string.residual_count) + record.getResidualCount());
                holder.residualAmount.setText(getResources().getString(R.string.residual_amount) + record.getResidualAmount());
                holder.residualCount.setVisibility(View.VISIBLE);
                holder.residualAmount.setVisibility(View.VISIBLE);
            } else {
                holder.residualCount.setVisibility(View.GONE);
                holder.residualAmount.setVisibility(View.GONE);
            }

        }

    }

}
