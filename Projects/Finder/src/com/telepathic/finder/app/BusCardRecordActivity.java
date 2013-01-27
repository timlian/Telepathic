package com.telepathic.finder.app;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.telepathic.finder.R;
import com.telepathic.finder.app.CardIdFragment.OnCardSelectedListener;
import com.telepathic.finder.app.MessageDispatcher.IMessageHandler;
import com.telepathic.finder.sdk.ITrafficService;
import com.telepathic.finder.sdk.ITrafficeMessage;
import com.telepathic.finder.sdk.traffic.BusCard;
import com.telepathic.finder.sdk.traffic.ConsumerRecord;
import com.telepathic.finder.sdk.traffic.ConsumerRecord.ConsumerType;
import com.telepathic.finder.util.Utils;
import com.telepathic.finder.view.DropRefreshListView;
import com.telepathic.finder.view.DropRefreshListView.OnRefreshListener;

public class BusCardRecordActivity extends FragmentActivity {
    private static final String TAG = BusCardRecordActivity.class.getSimpleName();

    private Button mSendButton;
    private TextView mResidualCountText;
    private TextView mResidualAmountText;
    private AutoCompleteTextView mEditText;
    private DropRefreshListView mRecordList;
    private ConsumerRecordsAdapter mListAdapter;
    private CardIdFragment mFragment;
    private ArrayList<String> mCardIdList;
    private ProgressDialog mWaitingDialog;
	private MessageDispatcher mMessageDispatcher;
    private ITrafficService mTrafficService;
    private volatile boolean isCanceled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consumer_records);
        Utils.copyAppDatabaseFiles(getPackageName());
        FinderApplication app = (FinderApplication) getApplication();
        mTrafficService = app.getTrafficService();
        mMessageDispatcher = app.getMessageDispatcher();
		mMessageDispatcher.add(new IMessageHandler() {
			@Override
			public int what() {
				return ITrafficeMessage.RECEIVED_CONSUMER_RECORDS;
			}

			@Override
			public void handleMessage(Message msg) {
				mSendButton.setEnabled(true);
				mWaitingDialog.dismiss();
				final int errorCode = msg.arg2;
				if (errorCode == 0) {
					BusCard dataInfo = (BusCard) msg.obj;
					onReceived(dataInfo);
				} else {
					showMessage("Get consumer records failed: " + errorCode);
				}
			}
		});
		
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initView() {
    	 mEditText = (AutoCompleteTextView) findViewById(R.id.searchkey);
         mSendButton = (Button) findViewById(R.id.search);
         mSendButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 String cardId = mEditText.getText().toString();
                 if (Utils.isValidBusCardNumber(cardId)) {
                     mTrafficService.getConsumerRecords(cardId, 30);
                     mSendButton.setEnabled(false);
                     Utils.hideSoftKeyboard(getApplicationContext(), mEditText);
                     mWaitingDialog.show();
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
         mWaitingDialog = createWaitingDialog();
    }
    
    private void onReceived(BusCard busCard) {
    	Utils.addCachedCards(BusCardRecordActivity.this,busCard.getCardNumber());
		String resiaualCount = getString(R.string.residual_count, busCard.getResidualCount());
		String resiaualAmount = getString(R.string.residual_amount, busCard.getResidualAmount());
		mResidualCountText.setText(resiaualCount);
		mResidualAmountText.setText(resiaualAmount);
		mListAdapter.updateRecords(busCard.getConsumerRecords());
		mFragment.selectItemByCardId(busCard.getCardNumber());
		refreshCardIDCache();
		mRecordList.onRefreshComplete();
    }
    
    private void selectConsumptionRecordsByIndex(int index){
        selectConsumptionRecordsByCardId(mCardIdList.get(index));
    }

    private void selectConsumptionRecordsByCardId(String cardId){
        mEditText.setText(null);
        BusCard busCard = mTrafficService.getTrafficeStore().getConsumptionInfo(cardId);
        String resiaualCount  = getString(R.string.residual_count, busCard.getResidualCount());
        String resiaualAmount = getString(R.string.residual_amount, busCard.getResidualAmount());
        mResidualCountText.setText(resiaualCount);
        mResidualAmountText.setText(resiaualAmount);
        mListAdapter.updateRecords(busCard.getConsumerRecords());
    }

    private void refreshCardIDCache(){
        mCardIdList = Utils.getCachedCards(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(BusCardRecordActivity.this,
                android.R.layout.simple_dropdown_item_1line, mCardIdList);
        mEditText.setAdapter(adapter);
    }

    private ProgressDialog createWaitingDialog() {
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
    
    private void showMessage(String msgText) {
    	Toast.makeText(this, msgText, Toast.LENGTH_SHORT).show();
    }
    
}
