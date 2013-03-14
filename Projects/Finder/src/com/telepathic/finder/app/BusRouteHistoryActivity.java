package com.telepathic.finder.app;

import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.telepathic.finder.R;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData;
import com.telepathic.finder.util.Utils;

public class BusRouteHistoryActivity extends BaseActivity {

    private static final int HISTORY_LOADER_ID = 2000;

    private static final String[] ROUTE_HISTORY_PROJECTION = {
        ITrafficData.BaiDuData.BusRoute._ID,
       // ITrafficData.BaiDuData.BusRoute.NAME,
       // ITrafficData.BaiDuData.BusRoute.LAST_UPDATE_TIME
    };
    private static final int IDX_LINE_NAME = 1;
    private static final int IDX_TIMESTAMP = 2;

    private ListView mRouteHistory;
    private BusRouteHistoryAdapter mHistroyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bus_route_history);
        mRouteHistory = (ListView) findViewById(R.id.route_history);
        getSupportLoaderManager().initLoader(HISTORY_LOADER_ID, null, new BusRouteHistoryLoaderCallback());
    }

    private static class HistoryItemHolder {
        TextView lineName;
        TextView timestamp;
    }

    private class BusRouteHistoryAdapter extends CursorAdapter {
        private LayoutInflater mInflater;

        public BusRouteHistoryAdapter(Cursor c) {
            super(getContext(), c, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
            mInflater = getLayoutInflater();
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View convertView = mInflater.inflate(R.layout.route_history_item, parent, false);
            HistoryItemHolder holder = new HistoryItemHolder();
            holder.lineName  = (TextView)convertView.findViewById(R.id.line_name);
            holder.timestamp = (TextView)convertView.findViewById(R.id.timestamp);
            convertView.setTag(holder);
            return convertView;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            if (cursor != null) {
                HistoryItemHolder holder = (HistoryItemHolder)view.getTag();
                holder.lineName.setText(cursor.getString(IDX_LINE_NAME));
                holder.timestamp.setText(Utils.formatDate(new Date(cursor.getLong(IDX_TIMESTAMP))));
            }
        }
    }

    private class BusRouteHistoryLoaderCallback implements LoaderCallbacks<Cursor> {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            CursorLoader loader = null;
            if (HISTORY_LOADER_ID == id) {
                loader = new CursorLoader(getContext(),
                        ITrafficData.BaiDuData.BusRoute.CONTENT_URI,
                        ROUTE_HISTORY_PROJECTION, null, null, null);
            }
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (HISTORY_LOADER_ID == loader.getId()) {
                if (mHistroyAdapter == null) {
                    mHistroyAdapter = new BusRouteHistoryAdapter(data);
                    mRouteHistory.setAdapter(mHistroyAdapter);
                }
                mHistroyAdapter.swapCursor(data);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            if (HISTORY_LOADER_ID == loader.getId()) {
                mHistroyAdapter.swapCursor(null);
            }
        }
    }
}
