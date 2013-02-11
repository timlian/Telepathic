
package com.telepathic.finder.app;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.CursorAdapter;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.telepathic.finder.R;
import com.telepathic.finder.app.MessageDispatcher.IMessageHandler;
import com.telepathic.finder.sdk.ITrafficService;
import com.telepathic.finder.sdk.ITrafficeMessage;
import com.telepathic.finder.sdk.traffic.entity.BusCard;
import com.telepathic.finder.sdk.traffic.entity.BusStationLines;
import com.telepathic.finder.sdk.traffic.entity.ConsumerRecord.ConsumerType;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData;
import com.telepathic.finder.util.Utils;
import com.telepathic.finder.view.DropRefreshListView;

public class BusCardRecordActivity2 extends BaseActivity {
    private static int LOADER_ID = 1000;

    private static final int TAB_COUNT = 3;

    private Button mSendButton;

    private AutoCompleteTextView mEditText;

    private ViewPager mViewPager;

    private HorizontalScrollView mViewPagerTab;

    private BusCardPageAdapter mViewPagerAdapter;

    private LinearLayout mNoItemTips;

    private LinearLayout mConsumptionDetail;

    private final int mBusCardLoaderId = getLoaderId();

    private ArrayList<BusCard> mBusCards;

    private ITrafficService mTrafficService;

    private LinearLayout mTabContent;

    private ArrayList<View> mViewPagerTabView = new ArrayList<View>();

    private int mScreenWidth;

    private volatile boolean isClicked;
    
    private ProgressDialog mWaitingDialog;
    private MessageDispatcher mMessageDispatcher;

    private IMessageHandler mMessageHandler = new IMessageHandler() {
		@Override
		public int what() {
			return ITrafficeMessage.GET_BUS_CARD_RECORDS_DONE;
		}
		
		@Override
		public void handleMessage(Message msg) {
			mWaitingDialog.cancel();
			mSendButton.setEnabled(true);
		}
	};
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_card_record);
        initView();
        FinderApplication app = (FinderApplication)getApplication();
        mTrafficService = app.getTrafficService();
        mMessageDispatcher = app.getMessageDispatcher();
        mMessageDispatcher.add(mMessageHandler);
        startLoadBusCards();
    }

    private void startLoadBusCards() {
        getSupportLoaderManager().initLoader(mBusCardLoaderId, null, new BusCardLoaderCallback());
    }

    private class BusCardLoaderCallback implements LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            CursorLoader loader = null;
            if (mBusCardLoaderId == id) {
                loader = new CursorLoader(getContext(), ITrafficData.BusCard.CONTENT_URI, null,
                        null, null, null);
            }
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (mBusCardLoaderId == loader.getId() && Utils.isValid(data)) {
                mNoItemTips.setVisibility(View.GONE);
                mConsumptionDetail.setVisibility(View.VISIBLE);
                mBusCards = new ArrayList<BusCard>();
                int idxCardNumber = data.getColumnIndex(ITrafficData.BusCard.CARD_NUMBER);
                int idxResidualCount = data.getColumnIndex(ITrafficData.BusCard.RESIDUAL_COUNT);
                int idxResidualAmount = data.getColumnIndex(ITrafficData.BusCard.RESIDUAL_AMOUNT);
                do {
                    BusCard card = new BusCard();
                    card.setCardNumber(data.getString(idxCardNumber));
                    String resiaualCount = data.getString(idxResidualCount);
                    String resiaualAmount = data.getString(idxResidualAmount);
                    card.setResidualCount(resiaualCount);
                    card.setResidualAmount(resiaualAmount);
                    mBusCards.add(card);
                } while (data.moveToNext());

                if (mViewPagerAdapter == null) {
                    mViewPagerAdapter = new BusCardPageAdapter();
                    mViewPager.setAdapter(mViewPagerAdapter);
                }
                initTab(mBusCards);
            } else {
                mNoItemTips.setVisibility(View.VISIBLE);
                mConsumptionDetail.setVisibility(View.GONE);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            if (mBusCardLoaderId == loader.getId()) {
                mBusCards = null;
            }
        }
    }

    private void initTab(ArrayList<BusCard> busCards) {
        final DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        final int screenWidth = dm.widthPixels;
        mViewPagerTabView.clear();
        mTabContent.removeAllViews();
        for (int i = 0; i < busCards.size(); i++) {
            View tabView = getLayoutInflater().inflate(R.layout.card_id_item, null);
            tabView.setId(i);
            tabView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    isClicked = true;
                    int id = v.getId();
                    for (int i = 0; i < mViewPagerTabView.size(); i++) {
                        if (id == i) {
                            mViewPager.setCurrentItem(i, true);
                        }
                    }
                }
            });
            TextView tv = (TextView)tabView.findViewById(R.id.card_id);
            tv.setLayoutParams(new LinearLayout.LayoutParams(screenWidth / TAB_COUNT,
                    LayoutParams.WRAP_CONTENT));
            tv.setText(busCards.get(i).getCardNumber());
            mViewPagerTabView.add(tabView);
            mTabContent.addView(tabView);
        }
        if (mViewPagerTabView.size() > 0) {
            mViewPagerTabView.get(mViewPager.getCurrentItem()).setBackgroundResource(
                    R.drawable.tab_selected);
        }
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

    private void initView() {
        final DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mSendButton = (Button)findViewById(R.id.search);
        mEditText = (AutoCompleteTextView)findViewById(R.id.key_card_id);
        mNoItemTips = (LinearLayout)findViewById(R.id.no_item_tips);
        mConsumptionDetail = (LinearLayout)findViewById(R.id.consumption_detail);
        mViewPager = (ViewPager)findViewById(R.id.viewpager);
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (!isClicked) {
                    final float curLeftDistance = mScreenWidth * position / TAB_COUNT;
                    mViewPagerTab.smoothScrollTo((int)curLeftDistance, 0);
                } else {
                    isClicked = false;
                }
                for (int i = 0; i < mViewPagerTabView.size(); i++) {
                    if (i != position) {
                        mViewPagerTabView.get(i).setBackgroundColor(Color.TRANSPARENT);
                    } else {
                        mViewPagerTabView.get(i).setBackgroundResource(R.drawable.tab_selected);
                    }
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // TODO Auto-generated method stub

            }
        });
        mViewPagerTab = (HorizontalScrollView)findViewById(R.id.viewpager_tab);
        mTabContent = (LinearLayout)findViewById(R.id.tabcontent);
        mWaitingDialog = createWaitingDialog();
    }

    public void onSearchCardIdClicked(View v) {
        if (!mSendButton.equals(v)) {
           return ;
        }
        String cardNumber = mEditText.getText().toString();
        if (Utils.isValidBusCardNumber(cardNumber)) {
        	 mSendButton.setEnabled(false);
             mWaitingDialog.show();
             Utils.hideSoftKeyboard(getApplicationContext(), mEditText);
            mTrafficService.getBusCardRecords(cardNumber, 30);
        } else {
        	mSendButton.setEnabled(true);
            mEditText.setError(getResources().getString(R.string.card_id_error_notice));
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
            holder.lineNumber = (TextView)convertView.findViewById(R.id.line_number);
            holder.busNumber = (TextView)convertView.findViewById(R.id.bus_number);
            holder.consumption = (TextView)convertView.findViewById(R.id.consumption);
            holder.consumerTime = (TextView)convertView.findViewById(R.id.consumer_time);
            holder.consumeType = (ImageView)convertView.findViewById(R.id.img_consumption_type);
            convertView.setTag(holder);
            return convertView;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            if (cursor != null) {
                RecordItemHolder holder = (RecordItemHolder)view.getTag();
                final int idxLineNumber = cursor
                        .getColumnIndex(ITrafficData.ConsumerRecord.LINE_NUMBER);
                final int idxBusNumber = cursor
                        .getColumnIndex(ITrafficData.ConsumerRecord.BUS_NUMBER);
                final int idxConsumption = cursor
                        .getColumnIndex(ITrafficData.ConsumerRecord.CONSUMPTION);
                final int idxDate = cursor.getColumnIndex(ITrafficData.ConsumerRecord.DATE);
                final int idxType = cursor.getColumnIndex(ITrafficData.ConsumerRecord.TYPE);
                holder.lineNumber.setText(getResources().getString(R.string.line_number)
                        + cursor.getString(idxLineNumber));
                holder.busNumber.setText(getResources().getString(R.string.bus_number)
                        + cursor.getString(idxBusNumber));
                String comsumption = "";
                ConsumerType type = ConsumerType.valueOf(cursor.getString(idxType));
                if (type == ConsumerType.COUNT) {
                    comsumption = getString(R.string.consumer_count,
                            cursor.getString(idxConsumption));
                    holder.consumeType.setImageResource(R.drawable.count);
                } else if (type == ConsumerType.EWALLET) {
                    comsumption = getString(R.string.consumer_amount,
                            cursor.getString(idxConsumption));
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
            mResidualCount = (TextView)mRootView.findViewById(R.id.residual_count_text);
            mResidualAmount = (TextView)mRootView.findViewById(R.id.residual_amount_text);
            mRecordList = (DropRefreshListView)mRootView.findViewById(R.id.consumer_record_list);
            mResidualCount.setText(getString(R.string.residual_count, mCard.getResidualCount()));
            mResidualAmount.setText(getString(R.string.residual_amount, mCard.getResidualAmount()));
            mLoaderId = getLoaderId();
            startLoadRecords();
        }

        public View getView() {
            return mRootView;
        }

        private void startLoadRecords() {
            getSupportLoaderManager().initLoader(mLoaderId, null, new LoaderCallback());
        }

        private class LoaderCallback implements LoaderCallbacks<Cursor> {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                CursorLoader loader = null;
                if (mLoaderId == id) {
                    String cardNumber = mCard.getCardNumber();
                    String selection = ITrafficData.BusCard.CARD_NUMBER + "=" + "\'" + cardNumber
                            + "\'";
                    String sortOrder = ITrafficData.ConsumerRecord.DATE + " DESC";
                    loader = new CursorLoader(getContext(),
                            ITrafficData.ConsumerRecord.CONTENT_URI, null, selection, null,
                            sortOrder);
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
            BusCardPageView pageView = null;
            if (mPageViews.size() > position) {
                pageView = mPageViews.get(position);
            }
            if (pageView == null) {
                pageView = new BusCardPageView(mBusCards.get(position));
                mPageViews.add(pageView);
            }
            ((ViewPager)container).addView(pageView.getView());
            return pageView.getView();
        }
    }

}
