package com.telepathic.finder.app;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Loader;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.telepathic.finder.R;
import com.telepathic.finder.sdk.ITrafficService;
import com.telepathic.finder.sdk.traffic.entity.ConsumerRecord.ConsumerType;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData;
import com.telepathic.finder.util.Utils;
import com.telepathic.finder.view.DropRefreshListView;
import com.telepathic.finder.view.DropRefreshListView.OnRefreshListener;

public class BusCardRecordActivity extends Activity {
    private static final String TAG = BusCardRecordActivity.class.getSimpleName();
    private static final int BUS_CARD_LOADER_ID = 100;
    private static final int CONSUMER_RECORD_LOADER_ID = 200;
    private static final String CARD_NUMBER = "card_number";
    private Button mSendButton;
    private TextView mResidualCountText;
    private TextView mResidualAmountText;
    private AutoCompleteTextView mEditText;
    private DropRefreshListView mRecordList;
    private ProgressDialog mWaitingDialog;
    private ITrafficService mTrafficService;
    private ConsumerRecordAdapter mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consumer_records);
        Utils.copyAppDatabaseFiles(getPackageName());
        FinderApplication app = (FinderApplication) getApplication();
        mTrafficService = app.getTrafficService();
        getLoaderManager().initLoader(BUS_CARD_LOADER_ID, null, new BusCardLoaderCallback());
        
        getContentResolver().registerContentObserver(ITrafficData.BusCard.CONTENT_URI, true, new ContentObserver(new Handler()) {
        	@Override
        	public boolean deliverSelfNotifications() {
        		Utils.debug(TAG, "ContentObserver - deliverSelfNotifications()");
        		return super.deliverSelfNotifications();
        	}
        	
        	@Override
        	public void onChange(boolean selfChange) {
        		Utils.debug(TAG, "ContentObserver - ITrafficData.BusCard onChange()");
        		super.onChange(selfChange);
        	}
		});
        getContentResolver().registerContentObserver(ITrafficData.ConsumerRecord.CONTENT_URI, true, new ContentObserver(new Handler()) {
        	@Override
        	public boolean deliverSelfNotifications() {
        		Utils.debug(TAG, "ContentObserver - deliverSelfNotifications()");
        		return super.deliverSelfNotifications();
        	}
        	
        	@Override
        	public void onChange(boolean selfChange) {
        		Utils.debug(TAG, "ContentObserver - ITrafficData.ConsumerRecord onChange()");
        		super.onChange(selfChange);
        	}
		});
        initView();
    }

    private Context getContext() {
    	return this;
    }
    
    private class BusCardLoaderCallback implements LoaderCallbacks<Cursor> {
		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			Utils.debug(TAG, "BusCardLoaderCallback - onCreateLoader: " + id);
			CursorLoader loader = null;
			switch(id) {
			case BUS_CARD_LOADER_ID:
				loader = new CursorLoader(getContext(), ITrafficData.BusCard.CONTENT_URI, null, null, null, null);
				break;
			case CONSUMER_RECORD_LOADER_ID:
				String cardNumber = args.getString(CARD_NUMBER);
				String selection = ITrafficData.BusCard.CARD_NUMBER + "=" + "\'" + cardNumber + "\'";
				String sortOrder = ITrafficData.ConsumerRecord.DATE + " DESC";
				loader = new CursorLoader(getContext(), ITrafficData.ConsumerRecord.CONTENT_URI, null, selection, null, sortOrder);
				break;
			default:
				break;
			}
			return loader;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			Utils.debug(TAG, "BusCardLoaderCallback - onLoadFinished: " + loader.getId());
			switch (loader.getId()) {
			case CONSUMER_RECORD_LOADER_ID:
				if (mListAdapter == null) {
					mListAdapter = new ConsumerRecordAdapter(data);
					mRecordList.setAdapter(mListAdapter);
				}
				mListAdapter.swapCursor(data);
				break;
			case BUS_CARD_LOADER_ID:
				do {
					final int idxCardNumber = data.getColumnIndex(ITrafficData.BusCard.CARD_NUMBER);
					final int idxResidualCount = data.getColumnIndex(ITrafficData.BusCard.RESIDUAL_COUNT);
					final int idxResidualAmount = data.getColumnIndex(ITrafficData.BusCard.RESIDUAL_AMOUNT);
					String resiaualCount = getString(R.string.residual_count, data.getString(idxResidualCount));
	                String resiaualAmount = getString(R.string.residual_amount, data.getString(idxResidualAmount));
					mResidualCountText.setText(resiaualCount);
					mResidualAmountText.setText(resiaualAmount);
					Bundle args = new Bundle();
					args.putString(CARD_NUMBER, data.getString(idxCardNumber));
					Loader<Cursor> loader2 = getLoaderManager().restartLoader(CONSUMER_RECORD_LOADER_ID, args, this);
					Utils.debug(TAG, "loader return: " + loader2);
				} while(data.moveToNext());
				break;
			default:
				break;
			}
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			Utils.debug(TAG, "BusCardLoaderCallback - onLoaderReset: " + loader.getId());
			mListAdapter.swapCursor(null);
		}
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
                final String cardNumber = mEditText.getText().toString();
                if (Utils.isValidBusCardNumber(cardNumber)) {
                    mTrafficService.getBusCardRecords(cardNumber, 30);
                    mSendButton.setEnabled(false);
                    mWaitingDialog.show();
                    Utils.hideSoftKeyboard(getApplicationContext(), mEditText);
                } else {
                    mEditText.setError(getResources().getString(R.string.card_id_error_notice));
                    getLoaderManager().restartLoader(BUS_CARD_LOADER_ID, null, new BusCardLoaderCallback());
                }
            }
        });

        mRecordList = (DropRefreshListView) findViewById(R.id.consumer_record_list);
        mRecordList.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                //mTrafficService.getConsumerRecords(mFragment.getSelectedCardId(), 30);
            }
        });
        mResidualCountText = (TextView) findViewById(R.id.residual_count_text);
        mResidualAmountText = (TextView) findViewById(R.id.residual_amount_text);
        mWaitingDialog = createWaitingDialog();
    }

    private ProgressDialog createWaitingDialog() {
        ProgressDialog prgDlg = new ProgressDialog(this);
        prgDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        prgDlg.setMessage(getResources().getString(R.string.find_ic_card_records));
        prgDlg.setIndeterminate(true);
        prgDlg.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
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

    private class ConsumerRecordAdapter extends CursorAdapter {
    	private LayoutInflater mInflater;

		public ConsumerRecordAdapter(Cursor c) {
			super(BusCardRecordActivity.this, c, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
			mInflater = getLayoutInflater();
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View convertView = mInflater.inflate(R.layout.consumer_record_item, parent, false);
			RecordItemHolder holder = new RecordItemHolder();
			holder.lineNumber = (TextView) convertView.findViewById(R.id.line_number);
			holder.busNumber = (TextView) convertView.findViewById(R.id.bus_number);
			holder.consumption = (TextView) convertView.findViewById(R.id.consumption);
			holder.consumerTime = (TextView) convertView.findViewById(R.id.consumer_time);
			holder.consumeType = (ImageView) convertView.findViewById(R.id.img_consumption_type);
			convertView.setTag(holder);
			return convertView;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			if (cursor != null) {
				RecordItemHolder holder = (RecordItemHolder) view.getTag();
				final int idxLineNumber = cursor.getColumnIndex(ITrafficData.ConsumerRecord.LINE_NUMBER);
				final int idxBusNumber = cursor.getColumnIndex(ITrafficData.ConsumerRecord.BUS_NUMBER);
				final int idxConsumption = cursor.getColumnIndex(ITrafficData.ConsumerRecord.CONSUMPTION);
				final int idxDate = cursor.getColumnIndex(ITrafficData.ConsumerRecord.DATE);
				final int idxType = cursor.getColumnIndex(ITrafficData.ConsumerRecord.TYPE);
				holder.lineNumber.setText(getResources().getString(R.string.line_number)+ cursor.getString(idxLineNumber));
				holder.busNumber.setText(getResources().getString(R.string.bus_number) + cursor.getString(idxBusNumber));
				String comsumption = "";
				ConsumerType type = ConsumerType.valueOf(cursor.getString(idxType));
				if (type == ConsumerType.COUNT) {
					comsumption = getString(R.string.consumer_count, cursor.getString(idxConsumption));
					holder.consumeType.setImageResource(R.drawable.count);
				} else if (type == ConsumerType.EWALLET) {
					comsumption = getString(R.string.consumer_amount,cursor.getString(idxConsumption));
					holder.consumeType.setImageResource(R.drawable.ewallet);
				}
				holder.consumption.setText(comsumption);
				holder.consumerTime.setText(cursor.getString(idxDate));
			}
		}
		
    }
    
    private void showMessage(String msgText) {
        Toast.makeText(this, msgText, Toast.LENGTH_SHORT).show();
    }
    
//    private class BusCardLoader {
//        private static final int BUS_CARD_LOADER = 100;
//        private static final int CONSUMER_RECORD_LOADER = 200;
//        private static final String CARD_NUMBER = "card_number";
//        
//    	void loadAllBusCard() {
//    		LoaderManager manager = getLoaderManager();
//    		if (manager.getLoader(BUS_CARD_LOADER) == null) {
//    			manager.initLoader(BUS_CARD_LOADER, null, new LoaderCallback());
//    		} else {
//    			manager.restartLoader(BUS_CARD_LOADER, null, new LoaderCallback());
//    		}
//    	}
//    	
//    	void loadConsumerRecords(String cardNumber) {
//    		LoaderManager manager = getLoaderManager();
//    		Bundle args = new Bundle();
//			args.putString(CARD_NUMBER, cardNumber);
//    		if (manager.getLoader(CONSUMER_RECORD_LOADER) == null) {
//    			manager.initLoader(CONSUMER_RECORD_LOADER, args, new LoaderCallback());
//    		} else {
//    			manager.restartLoader(CONSUMER_RECORD_LOADER, args, new LoaderCallback());
//    		}
//    	}
//    	
//    	private class LoaderCallback implements LoaderCallbacks<Cursor>{
//    		@Override
//    		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//    			Utils.debug(TAG, "BusCardLoaderCallback - onCreateLoader: " + id);
//    			CursorLoader loader = null;
//    			switch(id) {
//    			case BUS_CARD_LOADER:
//    				loader = new CursorLoader(getContext(), ITrafficData.BusCard.CONTENT_URI, null, null, null, null);
//    				break;
//    			case CONSUMER_RECORD_LOADER:
//    				//String cardNumber = args.getString(CARD_NUMBER);
//    				//String selection = ITrafficData.BusCard.CARD_NUMBER + "=" + "\'" + cardNumber + "\'";
//    				loader = new CursorLoader(getContext(), ITrafficData.ConsumerRecord.CONTENT_URI, null, null, null, null);
//    				break;
//    			default:
//    				break;
//    			}
//    			return loader;
//    		}
//
//    		@Override
//    		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
//    			Utils.debug(TAG, "BusCardLoaderCallback - onLoadFinished: " + loader.getId());
//    			if (loader.getId() == CONSUMER_RECORD_LOADER_ID) {
//    				if (mListAdapter == null) {
//    					mListAdapter = new ConsumerRecordAdapter(data);
//    					mRecordList.setAdapter(mListAdapter);
//    				}
//    				mListAdapter.swapCursor(data);
//    			} else {
//    				if (data.getCount() > 0) {
//    					final int idxCardNumber = data.getColumnIndex(ITrafficData.BusCard.CARD_NUMBER);
//    					final int idxResidualCount = data.getColumnIndex(ITrafficData.BusCard.RESIDUAL_COUNT);
//    					final int idxResidualAmount = data.getColumnIndex(ITrafficData.BusCard.RESIDUAL_AMOUNT);
//    					String resiaualCount = getString(R.string.residual_count, data.getString(idxResidualCount));
//    	                String resiaualAmount = getString(R.string.residual_amount, data.getString(idxResidualAmount));
//    					mResidualCountText.setText(resiaualCount);
//    					mResidualAmountText.setText(resiaualAmount);
//    					Bundle args = new Bundle();
//    					args.putString(CARD_NUMBER, data.getString(idxCardNumber));
//    					//getLoaderManager().initLoader(CONSUMER_RECORD_LOADER_ID, args, this);
//    				}
//    			}
//    		}
//
//    		@Override
//    		public void onLoaderReset(Loader<Cursor> loader) {
//    			Utils.debug(TAG, "BusCardLoaderCallback - onLoaderReset: " + loader.getId());
//    			mListAdapter.swapCursor(null);
//    		}
//    	}
//    }
//    
}
