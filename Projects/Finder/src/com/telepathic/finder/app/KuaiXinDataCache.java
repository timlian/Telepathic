package com.telepathic.finder.app;

import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusLine;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusRoute;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusStationLines;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusLine.Direction;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData.KuaiXinData;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

public class KuaiXinDataCache {
    private static final String TAG = "KuaiXinDataCache";

    private static final int MAX_RECENT_STATIONS = 60;

    private static final String[] BUS_STATION_PROJECTION = {
        KuaiXinData.BusStation._ID,
        KuaiXinData.BusStation.GPS_NUMBER,
        KuaiXinData.BusStation.NAME
    };

    private static final String[] BUS_STATION_LINES_PROJECTION = {
        ITrafficData.KuaiXinData.BusStation.NAME,
        ITrafficData.KuaiXinData.BusStation.GPS_NUMBER,
        ITrafficData.KuaiXinData.BusRoute.LINE_NUMBER,
        ITrafficData.KuaiXinData.BusRoute.DIRECTION,
        ITrafficData.KuaiXinData.BusRoute.START_TIME,
        ITrafficData.KuaiXinData.BusRoute.END_TIME,
        ITrafficData.KuaiXinData.BusRoute.STATIONS
    };
    private static final int IDX_NAME = 0;
    private static final int IDX_GPS_NUMBER = 1;
    private static final int IDX_LINE_NUMBER = 2;
    private static final int IDX_DIRECTION = 3;
    private static final int IDX_START_TIME = 4;
    private static final int IDX_END_TIME = 5;
    private static final int IDX_STATIONS = 6;

    private Context mContext;
    private ContentResolver mContentResolver;

    KuaiXinDataCache(Context context) {
        mContext = context;
        mContentResolver = context.getContentResolver();
    }

    public KXBusStationLines getLastStationLines() {
        String lastGpsNumber = null;
        Cursor cursor = queryLastStation();
        if (cursor != null && cursor.moveToFirst()) {
            try {
                lastGpsNumber = cursor.getString(cursor.getColumnIndex(KuaiXinData.BusStation.GPS_NUMBER));
            } finally {
                cursor.close();
            }
        }
        KXBusStationLines stationLines = null;
        if (!TextUtils.isEmpty(lastGpsNumber)) {
            stationLines = getStationLines(lastGpsNumber);
        }
        return stationLines;
    }

    public KXBusStationLines getStationLines(String gpsNumber) {
        KXBusStationLines stationLines = null;
        Cursor cursor = queryBusStationLines(gpsNumber);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                if (gpsNumber.equals(cursor.getString(IDX_GPS_NUMBER))) {
                    if (stationLines == null) {
                        stationLines = new KXBusStationLines();
                        stationLines.setName(cursor.getString(IDX_NAME));
                        stationLines.setGpsNumber(cursor.getString(IDX_GPS_NUMBER));
                    }
                    String lineNumber = cursor.getString(IDX_LINE_NUMBER);
                    Direction direction = Direction.fromString(cursor.getString(IDX_DIRECTION));
                    KXBusLine busLine = new KXBusLine(lineNumber);
                    KXBusRoute busRoute = new KXBusRoute();
                    busRoute.setStartTime(cursor.getString(IDX_START_TIME));
                    busRoute.setEndTime(cursor.getString(IDX_END_TIME));
                    busRoute.setStations(cursor.getString(IDX_STATIONS).split(","));
                    busRoute.setDirection(direction);
                    busLine.addRoute(busRoute);
                    stationLines.addBusLine(busLine);
                    stationLines.addLineDirection(lineNumber, direction);
                }
            } while (cursor.moveToNext());
        }
        return stationLines;
    }

    public Cursor queryBusStations(String keyword) {
        StringBuilder sortOrder = new StringBuilder();
        sortOrder.append(KuaiXinData.BusStation.LAST_UPDATE_TIME + " DESC ")
                 .append("LIMIT 0,")
                 .append(MAX_RECENT_STATIONS);
        String selection = null;
        String[] selectionArgs = null;
        if(!TextUtils.isEmpty(keyword)){
            selection = KuaiXinData.BusStation.GPS_NUMBER + " LIKE ?";
            selectionArgs = new String[] { keyword + "%" };
        }
        Cursor cursor = mContentResolver.query(KuaiXinData.BusStation.CONTENT_URI, BUS_STATION_PROJECTION,
                selection, selectionArgs, sortOrder.toString());
        return cursor;
    }

    private Cursor queryLastStation() {
        String sortOrder = ITrafficData.KuaiXinData.BusStation.LAST_UPDATE_TIME + " DESC LIMIT 0,1";
        Cursor cursor = mContentResolver.query(ITrafficData.KuaiXinData.BusStation.CONTENT_URI, BUS_STATION_PROJECTION, null, null, sortOrder);
        return cursor;
    }

    private Cursor queryBusStationLines(String gpsNumber) {
        String selection = ITrafficData.KuaiXinData.BusStation.GPS_NUMBER + "=?";
        String[] selectionArgs = new String[]{ gpsNumber };
        String sortOrder = ITrafficData.KuaiXinData.BusStation.LAST_UPDATE_TIME + " DESC ";
        Cursor cursor = mContentResolver.query(ITrafficData.KuaiXinData.BusStationLines.CONTENT_URI, BUS_STATION_LINES_PROJECTION, selection, selectionArgs, sortOrder);
        return cursor;
    }

}
