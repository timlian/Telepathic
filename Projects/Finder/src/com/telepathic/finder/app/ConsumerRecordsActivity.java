package com.telepathic.finder.app;

import java.util.ArrayList;

import javax.xml.transform.ErrorListener;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.telepathic.finder.R;
import com.telepathic.finder.app.CardIdFragment.OnCardSelectedListener;
import com.telepathic.finder.sdk.ConsumerRecord;
import com.telepathic.finder.sdk.ConsumerRecord.ConsumerType;
import com.telepathic.finder.sdk.ConsumptionInfo;
import com.telepathic.finder.sdk.ITrafficService;
import com.telepathic.finder.sdk.TrafficListeners;
import com.telepathic.finder.sdk.TrafficManager;
import com.telepathic.finder.util.Utils;
import com.telepathic.finder.view.DropRefreshListView;
import com.telepathic.finder.view.DropRefreshListView.OnRefreshListener;

public class ConsumerRecordsActivity extends FragmentActivity {
    private static final String TAG = "ConsumerRecordsActivity";
    private static final int DIALOG_WAITING = 2;

    private Button mSendButton;
    private TextView mResidualCountText;
    private TextView mResidualAmountText;
    private AutoCompleteTextView mEditText;
    private DropRefreshListView mRecordList;
    private ConsumerRecordsAdapter mListAdapter;
    private CardIdFragment mFragment;
    private ArrayList<String> mCardIdList;

    private ITrafficService mTrafficService;
    
    private TrafficListeners.ErrorListener mErrorListener = new TrafficListeners.ErrorListener() {
		@Override
		public void done(final String error) {
			runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSendButton.setEnabled(true);
                    removeDialog(DIALOG_WAITING);
                    if (!isCanceled) {
                        Toast.makeText(ConsumerRecordsActivity.this, error, Toast.LENGTH_LONG).show();
                    } else {
                        isCanceled = false;
                    }
                    mRecordList.onRefreshComplete();
                }
            });
		}
	};
	
	private TrafficListeners.ConsumerRecordsListener mConsumerRecordsListener = new TrafficListeners.ConsumerRecordsListener() {
		
		@Override
		public void onReceived(final ConsumptionInfo dataInfo) {
			 runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     mSendButton.setEnabled(true);
                     Utils.addCachedCards(ConsumerRecordsActivity.this, dataInfo.getCardId());
                     String resiaualCount  = getString(R.string.residual_count, dataInfo.getResidualCount());
                     String resiaualAmount = getString(R.string.residual_amount, dataInfo.getResidualAmount());
                     mResidualCountText.setText(resiaualCount);
                     mResidualAmountText.setText(resiaualAmount);
                     mListAdapter.updateRecords(dataInfo.getRecordList());
                     mFragment.selectItemByCardId(dataInfo.getCardId());
                     removeDialog(DIALOG_WAITING);
                     refreshCardIDCache();
                     mRecordList.onRefreshComplete();
                 }
             });
		}
	};

    private volatile boolean isCanceled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consumer_records);

        mTrafficService = TrafficManager.getTrafficManager(null, getApplicationContext()).getTrafficService();

        mEditText = (AutoCompleteTextView) findViewById(R.id.searchkey);
        mSendButton = (Button) findViewById(R.id.search);
        mSendButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String cardId = mEditText.getText().toString();
                if (cardId.length() == 8) {
                    mTrafficService.getConsumerRecords(cardId, 30);
                    mSendButton.setEnabled(false);
                    Utils.hideSoftKeyboard(getApplicationContext(), mEditText);
                    showDialog(DIALOG_WAITING);
                } else {
                    mEditText.setError(getResources().getString(R.string.card_id_error_notice));
                }
            }
        });
        refreshCardIDCache();

        mListAdapter = new ConsumerRecordsAdapter();
        mRecordList = (DropRefreshListView) findViewById(R.id.consumer_record_list);
        mRecordList.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                mTrafficService.getConsumerRecords(mFragment.getSelectedCardId(), 30);
            }
        });
        mRecordList.setAdapter(mListAdapter);

        mResidualCountText = (TextView) findViewById(R.id.residual_count_text);
        mResidualAmountText = (TextView) findViewById(R.id.residual_amount_text);

        mFragment = (CardIdFragment)getSupportFragmentManager().findFragmentById(R.id.card_id_list);
        mFragment.setOnCardSelectedListener(new OnCardSelectedListener() {
            @Override
            public void onCardSelected(String cardId) {
                selectConsumptionRecordsByCardId(cardId);
            }
        });

        if (mCardIdList.size() > 0){
            selectConsumptionRecordsByIndex(0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTrafficService.getTrafficMonitor().add(mErrorListener);
        mTrafficService.getTrafficMonitor().add(mConsumerRecordsListener);
        
    }

    @Override
    protected void onPause() {
    	super.onPause();
    	mTrafficService.getTrafficMonitor().remove(mErrorListener);
    	mTrafficService.getTrafficMonitor().remove(mConsumerRecordsListener);
    }
    
    private void selectConsumptionRecordsByIndex(int index){
        selectConsumptionRecordsByCardId(mCardIdList.get(index));
    }

    private void selectConsumptionRecordsByCardId(String cardId){
        mEditText.setText(null);
        //ConsumptionInfo dataInfo = mTrafficService.getConsumptionStore().getConsumptionInfo(cardId);
//        String resiaualCount  = getString(R.string.residual_count, dataInfo.getResidualCount());
//        String resiaualAmount = getString(R.string.residual_amount, dataInfo.getResidualAmount());
//        mResidualCountText.setText(resiaualCount);
//        mResidualAmountText.setText(resiaualAmount);
//        mListAdapter.updateRecords(dataInfo.getRecordList());
    }

    private void refreshCardIDCache(){
        mCardIdList = Utils.getCachedCards(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ConsumerRecordsActivity.this,
                android.R.layout.simple_dropdown_item_1line, mCardIdList);
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
                  //  mTrafficService.cancelRetrieve();
                    mSendButton.setEnabled(true);
                }
            });
            return prgDlg;
        }
        return null;
    }

    private static class RecordItemHolder {
        TextView lineNumber;
        TextView busNumber;
        TextView consumption;
        TextView consumerTime;
        ImageView consumeType;
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
                holder.consumption = (TextView) convertView.findViewById(R.id.consumption);
                holder.consumerTime = (TextView) convertView.findViewById(R.id.consumer_time);
                holder.consumeType = (ImageView) convertView.findViewById(R.id.img_consumption_type);
                convertView.setTag(holder);
            }
            bindView(position, record, convertView);
            return convertView;
        }

        private void bindView(int position, ConsumerRecord record, View view) {
            RecordItemHolder holder = (RecordItemHolder) view.getTag();
            holder.lineNumber.setText(getResources().getString(R.string.line_number) + record.getLineNumber());
            holder.busNumber.setText(getResources().getString(R.string.bus_number) + record.getBusNumber());
            String comsumption = "";
            if (record.getType() == ConsumerType.COUNT) {
                comsumption = getString(R.string.consumer_count, record.getConsumption());
                holder.consumeType.setImageResource(R.drawable.count);
            } else if (record.getType() == ConsumerType.EWALLET) {
                comsumption = getString(R.string.consumer_amount, record.getConsumption());
                holder.consumeType.setImageResource(R.drawable.ewallet);
            }
            holder.consumption.setText(comsumption);
            holder.consumerTime.setText(Utils.formatDate(record.getConsumerTime()));
        }

    }

}
