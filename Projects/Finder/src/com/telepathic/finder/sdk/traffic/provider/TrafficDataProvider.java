package com.telepathic.finder.sdk.traffic.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.telepathic.finder.sdk.traffic.provider.ITrafficData.BusCardColumns;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData.BusRouteColumns;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData.BusRouteStationColumns;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData.BusStationColumns;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData.ConsumerRecordColumns;
import com.telepathic.finder.util.Utils;

public class TrafficDataProvider extends ContentProvider {
	private static final String TAG = TrafficDataProvider.class.getSimpleName();
	
	private static final int DB_VERSION = 1;
    private static final String DB_NAME = "finder.db";
    
    private static final String TABLE_BUS_CARD = "busCard";
    private static final String TABLE_CONSUMER_RECORD = "consumerRecord";
    private static final String TABLE_BUS_ROUTE = "busRoute";
    private static final String TABLE_BUS_STATION = "busStation";
    private static final String TABLE_BUS_ROUTE_STATION = "busRouteStation";
    
	private static final int MATCH_BUS_CARD = 1;
	private static final int MATCH_BUS_CARD_BY_ID = 2;
	private static final int MATCH_CONSUMER_RECORD = 3;
	private static final int MATCH_BUS_ROUTE = 4;
	private static final int MATCH_BUS_STATION = 5;
	private static final int MATCH_BUS_ROUTE_STATION = 6;
	
	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	
	static {
		sUriMatcher.addURI(ITrafficData.AUTHORITY, "busCard", MATCH_BUS_CARD);
		sUriMatcher.addURI(ITrafficData.AUTHORITY, "busCard/#", MATCH_BUS_CARD_BY_ID);
		sUriMatcher.addURI(ITrafficData.AUTHORITY, "consumerRecord", MATCH_CONSUMER_RECORD);
		sUriMatcher.addURI(ITrafficData.AUTHORITY, "busRoute", MATCH_BUS_ROUTE);
		sUriMatcher.addURI(ITrafficData.AUTHORITY, "busStation", MATCH_BUS_STATION);
		sUriMatcher.addURI(ITrafficData.AUTHORITY, "busRouteStation", MATCH_BUS_ROUTE_STATION);
	}
	
	private DbHelper mDBHelper;
	
	@Override
	public boolean onCreate() {
		mDBHelper = new DbHelper(getContext());
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		String tableName = null;
		switch (sUriMatcher.match(uri)) {
		case MATCH_BUS_CARD:
			tableName = TABLE_BUS_CARD;
			break;
		case MATCH_BUS_CARD_BY_ID:
			tableName = TABLE_CONSUMER_RECORD;
			break;
		case MATCH_BUS_ROUTE:
			tableName = TABLE_BUS_ROUTE;
			break;
		case MATCH_BUS_STATION:
			tableName = TABLE_BUS_STATION;
			break;
		case MATCH_BUS_ROUTE_STATION:
			tableName = TABLE_BUS_ROUTE_STATION;
			break;
		default:
			throw new IllegalArgumentException("query() - Unknown uri: " + uri);
		}
		SQLiteDatabase db = mDBHelper.getReadableDatabase();
		Cursor cursor = db.query(tableName, projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		Utils.debug(TAG, "query cursor: " + cursor.getClass().getName());
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		String mimeType = "";
		switch (sUriMatcher.match(uri)) {
		case MATCH_BUS_CARD:
			mimeType = ITrafficData.BusCard.CONTENT_TYPE;
			break;
		case MATCH_BUS_CARD_BY_ID:
			mimeType = ITrafficData.BusCard.CONTENT_ITEM_TYPE;
			break;
		case MATCH_CONSUMER_RECORD:
			mimeType = ITrafficData.ConsumerRecord.CONTENT_TYPE;
			break;
		case MATCH_BUS_ROUTE:
			mimeType = ITrafficData.BusRoute.CONTENT_TYPE;
			break;
		case MATCH_BUS_STATION:
			mimeType = ITrafficData.BusStation.CONTENT_TYPE;
			break;
		case MATCH_BUS_ROUTE_STATION:
			mimeType = ITrafficData.BusRouteStation.CONTENT_TYPE;
			break;
		default:
			throw new IllegalArgumentException("getType() - Unknown uri: " + uri);
		}
		return mimeType;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		String tableName = null;
		switch (sUriMatcher.match(uri)) {
		case MATCH_BUS_CARD:
			tableName = TABLE_BUS_CARD;
			break;
		case MATCH_CONSUMER_RECORD:
			tableName = TABLE_CONSUMER_RECORD;
			break;
		case MATCH_BUS_ROUTE:
			tableName = TABLE_BUS_ROUTE;
			break;
		case MATCH_BUS_STATION:
			tableName = TABLE_BUS_STATION;
			break;
		case MATCH_BUS_ROUTE_STATION:
			tableName = TABLE_BUS_ROUTE_STATION;
			break;
		default:
			throw new UnsupportedOperationException("Can't insert into uri: " + uri);
		}
		Uri retUri = null;
		long rowId = db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE);
		if (rowId > 0) {
			retUri = Uri.withAppendedPath(uri, String.valueOf(rowId));
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return retUri;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}
	
	class DbHelper extends SQLiteOpenHelper {
        Context context;

        public DbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
            this.context = context;
        }

        // Called only once, first time the DB is created
        @Override
        public void onCreate(SQLiteDatabase db) {
        	db.execSQL("CREATE TABLE " + TABLE_BUS_CARD + " ("
                    + BusCardColumns._ID + " INTEGER PRIMARY KEY, "
                    + BusCardColumns.CARD_NUMBER + " TEXT, "
                    + BusCardColumns.RESIDUAL_COUNT + " TEXT, "
                    + BusCardColumns.RESIDUAL_AMOUNT + " TEXT, "
                    + BusCardColumns.LAST_DATE + " TEXT, "
                    + "UNIQUE (" + BusCardColumns.CARD_NUMBER + ")"+ " )");
        	
        	db.execSQL("CREATE TABLE " + TABLE_CONSUMER_RECORD + " ("
                    + ConsumerRecordColumns._ID + " INTEGER PRIMARY KEY, "
                    + ConsumerRecordColumns.CARD_ID + " INTEGER, "
                    + ConsumerRecordColumns.LINE_NUMBER + " TEXT, "
                    + ConsumerRecordColumns.BUS_NUMBER + " TEXT, "
                    + ConsumerRecordColumns.DATE + " TEXT, "
                    + ConsumerRecordColumns.CONSUMPTION + " TEXT, "
                    + ConsumerRecordColumns.RESIDUAL + " TEXT, "
                    + ConsumerRecordColumns.TYPE + " TEXT, "
                    + "FOREIGN KEY" + " (" + ConsumerRecordColumns.CARD_ID + ") "
                    + "REFERENCES " + TABLE_BUS_CARD + "(" + BusCardColumns._ID + ") ON DELETE CASCADE, "
                    + "UNIQUE (" + ConsumerRecordColumns.CARD_ID + ", "
                    + ConsumerRecordColumns.DATE + " )"+ " )");
        	
        	db.execSQL("CREATE TABLE " + TABLE_BUS_ROUTE + " ("
                    + BusRouteColumns._ID + " INTEGER PRIMARY KEY, "
                    + BusRouteColumns.DIRECTION + " TEXT, "
                    + BusRouteColumns.LINE_NUMBER + " TEXT, "
                    + BusRouteColumns.DEPARTURE_TIME + " TEXT, "
                    + BusRouteColumns.CLOSE_OFF_TIME + " TEXT, "
                    + "UNIQUE (" + BusRouteColumns.LINE_NUMBER + ", "
                    + BusRouteColumns.DIRECTION + " )"+ " )");
        	
        	db.execSQL("CREATE TABLE " + TABLE_BUS_STATION + " ("
                    + BusStationColumns._ID + " INTEGER PRIMARY KEY, "
                    + BusStationColumns.NAME + " TEXT, "
                    + BusStationColumns.GPS_NUMBER + " TEXT, "
                    + BusStationColumns.LONGITUDE + " TEXT, "
                    + BusStationColumns.LATITUDE + " TEXT, "
                    + "UNIQUE (" + BusStationColumns.GPS_NUMBER + ")"+ " )");
            
        	db.execSQL("CREATE TABLE " + TABLE_BUS_ROUTE_STATION + " ("
                    + BusRouteStationColumns.ROUTE_ID + " INTEGER, "
                    + BusRouteStationColumns.STATION_ID + " INTEGER, "
                    + BusRouteStationColumns.INDEX + " INTEGER, "
                    + "FOREIGN KEY" + " (" + BusRouteStationColumns.ROUTE_ID + ") " 
                    + "REFERENCES " + TABLE_BUS_ROUTE + "(" + BusRouteColumns._ID + ") ON DELETE CASCADE, "
                    + "FOREIGN KEY" + " (" + BusRouteStationColumns.STATION_ID + ") "
                    + "REFERENCES " + TABLE_BUS_STATION + "(" + BusStationColumns._ID + ") ON DELETE CASCADE, "
                    + "UNIQUE (" + BusRouteStationColumns.ROUTE_ID + ", "
                    + BusRouteStationColumns.STATION_ID + " )"+ " )");
        }

        // Called whenever newVersion != oldVersion
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	Utils.debug(TAG, "onUpgrade");
        	// drops the old tables
        	db.execSQL("drop table if exists " + TABLE_BUS_CARD);
            db.execSQL("drop table if exists " + TABLE_CONSUMER_RECORD); 
            db.execSQL("drop table if exists " + TABLE_BUS_ROUTE); 
            db.execSQL("drop table if exists " + TABLE_BUS_STATION); 
            db.execSQL("drop table if exists " + TABLE_BUS_ROUTE_STATION); 
            onCreate(db);
        }
        
        @Override
        public void onOpen(SQLiteDatabase db) {
        	super.onOpen(db);
        	if (!db.isReadOnly()) {
                // Enable foreign key constraints
                db.execSQL("PRAGMA foreign_keys=ON;");
            }
        }
    }
	
}
