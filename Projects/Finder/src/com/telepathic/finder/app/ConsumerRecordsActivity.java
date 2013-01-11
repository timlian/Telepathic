package com.telepathic.finder.app;

import java.util.ArrayList;
import java.util.Map;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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
import com.telepathic.finder.sdk.ConsumptionInfo;
import com.telepathic.finder.sdk.TrafficService;
import com.telepathic.finder.util.Utils;

public class ConsumerRecordsActivity extends FragmentActivity {
    private static final String TAG = "TestActivity";
    private static final int DIALOG_WAITING = 2;
    private static final String CARD_ID_CACHE = "card_id_cache";

    private Button mSendButton;
    private TextView mResidualCountText;
    private TextView mResidualAmountText;
    private AutoCompleteTextView mEditText;
    private ListView mRecordList;
    private ConsumerRecordsAdapter mListAdapter;

    private TrafficService mTrafficService;
    private BusLineRoute mBusLineRoute;

    private volatile boolean isCanceled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consumer_records);

        mTrafficService = TrafficService.getTrafficService(null, getApplicationContext());

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
        
        mResidualCountText = (TextView) findViewById(R.id.residual_count_text);
        mResidualAmountText = (TextView) findViewById(R.id.residual_amount_text);
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
            prgDlg.setOnCancelListener(new OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    isCanceled = true;
                    mTrafficService.cancelRetrieve();
                    mSendButton.setEnabled(true);
                }
            });
            return prgDlg;
        }
        return null;
    }

    private class MyChargeRecordsListener implements ConsumerRecordsListener {

        @Override
        public void onSuccess(final ConsumptionInfo dataInfo) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSendButton.setEnabled(true);
                    mResidualCountText.setText(getResources().getString(R.string.residual_count) + dataInfo.getResidualCount());
                    mResidualAmountText.setText(getResources().getString(R.string.residual_amount) + dataInfo.getResidualAmount());
                    mListAdapter.updateRecords(dataInfo.getRecordList());
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
                    if (!isCanceled) {
                        Toast.makeText(ConsumerRecordsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    } else {
                        isCanceled = false;
                    }
                }
            });

        }
    }

    private static class RecordItemHolder {
        TextView lineNumber;
        TextView busNumber;
        TextView consumerCount;
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
                holder.consumerTime = (TextView) convertView.findViewById(R.id.consumer_time);
                convertView.setTag(holder);
            }
            bindView(position, record, convertView);
            return convertView;
        }

        private void bindView(int position, ConsumerRecord record, View view) {
            RecordItemHolder holder = (RecordItemHolder) view.getTag();
            holder.lineNumber.setText(getResources().getString(R.string.line_number) + record.getLineNumber());
            holder.busNumber.setText(getResources().getString(R.string.bus_number) + record.getBusNumber());
            if (record.getType() == ConsumerType.COUNT) {
                holder.consumerCount.setText(getResources().getString(R.string.consumer_count) + record.getConsumption());
            } else if (record.getType() == ConsumerType.EWALLET) {
                holder.consumerCount.setText(getResources().getString(R.string.consumer_amount) + record.getConsumption());
            }
            holder.consumerTime.setText(Utils.formatDate(record.getConsumerTime()));
        }

    }

}
