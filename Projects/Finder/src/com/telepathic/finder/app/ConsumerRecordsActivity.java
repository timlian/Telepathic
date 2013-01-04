package com.telepathic.finder.app;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.telepathic.finder.R;
import com.telepathic.finder.sdk.BusLineRoute;
import com.telepathic.finder.sdk.ChargeRecordsListener;
import com.telepathic.finder.sdk.ConsumerRecord;
import com.telepathic.finder.sdk.TrafficService;
import com.telepathic.finder.util.Utils;

public class ConsumerRecordsActivity extends Activity {
    private static final String TAG = "TestActivity";
    private static final int DIALOG_WAITING = 2;

    private Button mSendButton;
    private EditText mEditText;
    private ListView mRecordList;
    private ConsumerRecordsAdapter mListAdapter;

    private TrafficService mTrafficService;
    private BusLineRoute mBusLineRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consumer_records);

        mTrafficService = TrafficService.getTrafficService(null);

        mEditText = (EditText) findViewById(R.id.searchkey);
        mSendButton = (Button) findViewById(R.id.search);
        mSendButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mTrafficService.getBusStationLines();
                String number = mEditText.getText().toString();
                if (number.length() == 8) {
                    mTrafficService.getChargeRecords(number, 30, new MyChargeRecordsListener());
                    mSendButton.setEnabled(false);
                    Utils.hideSoftKeyboard(getApplicationContext(), mEditText);
                    showDialog(DIALOG_WAITING);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter the correct parameter.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        mListAdapter = new ConsumerRecordsAdapter();
        mRecordList = (ListView) findViewById(R.id.consumer_record_list);
        mRecordList.setAdapter(mListAdapter);
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

    private class MyChargeRecordsListener implements ChargeRecordsListener {

        @Override
        public void onSuccess(final ArrayList<ConsumerRecord> consumerRecords) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSendButton.setEnabled(true);
                    mListAdapter.updateRecords(consumerRecords);
                    dismissDialog(DIALOG_WAITING);
                }
            });

        }

        @Override
        public void onError(final String errorMessage) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSendButton.setEnabled(true);
                    dismissDialog(DIALOG_WAITING);
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
                convertView.setTag(holder);
            }
            bindView(record, convertView);
            return convertView;
        }

        private void bindView(ConsumerRecord record, View view) {
            RecordItemHolder holder = (RecordItemHolder) view.getTag();
            holder.lineNumber.setText(getResources().getString(R.string.line_number) + record.getLineNumber());
            holder.busNumber.setText(getResources().getString(R.string.bus_number) + record.getBusNumber());
            holder.consumerCount.setText(getResources().getString(R.string.consumer_count) + record.getConsumerCount());
            holder.residualCount.setText(getResources().getString(R.string.residual_count) + record.getResidualCount());
            holder.consumerTime.setText(record.getConsumerTime());
        }

    }

}
