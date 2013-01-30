
package com.telepathic.finder.app;

import java.util.ArrayList;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.telepathic.finder.R;
import com.telepathic.finder.sdk.ITrafficService;
import com.telepathic.finder.sdk.traffic.BusCard;
import com.telepathic.finder.sdk.traffic.ConsumerRecord.ConsumerType;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData;
import com.telepathic.finder.util.Utils;
import com.telepathic.finder.view.DropRefreshListView;

public class BusCardRecordActivity2 extends BaseActivity {
	private static int LOADER_ID = 1000;
    private Button mSendButton;
    private AutoCompleteTextView mEditText;
    private ViewPager mViewPager;
    private BusCardPageAdapter mViewPagerAdapter;
    private LinearLayout mNoItemTips;
    private RelativeLayout mConsumptionDetail;
    private final int mBusCardLoaderId = getLoaderId();
    private ArrayList<BusCard> mBusCards;
    private ITrafficService mTrafficService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_card_record);
        initView();
        FinderApplication app = (FinderApplication) getApplication();
        mTrafficService = app.getTrafficService();
        startLoadBusCards();
    }

    private void startLoadBusCards() {
    	getLoaderManager().initLoader(mBusCardLoaderId, null, new BusCardLoaderCallback());
    }
    
    private class BusCardLoaderCallback implements LoaderCallbacks<Cursor> {

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			CursorLoader loader = null;
			if (mBusCardLoaderId == id) {
				loader = new CursorLoader(getContext(), ITrafficData.BusCard.CONTENT_URI, null, null, null, null);
			}
			return loader;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			if (mBusCardLoaderId == loader.getId() && Utils.isValid(data)) {
				mBusCards = new ArrayList<BusCard>();
				 int idxCardNumber = data.getColumnIndex(ITrafficData.BusCard.CARD_NUMBER);
			     int idxResidualCount = data.getColumnIndex(ITrafficData.BusCard.RESIDUAL_COUNT);
			     int idxResidualAmount = data.getColumnIndex(ITrafficData.BusCard.RESIDUAL_AMOUNT);
				do {
					BusCard card = new BusCard();
					card.setCardNumber(data.getString(idxCardNumber));
					String resiaualCount = getString(R.string.residual_count, data.getString(idxResidualCount));
	                String resiaualAmount = getString(R.string.residual_amount, data.getString(idxResidualAmount));
					card.setResidualCount(resiaualCount);
					card.setResidualAmount(resiaualAmount);
					mBusCards.add(card);
				} while (data.moveToNext());
				
				if (mViewPagerAdapter == null) {
					mViewPagerAdapter = new BusCardPageAdapter();
					mViewPager.setAdapter(mViewPagerAdapter);
				}
			}
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			if (mBusCardLoaderId == loader.getId()) {
				mBusCards = null;
			}
		}
    }
    
    private void initView(){
        mSendButton = (Button)findViewById(R.id.search);
        mEditText = (AutoCompleteTextView)findViewById(R.id.key_card_id);
        mNoItemTips = (LinearLayout)findViewById(R.id.no_item_tips);
        mConsumptionDetail = (RelativeLayout)findViewById(R.id.consumption_detail);
        mViewPager = (ViewPager)findViewById(R.id.viewpager);
        mNoItemTips.setVisibility(View.GONE);
        mConsumptionDetail.setVisibility(View.VISIBLE);
    }

    public void onSearchCardIdClicked(View v){
        if(mSendButton.equals(v)) {
            String cardNumber = mEditText.getText().toString();
            if (Utils.isValidBusCardNumber(cardNumber)) {
            	mTrafficService.getConsumerRecords(cardNumber, 30);
            } else {
                mEditText.setError(getResources().getString(R.string.card_id_error_notice));
            }
        }
    }

    private static int getLoaderId() {
    	return LOADER_ID++;
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
			super(getContext(), c, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
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
    
    private class BusCardPageView {
    	private TextView mResidualCount;
        private TextView mResidualAmount;
        private DropRefreshListView mRecordList;
        private ConsumerRecordAdapter mAdapter;
    	private View mRootView;
    	private BusCard mCard;
    	private final int mLoaderId;
    	
    	public BusCardPageView(BusCard card) {
    		mCard = card;
    		mRootView = getLayoutInflater().inflate(R.layout.bus_card_consume_records, null);
    		mResidualCount = (TextView) mRootView.findViewById(R.id.residual_count_text);
    		mResidualAmount = (TextView) mRootView.findViewById(R.id.residual_amount_text);
    		mRecordList = (DropRefreshListView) mRootView.findViewById(R.id.consumer_record_list);
    		mResidualCount.setText(mCard.getResidualCount());
            mResidualAmount.setText(mCard.getResidualAmount());
            mLoaderId = getLoaderId();
            startLoadRecords();
		}
    	
    	public View getView() {
    		return mRootView;
    	}
    	
    	private void startLoadRecords() {
    		getLoaderManager().initLoader(mLoaderId, null, new LoaderCallback());
    	}
    	
    	private class LoaderCallback implements LoaderCallbacks<Cursor> {
			@Override
			public Loader<Cursor> onCreateLoader(int id, Bundle args) {
				CursorLoader loader = null;
				if (mLoaderId == id) {
					String cardNumber = mCard.getCardNumber();
					String selection = ITrafficData.BusCard.CARD_NUMBER + "=" + "\'" + cardNumber + "\'";
					String sortOrder = ITrafficData.ConsumerRecord.DATE + " DESC";
					loader = new CursorLoader(getContext(), ITrafficData.ConsumerRecord.CONTENT_URI, null, selection, null, sortOrder);
				}
				return loader;
			}

			@Override
			public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
				if (mLoaderId == loader.getId()) {
					if (mAdapter == null) {
						mAdapter = new ConsumerRecordAdapter(data);
						mRecordList.setAdapter(mAdapter);
					}
					mAdapter.swapCursor(data);
				}
			}

			@Override
			public void onLoaderReset(Loader<Cursor> loader) {
				if (mLoaderId == loader.getId()) {
					mAdapter.swapCursor(null);
				}
			}
			
    	}
    }
    
    private class BusCardPageAdapter extends PagerAdapter {
    	private ArrayList<BusCardPageView> mPageViews = new ArrayList<BusCardPageView>();
    	
        @Override
        public int getCount() {
            return mBusCards.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager)container).removeView(mPageViews.get(position).getView());
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
        	//BusCardPageView pageView = mPageViews.get(position);
            //if (pageView == null) {
        	BusCardPageView pageView = new BusCardPageView(mBusCards.get(position));
                mPageViews.add(pageView);
            //}
            ((ViewPager)container).addView(pageView.getView());
            return pageView.getView();
        }
    }

}
