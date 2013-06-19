
package com.telepathic.finder.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.actionbarsherlock.widget.SearchView.OnSuggestionListener;
import com.telepathic.finder.R;
import com.telepathic.finder.app.MessageDispatcher.IMessageHandler;
import com.telepathic.finder.sdk.ICompletionListener;
import com.telepathic.finder.sdk.ITrafficService;
import com.telepathic.finder.sdk.ITrafficeMessage;
import com.telepathic.finder.sdk.traffic.entity.BusCard;
import com.telepathic.finder.sdk.traffic.entity.ConsumerRecord.ConsumerType;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData;
import com.telepathic.finder.util.UmengEvent;
import com.telepathic.finder.util.Utils;
import com.telepathic.finder.view.DropRefreshListView;
import com.telepathic.finder.view.DropRefreshListView.OnRefreshListener;
import com.umeng.analytics.MobclickAgent;

public class BusCardRecordFragment extends SherlockFragment {

    private static final String TAG = BusCardRecordFragment.class.getSimpleName();

    private MainActivity mActivity;

    private static int LOADER_ID = 1000;

    private static final int TAB_COUNT = 3;

    private SearchView mSearchView;

    private ViewPager mViewPager;

    private HorizontalScrollView mViewPagerTab;

    private BusCardPageAdapter mViewPagerAdapter;

    private LinearLayout mNoItemTips;

    private RelativeLayout mConsumptionDetail;

    private final int mBusCardLoaderId = getLoaderId();

    private ArrayList<BusCard> mBusCards;

    private ITrafficService mTrafficService;

    private LinearLayout mTabContent;

    private ArrayList<View> mViewPagerTabView = new ArrayList<View>();

    private ArrayList<BusCardPageView> mPageViews = new ArrayList<BusCardPageView>();

    private int mScreenWidth;

    private volatile boolean isClicked;

    private ProgressDialog mWaitingDialog;

    private MessageDispatcher mMessageDispatcher;

    private boolean isDeleteMode = false;

    private ActionMode mDeleteMode;

    private ArrayList<String> mDeleteCardsList = new ArrayList<String>();

    private static final String[] CARD_RECORDS_HISTORY = {
        ITrafficData.KuaiXinData.BusCard._ID, ITrafficData.KuaiXinData.BusCard.CARD_NUMBER
    };

    private IMessageHandler mMessageHandler = new IMessageHandler() {
        @Override
        public int what() {
            return ITrafficeMessage.GET_BUS_CARD_RECORDS_DONE;
        }

        @Override
        public void handleMessage(Message msg) {
            mWaitingDialog.cancel();
            for (BusCardPageView page : mPageViews) {
                page.mRecordList.onRefreshComplete();
            }
            mConsumptionDetail.requestFocusFromTouch();
            switch (msg.arg2) {
                case ITrafficeMessage.GET_BUS_CARD_RECORDS_FAILED:
                    Toast.makeText(mActivity, (CharSequence)msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bus_card_record, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (MainActivity)getSherlockActivity();

        initView();
        FinderApplication app = (FinderApplication)mActivity.getApplication();
        mTrafficService = app.getTrafficService();
        mMessageDispatcher = app.getMessageDispatcher();
    }

    @Override
    public void onStart() {
        mMessageDispatcher.add(mMessageHandler);
        startLoadBusCards();
        super.onStart();
    }

    private void startLoadBusCards() {
        mActivity.getSupportLoaderManager().initLoader(mBusCardLoaderId, null,
                new BusCardLoaderCallback());
    }

    private class BusCardLoaderCallback implements LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            CursorLoader loader = null;
            if (mBusCardLoaderId == id) {
                loader = new CursorLoader(mActivity,
                        ITrafficData.KuaiXinData.BusCard.CONTENT_URI, null, null, null, null);
            }
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (mBusCardLoaderId == loader.getId() && data != null && data.moveToFirst()) {
                mNoItemTips.setVisibility(View.GONE);
                mConsumptionDetail.setVisibility(View.VISIBLE);
                mBusCards = new ArrayList<BusCard>();
                int idxCardNumber = data
                        .getColumnIndex(ITrafficData.KuaiXinData.BusCard.CARD_NUMBER);
                int idxResidualCount = data
                        .getColumnIndex(ITrafficData.KuaiXinData.BusCard.RESIDUAL_COUNT);
                int idxResidualAmount = data
                        .getColumnIndex(ITrafficData.KuaiXinData.BusCard.RESIDUAL_AMOUNT);
                do {
                    BusCard card = new BusCard();
                    card.setCardNumber(data.getString(idxCardNumber));
                    String resiaualCount = data.getString(idxResidualCount);
                    String resiaualAmount = data.getString(idxResidualAmount);
                    card.setResidualCount(resiaualCount);
                    card.setResidualAmount(resiaualAmount);
                    mBusCards.add(card);
                } while (data.moveToNext());

                Comparator cp = new Comparator() {
                    @Override
                    public int compare(Object lhs, Object rhs) {
                        BusCard left = (BusCard)lhs;
                        BusCard right = (BusCard)rhs;
                        return left.getCardNumber().compareTo(right.getCardNumber());
                    }
                };
                Collections.sort(mBusCards, cp);
                mViewPagerAdapter = new BusCardPageAdapter();
                mViewPager.setAdapter(mViewPagerAdapter);
                initTab(mBusCards);
            } else {
                if (isDeleteMode) {
                    isDeleteMode = false;
                }
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

    private void deleteModeSelected(final TextView tv, final String cardNumber){
        mDeleteCardsList.add(cardNumber);
        tv.setBackgroundResource(R.drawable.card_number_focused_holo_light);
        tv.setPadding(0, Utils.dip2px(mActivity, 10), 0, Utils.dip2px(mActivity, 10));
        if (!mDeleteCardsList.isEmpty()) {
            mDeleteMode.setTitle(String.valueOf(mDeleteCardsList.size()));
        }
    }

    private void deleteModeUnselected(final TextView tv, final String cardNumber){
        mDeleteCardsList.remove(cardNumber);
        tv.setBackgroundColor(Color.TRANSPARENT);
        if (!mDeleteCardsList.isEmpty()) {
            mDeleteMode.setTitle(String.valueOf(mDeleteCardsList.size()));
        } else {
            mDeleteMode.finish();
        }
    }

    private void initTab(ArrayList<BusCard> busCards) {
        final DisplayMetrics dm = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        final int screenWidth = dm.widthPixels;
        mViewPagerTabView.clear();
        mTabContent.removeAllViews();
        for (int i = 0; i < busCards.size(); i++) {
            final String card_number = busCards.get(i).getCardNumber();
            View tabView = mActivity.getLayoutInflater().inflate(R.layout.card_id_item, null);
            final TextView tv = (TextView)tabView.findViewById(R.id.card_id);
            tabView.setId(i);
            tabView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isDeleteMode) {
                        isClicked = true;
                        int id = v.getId();
                        if (mViewPagerTabView.size() > id) {
                            mViewPager.setCurrentItem(id, true);
                        }
                    } else {
                        if (mDeleteCardsList.contains(card_number)) {
                            deleteModeUnselected(tv, card_number);
                        }else{
                            deleteModeSelected(tv, card_number);
                        }
                    }
                }
            });
            tabView.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    if (mBusCards != null && mBusCards.size() > 0 && !isDeleteMode) {
                        mDeleteMode = mActivity.startActionMode(new DeleteActionModeCallback());
                        deleteModeSelected(tv, card_number);
                    } else {
                        if (mDeleteCardsList.contains(card_number)) {
                            deleteModeUnselected(tv, card_number);
                        }else{
                            deleteModeSelected(tv, card_number);
                        }
                    }
                    return true;
                }
            });
            tv.setLayoutParams(new LinearLayout.LayoutParams(screenWidth / TAB_COUNT,
                    LayoutParams.WRAP_CONTENT));
            tv.setText(card_number);
            if (isDeleteMode && i == 0) {
                mDeleteCardsList.clear();
                deleteModeSelected(tv, card_number);
            }
            //            ImageView iv = (ImageView)tabView.findViewById(R.id.delete_card);
            //            iv.setOnClickListener(new OnClickListener() {
            //                @Override
            //                public void onClick(View v) {
            //                    deleteBusCardRecordsByNumber(card_number);
            //                }
            //            });
            //            if (isDeleteMode) {
            //                iv.setVisibility(View.VISIBLE);
            //            } else {
            //                iv.setVisibility(View.GONE);
            //            }
            mViewPagerTabView.add(tabView);
            mTabContent.addView(tabView);
        }
        if (mViewPagerTabView.size() > 0) {
            mViewPagerTabView.get(mViewPager.getCurrentItem()).setBackgroundResource(
                    R.drawable.tab_selected);
        }
        final float curLeftDistance = mScreenWidth * 0 / TAB_COUNT;
        mViewPagerTab.smoothScrollTo((int)curLeftDistance, 0);
        mViewPager.setCurrentItem(0);
    }

    private ProgressDialog createWaitingDialog() {
        ProgressDialog prgDlg = new ProgressDialog(mActivity);
        prgDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        prgDlg.setMessage(getResources().getString(R.string.find_ic_card_records));
        prgDlg.setIndeterminate(true);
        prgDlg.setOnCancelListener(null);
        return prgDlg;
    }

    private void initView() {
        final DisplayMetrics metrics = new DisplayMetrics();
        mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenWidth = metrics.widthPixels;
        mNoItemTips = (LinearLayout)getView().findViewById(R.id.no_item_tips);
        mConsumptionDetail = (RelativeLayout)getView().findViewById(R.id.consumption_detail);
        mViewPager = (ViewPager)getView().findViewById(R.id.viewpager);
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
        mViewPagerTab = (HorizontalScrollView)getView().findViewById(R.id.viewpager_tab);
        mTabContent = (LinearLayout)getView().findViewById(R.id.tabcontent);
        mWaitingDialog = createWaitingDialog();
    }

    @Override
    public void onStop() {
        mMessageDispatcher.remove(mMessageHandler);
        mViewPager.removeAllViews();
        mViewPagerAdapter = null;
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_card:
                //                if (mBusCards != null && mBusCards.size() > 0) {
                //                    isDeleteMode = true;
                //                    initTab(mBusCards);
                //                    mBtnDeleteComplete.setVisibility(View.VISIBLE);
                //                }
                if (mBusCards != null && mBusCards.size() > 0) {
                    mDeleteMode = mActivity.startActionMode(new DeleteActionModeCallback());
                    initTab(mBusCards);
                }
                return true;
            case R.id.clear_cache:
                Builder builder = new AlertDialog.Builder(mActivity);
                builder.setTitle(R.string.confirm_clean_cache_title)
                .setMessage(R.string.confirm_clean_bus_card_cache)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Utils.copyAppDatabaseFiles(mActivity.getPackageName());
                        deleteAllBusCardRecords();
                        getSuggestions(""); // reset the
                        // suggestions
                    }
                }).setNegativeButton(R.string.cancel, null);
                builder.create().show();
                return true;
            case R.id.about:
                startActivity(new Intent(mActivity, AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        // Inflate the options menu from XML
        inflater.inflate(R.menu.menu_card_record, menu);

        // Get the SearchView and set the searchable configuration
        mSearchView = (SearchView)menu.findItem(R.id.search_card_record).getActionView();
        SearchManager manager = (SearchManager)this.getSherlockActivity().getSystemService(
                Context.SEARCH_SERVICE);
        SearchableInfo info = manager.getSearchableInfo(this.getSherlockActivity()
                .getComponentName());
        mSearchView.setSearchableInfo(info);
        mSearchView.setQueryHint(getResources().getText(R.string.ic_card_hint));
        mSearchView.setInputType(InputType.TYPE_CLASS_NUMBER);
        mSearchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mSearchView.setOnQueryTextListener(new OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                final String cardNumber = query;
                if (Utils.isValidBusCardNumber(cardNumber)) {
                    mWaitingDialog.show();
                    Utils.hideSoftKeyboard(mActivity.getApplicationContext(), mSearchView);
                    MobclickAgent.onEvent(mActivity, UmengEvent.CARD_NUMBER, cardNumber);
                    mTrafficService.getBusCardRecords(cardNumber, 30, new ICompletionListener() {

                        @Override
                        public void onSuccess(Object result) {
                            // TODO Auto-generated method stub

                        }

                        @Override
                        public void onFailure(int errorCode, String errorText) {
                            // TODO Auto-generated method stub

                        }
                    });
                } else {
                    Toast.makeText(mActivity, R.string.invalid_card_number, Toast.LENGTH_SHORT)
                    .show();
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (mActivity == null) {
                    return false;
                }
                getSuggestions(newText);
                return true;
            }
        });
        mSearchView.setOnSuggestionListener(new OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = (Cursor)mSearchView.getSuggestionsAdapter().getItem(position);
                int suggestionIndex = cursor
                        .getColumnIndex(ITrafficData.KuaiXinData.BusCard.CARD_NUMBER);

                mSearchView.setQuery(cursor.getString(suggestionIndex), true);
                return true;
            }
        });
        EditText searchEditText = (EditText)mSearchView.findViewById(R.id.abs__search_src_text);
        if (searchEditText != null) {
            searchEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    String queryText = mSearchView.getQuery().toString();
                    if (hasFocus) {
                        getSuggestions(queryText);
                        mSearchView.setQuery(queryText, false);
                    }
                }
            });
        } else {
            mSearchView.setOnQueryTextFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    String queryText = mSearchView.getQuery().toString();
                    if (hasFocus) {
                        getSuggestions(queryText);
                        mSearchView.setQuery(queryText, false);
                    }
                }
            });
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void getSuggestions(String queryText) {
        Cursor cursor = queryBusCardID(queryText);
        String[] from = new String[] {
                ITrafficData.KuaiXinData.BusCard.CARD_NUMBER
        };
        int[] to = new int[] {
                android.R.id.text1
        };
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(mActivity,
                android.R.layout.simple_list_item_1, cursor, from, to, 0);
        mSearchView.setSuggestionsAdapter(adapter);
    }

    private static int getLoaderId() {
        return LOADER_ID++;
    }

    private Cursor queryBusCardID(String keywords) {
        ContentResolver resolver = mActivity.getContentResolver();
        String sortOrder = ITrafficData.KuaiXinData.BusCard.CARD_NUMBER + " ASC ";
        String selection = null;
        String[] selectionArgs = null;
        if (!TextUtils.isEmpty(keywords)) {
            selection = ITrafficData.KuaiXinData.BusCard.CARD_NUMBER + " LIKE ?";
            selectionArgs = new String[] {
                    keywords + "%"
            };
        }
        Cursor cursor = resolver.query(ITrafficData.KuaiXinData.BusCard.CONTENT_URI,
                CARD_RECORDS_HISTORY, selection, selectionArgs, sortOrder);
        return cursor;
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
            super(mActivity, c, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
            mInflater = mActivity.getLayoutInflater();
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
                        .getColumnIndex(ITrafficData.KuaiXinData.ConsumerRecord.LINE_NUMBER);
                final int idxBusNumber = cursor
                        .getColumnIndex(ITrafficData.KuaiXinData.ConsumerRecord.BUS_NUMBER);
                final int idxConsumption = cursor
                        .getColumnIndex(ITrafficData.KuaiXinData.ConsumerRecord.CONSUMPTION);
                final int idxDate = cursor
                        .getColumnIndex(ITrafficData.KuaiXinData.ConsumerRecord.DATE);
                final int idxType = cursor
                        .getColumnIndex(ITrafficData.KuaiXinData.ConsumerRecord.TYPE);
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
            mRootView = mActivity.getLayoutInflater().inflate(R.layout.bus_card_consume_records,
                    null);
            mResidualCount = (TextView)mRootView.findViewById(R.id.residual_count_text);
            mResidualAmount = (TextView)mRootView.findViewById(R.id.residual_amount_text);
            mRecordList = (DropRefreshListView)mRootView.findViewById(R.id.consumer_record_list);
            String count = TextUtils.isEmpty(mCard.getResidualCount())?"0":mCard.getResidualCount();
            String amount = TextUtils.isEmpty(mCard.getResidualAmount())?"0":mCard.getResidualAmount();
            mResidualCount.setText(getString(R.string.residual_count, count));
            mResidualAmount.setText(getString(R.string.residual_amount, amount));
            mLoaderId = getLoaderId();
            startLoadRecords();
        }

        public void refreshPageView(BusCard card) {
            mCard = card;
            String count = TextUtils.isEmpty(mCard.getResidualCount())?"0":mCard.getResidualCount();
            String amount = TextUtils.isEmpty(mCard.getResidualAmount())?"0":mCard.getResidualAmount();
            mResidualCount.setText(getString(R.string.residual_count, count));
            mResidualAmount.setText(getString(R.string.residual_amount, amount));
        }

        public String getCardNumber() {
            return mCard.getCardNumber();
        }

        public View getView() {
            return mRootView;
        }

        private void startLoadRecords() {
            mActivity.getSupportLoaderManager().initLoader(mLoaderId, null, new LoaderCallback());
        }

        private class LoaderCallback implements LoaderCallbacks<Cursor> {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                CursorLoader loader = null;
                if (mLoaderId == id) {
                    String cardNumber = mCard.getCardNumber();
                    String selection = ITrafficData.KuaiXinData.BusCard.CARD_NUMBER + "=" + "\'"
                            + cardNumber + "\'";
                    String sortOrder = ITrafficData.KuaiXinData.ConsumerRecord.DATE + " DESC";
                    loader = new CursorLoader(mActivity,
                            ITrafficData.KuaiXinData.ConsumerRecord.CONTENT_URI, null, selection,
                            null, sortOrder);
                }
                return loader;
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                if (mLoaderId == loader.getId()) {
                    if (mAdapter == null) {
                        mAdapter = new ConsumerRecordAdapter(data);
                        mRecordList.setAdapter(mAdapter);
                        mRecordList.setOnRefreshListener(new OnRefreshListener() {
                            @Override
                            public void onRefresh() {
                                String cardNumber = mCard.getCardNumber();
                                if (Utils.isValidBusCardNumber(cardNumber)) {
                                    mTrafficService.getBusCardRecords(cardNumber, 30, new ICompletionListener() {

                                        @Override
                                        public void onSuccess(Object result) {
                                            // TODO Auto-generated method stub

                                        }

                                        @Override
                                        public void onFailure(int errorCode, String errorText) {
                                            // TODO Auto-generated method stub

                                        }
                                    });
                                } else {
                                    Toast.makeText(mActivity, R.string.card_id_error_notice,
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
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
                for (BusCardPageView pView : mPageViews) {
                    if (pView.getCardNumber().equals(mBusCards.get(position).getCardNumber())) {
                        pageView = pView;
                        break;
                    }
                }
            }
            if (pageView == null) {
                pageView = new BusCardPageView(mBusCards.get(position));
                mPageViews.add(position, pageView);
            } else {
                pageView.refreshPageView(mBusCards.get(position));
            }
            ((ViewPager)container).addView(pageView.getView());
            return pageView.getView();
        }
    }

    private void deleteAllBusCardRecords() {
        MobclickAgent.onEvent(mActivity, UmengEvent.CARD_CLEAR);
        ContentResolver resolver = mActivity.getContentResolver();
        int rows = resolver.delete(ITrafficData.KuaiXinData.BusCard.CONTENT_URI, null, null);
        Utils.debug(TAG, "delete rows: " + rows);
    }

    private void deleteBusCardRecordsByNumber(String number) {
        MobclickAgent.onEvent(mActivity, UmengEvent.CARD_DEL_NUMBER, number);
        ContentResolver resolver = mActivity.getContentResolver();
        String where = ITrafficData.KuaiXinData.BusCard.CARD_NUMBER + "=?";
        String[] selectionArgs = new String[] {
                number
        };
        int row = resolver.delete(ITrafficData.KuaiXinData.BusCard.CONTENT_URI, where,
                selectionArgs);
        if (row != -1) {
            resolver.notifyChange(ITrafficData.KuaiXinData.BusCard.CONTENT_URI, null);
        }
    }

    private final class DeleteActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.action_mode_delete_provider, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            isDeleteMode = true;
            mDeleteCardsList.clear();
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch(item.getItemId()) {
                case R.id.delete:
                    mode.finish();
                    break;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            isDeleteMode = false;
            mDeleteCardsList.clear();
            initTab(mBusCards); // re-init tabs to make the tab display normally
            mDeleteMode = null;
        }
    }

}
