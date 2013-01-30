package com.telepathic.finder.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.telepathic.finder.R;
import com.telepathic.finder.sdk.traffic.BusCard;
import com.telepathic.finder.util.Utils;
import com.telepathic.finder.view.DropRefreshListView;

public class BusCardPageAdapter extends PagerAdapter {
	private static final String TAG  = "BusCardPageAdapter";
    private Context mContext;
    private ArrayList<BusCardPageInfo> mBusCardPageInfoList;

    public BusCardPageAdapter(Context ctx, ArrayList<BusCard> BusCardList) {
        mContext = ctx;
        mBusCardPageInfoList = new ArrayList<BusCardPageAdapter.BusCardPageInfo>();
        for (BusCard busCard : BusCardList) {
            BusCardPageInfo pageInfo = new BusCardPageInfo();
            pageInfo.busCard = busCard;
            mBusCardPageInfoList.add(pageInfo);
        }
    }

    @Override
    public int getCount() {
        return mBusCardPageInfoList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    /* (non-Javadoc)
     * @see android.support.v4.view.PagerAdapter#destroyItem(android.view.ViewGroup, int, java.lang.Object)
     */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager)container).removeView(mBusCardPageInfoList.get(position).view);
    }

    /* (non-Javadoc)
     * @see android.support.v4.view.PagerAdapter#instantiateItem(android.view.ViewGroup, int)
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
    	Utils.debug(TAG, "instantiateItem - : " + position);
        View containView = mBusCardPageInfoList.get(position).view;
        if (containView == null) {
            ViewHolder holder = new ViewHolder();
            containView = ((Activity)mContext).getLayoutInflater().inflate(R.layout.bus_card_consume_records, null);
            holder.tvResidualCount = (TextView)containView.findViewById(R.id.residual_count_text);
            holder.tvResidualAmount = (TextView)containView.findViewById(R.id.residual_amount_text);
            holder.lvRecordsList = (DropRefreshListView)containView.findViewById(R.id.consumer_record_list);
            containView.setTag(holder);
        }
        bindView(containView, position);
        ((ViewPager)container).addView(containView);
        mBusCardPageInfoList.get(position).view = containView;
        return mBusCardPageInfoList.get(position).view;
    }

    private void bindView(View view, int position){
        ViewHolder holder = (ViewHolder)view.getTag();
        String resiaualCount = mContext.getString(R.string.residual_count,mBusCardPageInfoList.get(position).busCard.getResidualCount());
        String resiaualAmount = mContext.getString(R.string.residual_amount,mBusCardPageInfoList.get(position).busCard.getResidualAmount());
        holder.tvResidualCount.setText(resiaualCount);
        holder.tvResidualAmount.setText(resiaualAmount);
        holder.lvRecordsList.setAdapter(new ConsumerRecordsAdapter(mContext, mBusCardPageInfoList.get(position).busCard.getConsumerRecords()));
    }

    public void addItem(BusCardPageInfo info){
        mBusCardPageInfoList.add(info);
    }


    public static class BusCardPageInfo {
        View view;
        BusCard busCard;
    }

    private static class ViewHolder {
        TextView tvResidualCount;
        TextView tvResidualAmount;
        DropRefreshListView lvRecordsList;
    }

}
