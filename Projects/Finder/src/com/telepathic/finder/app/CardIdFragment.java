package com.telepathic.finder.app;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.telepathic.finder.R;
import com.telepathic.finder.util.Utils;


public class CardIdFragment extends android.support.v4.app.Fragment {
    private Activity mHostActivity;
    private ListView mCardListView;

    private OnCardSelectedListener mListener;

    // Container Activity must implement this interface
    public interface OnCardSelectedListener {
        public void onCardSelected(String cardId);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHostActivity = getActivity();
    }

    public void setOnCardSelectedListener(OnCardSelectedListener listener) {
        mListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.card_list, container, false);
        mCardListView = (ListView) view.findViewById(R.id.card_list);
        mCardListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                arg1.setSelected(true);
                mListener.onCardSelected(mCardListView.getItemAtPosition(arg2).toString());
            }
        });
        mCardListView.setAdapter(new MyAdapter());
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //        try {
        //            mListener = (OnCardSelectedListener) activity;
        //        } catch (ClassCastException e) {
        //            throw new ClassCastException(activity.toString() + " must implement OnCardSelectedListener");
        //        }
    }

    private static class CardItemHolder {
        TextView cardIdText;
    }

    private class MyAdapter extends BaseAdapter {

        ArrayList<String> mCardList;

        MyAdapter() {
            ArrayList<String> list = Utils.getCachedCards(getActivity());
            if (list.size() <= 0) {
                list.add("no item");
            }
            mCardList = list;
        }

        @Override
        public int getCount() {
            return mCardList.size();
        }

        @Override
        public Object getItem(int position) {
            return mCardList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String cardId = mCardList.get(position);
            if (convertView == null) {
                convertView = mHostActivity.getLayoutInflater().inflate(R.layout.card_item, parent, false);
                CardItemHolder holder = new CardItemHolder();
                holder.cardIdText = (TextView) convertView.findViewById(R.id.card);
                convertView.setTag(holder);
            }
            CardItemHolder holder = (CardItemHolder) convertView.getTag();
            holder.cardIdText.setText(cardId);
            return convertView;
        }
    }

}
