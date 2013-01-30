package com.telepathic.finder.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.telepathic.finder.R;
import com.telepathic.finder.sdk.traffic.ConsumerRecord;
import com.telepathic.finder.sdk.traffic.ConsumerRecord.ConsumerType;

public class ConsumerRecordsAdapter extends BaseAdapter {

    private ArrayList<ConsumerRecord> mRecordsList;
    private Context mContext;

    public ConsumerRecordsAdapter(Context context, ArrayList<ConsumerRecord> list) {
        mContext = context;
        mRecordsList = list;
    }

    @Override
    public int getCount() {
        return mRecordsList.size();
    }

    @Override
    public Object getItem(int position) {
        return mRecordsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = ((Activity)mContext).getLayoutInflater();
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.consumer_record_item, parent, false);
            RecordItemHolder holder = new RecordItemHolder();
            holder.lineNumber = (TextView) convertView.findViewById(R.id.line_number);
            holder.busNumber = (TextView) convertView.findViewById(R.id.bus_number);
            holder.consumption = (TextView) convertView.findViewById(R.id.consumption);
            holder.consumerTime = (TextView) convertView.findViewById(R.id.consumer_time);
            holder.consumeType = (ImageView) convertView.findViewById(R.id.img_consumption_type);
            convertView.setTag(holder);
        }
        bindView(convertView, position);
        return convertView;
    }

    private void bindView(View view, int position) {
        RecordItemHolder holder = (RecordItemHolder)view.getTag();
        holder.lineNumber.setText(mContext.getString(R.string.line_number, mRecordsList.get(position).getLineNumber()));
        holder.busNumber.setText(mContext.getString(R.string.line_number, mRecordsList.get(position).getBusNumber()));
        String comsumption = "";
        ConsumerType type = mRecordsList.get(position).getType();
        switch(type) {
            case COUNT:
                comsumption = mContext.getString(R.string.consumer_count, mRecordsList.get(position).getConsumption());
                holder.consumeType.setImageResource(R.drawable.count);
                break;
            case EWALLET:
                comsumption = mContext.getString(R.string.consumer_amount, mRecordsList.get(position).getConsumption());
                holder.consumeType.setImageResource(R.drawable.ewallet);
                break;
        }
        holder.consumption.setText(comsumption);
        holder.consumerTime.setText(mRecordsList.get(position).getConsumerTime().toString());
    }

    private static class RecordItemHolder {
        TextView lineNumber;
        TextView busNumber;
        TextView consumption;
        TextView consumerTime;
        ImageView consumeType;
    }

}
