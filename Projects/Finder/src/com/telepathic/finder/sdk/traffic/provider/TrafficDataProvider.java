package com.telepathic.finder.sdk.traffic.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.telepathic.finder.sdk.traffic.provider.ITrafficData.BaiDuData;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData.KuaiXinData;

public class TrafficDataProvider extends ContentProvider {
	private static final String TAG = TrafficDataProvider.class.getSimpleName();
	
	private static final int DB_VERSION = 1;
    private static final String DB_NAME = "finder.db";
    
    private static final String TABLE_KUAI_XIN_BUS_CARD = "kuaiXinBusCard";
    private static final String TABLE_KUAI_XIN_CONSUMER_RECORD = "kuaiXinConsumerRecord";
    
    private static final String TABLE_KUAI_XIN_BUS_ROUTE = "kuaiXinBusRoute";
    private static final String TABLE_KUAI_XIN_BUS_STATION = "kuaiXinBusStation";
    private static final String TABLE_KUAI_XIN_BUS_ROUTE_STATION = "kuaiXinBusRouteStation";
    
    private static final String TABLE_BAI_DU_BUS_ROUTE = "baiDuBusRoute";
    private static final String TABLE_BAI_DU_BUS_STATION = "baiDuBusStation";
    private static final String TABLE_BAI_DU_BUS_ROUTE_STATION = "baiDuBusRouteStation";
    
	private static final int MATCH_KUAI_XIN_BUS_CARD = 1;
	private static final int MATCH_KUAI_XIN_BUS_CARD_BY_ID = 2;
	private static final int MATCH_KUAI_XIN_CONSUMER_RECORD = 3;
	private static final int MATCH_KUAI_XIN_BUS_ROUTE = 4;
	private static final int MATCH_KUAI_XIN_BUS_STATION = 5;
	private static final int MATCH_KUAI_XIN_BUS_ROUTE_STATION = 6;
	private static final int MATCH_BAI_DU_BUS_ROUTE = 7;
	private static final int MATCH_BAI_DU_BUS_STATION = 8;
	private static final int MATCH_BAI_DU_BUS_ROUTE_STATION = 9;
	
	private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	
	static {
		sUriMatcher.addURI(ITrafficData.AUTHORITY, "kuaiXinBusCard", MATCH_KUAI_XIN_BUS_CARD);
		sUriMatcher.addURI(ITrafficData.AUTHORITY, "kuaiXinBusCard/#", MATCH_KUAI_XIN_BUS_CARD_BY_ID);
		sUriMatcher.addURI(ITrafficData.AUTHORITY, "kuaiXinConsumerRecord", MATCH_KUAI_XIN_CONSUMER_RECORD);
		sUriMatcher.addURI(ITrafficData.AUTHORITY, "kuaiXinBusRoute", MATCH_KUAI_XIN_BUS_ROUTE);
		sUriMatcher.addURI(ITrafficData.AUTHORITY, "kuaiXinBusStation", MATCH_KUAI_XIN_BUS_STATION);
		sUriMatcher.addURI(ITrafficData.AUTHORITY, "kuaiXinBusRouteStation", MATCH_KUAI_XIN_BUS_ROUTE_STATION);
		sUriMatcher.addURI(ITrafficData.AUTHORITY, "baiDuBusRoute", MATCH_BAI_DU_BUS_ROUTE);
		sUriMatcher.addURI(ITrafficData.AUTHORITY, "baiDuBusStation", MATCH_BAI_DU_BUS_STATION);
		sUriMatcher.addURI(ITrafficData.AUTHORITY, "baiDuBusRouteStation", MATCH_BAI_DU_BUS_ROUTE_STATION);
	}
	
	private static final String BUS_CARD_JOIN_CONSUMER_RECORD = 
			TABLE_KUAI_XIN_BUS_CARD + " LEFT OUTER JOIN " + TABLE_KUAI_XIN_CONSUMER_RECORD + " ON "
			+ "(" + TABLE_KUAI_XIN_BUS_CARD + "." + KuaiXinData.BusCardColumns._ID + "=" + KuaiXinData.ConsumerRecordColumns.CARD_ID + ")";
	
	
	private DbHelper mDBHelper;
	
	@Override
	public boolean onCreate() {
		mDBHelper = new DbHelper(getContext());
		return false;
	}

	@Override
	public synchronized Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		String tableName = null;
		switch (sUriMatcher.match(uri)) {
		case MATCH_KUAI_XIN_BUS_CARD:
			tableName = TABLE_KUAI_XIN_BUS_CARD;
			break;
		case MATCH_KUAI_XIN_BUS_CARD_BY_ID:
			tableName = TABLE_KUAI_XIN_BUS_CARD;
			break;
		case MATCH_KUAI_XIN_CONSUMER_RECORD:
			tableName = BUS_CARD_JOIN_CONSUMER_RECORD;
			break;
		case MATCH_KUAI_XIN_BUS_ROUTE:
			tableName = TABLE_KUAI_XIN_BUS_ROUTE;
			break;
		case MATCH_KUAI_XIN_BUS_STATION:
			tableName = TABLE_KUAI_XIN_BUS_STATION;
			break;
		case MATCH_KUAI_XIN_BUS_ROUTE_STATION:
			tableName = TABLE_KUAI_XIN_BUS_ROUTE_STATION;
			break;
		case MATCH_BAI_DU_BUS_ROUTE:
			tableName = TABLE_BAI_DU_BUS_ROUTE;
			break;
		case MATCH_BAI_DU_BUS_STATION:
			tableName = TABLE_BAI_DU_BUS_STATION;
			break;
		case MATCH_BAI_DU_BUS_ROUTE_STATION:
			tableName = TABLE_BAI_DU_BUS_ROUTE_STATION;
			break;	
		default:
			throw new IllegalArgumentException("query() - Unknown uri: " + uri);
		}
		SQLiteDatabase db = mDBHelper.getReadableDatabase();
		Cursor cursor = db.query(tableName, projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public synchronized String getType(Uri uri) {
		String mimeType = "";
		switch (sUriMatcher.match(uri)) {
		case MATCH_KUAI_XIN_BUS_CARD:
			mimeType = ITrafficData.KuaiXinData.BusCard.CONTENT_TYPE;
			break;
		case MATCH_KUAI_XIN_BUS_CARD_BY_ID:
			mimeType = ITrafficData.KuaiXinData.BusCard.CONTENT_ITEM_TYPE;
			break;
		case MATCH_KUAI_XIN_CONSUMER_RECORD:
			mimeType = ITrafficData.KuaiXinData.ConsumerRecord.CONTENT_TYPE;
			break;
		case MATCH_KUAI_XIN_BUS_ROUTE:
			mimeType = ITrafficData.KuaiXinData.BusRoute.CONTENT_TYPE;
			break;
		case MATCH_KUAI_XIN_BUS_STATION:
			mimeType = ITrafficData.KuaiXinData.BusStation.CONTENT_TYPE;
			break;
		case MATCH_KUAI_XIN_BUS_ROUTE_STATION:
			mimeType = ITrafficData.KuaiXinData.BusRouteStation.CONTENT_TYPE;
			break;
		case MATCH_BAI_DU_BUS_ROUTE:
			mimeType = ITrafficData.BaiDuData.BusRoute.CONTENT_TYPE;
			break;
		case MATCH_BAI_DU_BUS_STATION:
			mimeType = ITrafficData.BaiDuData.BusStation.CONTENT_TYPE;
			break;
		case MATCH_BAI_DU_BUS_ROUTE_STATION:
			mimeType = ITrafficData.BaiDuData.BusRouteStation.CONTENT_TYPE;
			break;
		default:
			throw new IllegalArgumentException("getType() - Unknown uri: " + uri);
		}
		return mimeType;
	}
	
	@Override
	public synchronized Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		String tableName = null;
		switch (sUriMatcher.match(uri)) {
		case MATCH_KUAI_XIN_BUS_CARD:
			tableName = TABLE_KUAI_XIN_BUS_CARD;
			break;
		case MATCH_KUAI_XIN_CONSUMER_RECORD:
			tableName = TABLE_KUAI_XIN_CONSUMER_RECORD;
			break;
		case MATCH_KUAI_XIN_BUS_ROUTE:
			tableName = TABLE_KUAI_XIN_BUS_ROUTE;
			break;
		case MATCH_KUAI_XIN_BUS_STATION:
			tableName = TABLE_KUAI_XIN_BUS_STATION;
			break;
		case MATCH_KUAI_XIN_BUS_ROUTE_STATION:
			tableName = TABLE_KUAI_XIN_BUS_ROUTE_STATION;
			break;
		case MATCH_BAI_DU_BUS_ROUTE:
			tableName = TABLE_BAI_DU_BUS_ROUTE;
			break;
		case MATCH_BAI_DU_BUS_STATION:
			tableName = TABLE_BAI_DU_BUS_STATION;
			break;
		case MATCH_BAI_DU_BUS_ROUTE_STATION:
			tableName = TABLE_BAI_DU_BUS_ROUTE_STATION;
			break;
		default:
			throw new UnsupportedOperationException("Can't insert into uri: " + uri);
		}
		long rowId = db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE);
		return Uri.withAppendedPath(uri, String.valueOf(rowId));
	}
	

	@Override
	public synchronized int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public synchronized int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		String tableName = null;
		switch (sUriMatcher.match(uri)) {
		case MATCH_KUAI_XIN_BUS_CARD:
			tableName = TABLE_KUAI_XIN_BUS_CARD;
			break;
		case MATCH_KUAI_XIN_CONSUMER_RECORD:
			tableName = TABLE_KUAI_XIN_CONSUMER_RECORD;
			break;
		case MATCH_KUAI_XIN_BUS_ROUTE:
			tableName = TABLE_KUAI_XIN_BUS_ROUTE;
			break;
		case MATCH_KUAI_XIN_BUS_STATION:
			tableName = TABLE_KUAI_XIN_BUS_STATION;
			break;
		case MATCH_KUAI_XIN_BUS_ROUTE_STATION:
			tableName = TABLE_KUAI_XIN_BUS_ROUTE_STATION;
			break;
		case MATCH_BAI_DU_BUS_ROUTE:
			tableName = TABLE_BAI_DU_BUS_ROUTE;
			break;
		case MATCH_BAI_DU_BUS_STATION:
			tableName = TABLE_BAI_DU_BUS_STATION;
			break;
		case MATCH_BAI_DU_BUS_ROUTE_STATION:
			tableName = TABLE_BAI_DU_BUS_ROUTE_STATION;
			break;
		default:
			throw new UnsupportedOperationException("Can't update uri: " + uri);
		}
		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		return db.updateWithOnConflict(tableName, values, selection, selectionArgs, SQLiteDatabase.CONFLICT_IGNORE);
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
        	db.execSQL("CREATE TABLE " + TABLE_KUAI_XIN_BUS_CARD + " ("
                    + KuaiXinData.BusCardColumns._ID + " INTEGER PRIMARY KEY, "
                    + KuaiXinData.BusCardColumns.CARD_NUMBER + " TEXT, "
                    + KuaiXinData.BusCardColumns.RESIDUAL_COUNT + " TEXT, "
                    + KuaiXinData.BusCardColumns.RESIDUAL_AMOUNT + " TEXT, "
                    + KuaiXinData.BusCardColumns.LAST_UPDATE_TIME + " INTEGER, "
                    + "UNIQUE (" + KuaiXinData.BusCardColumns.CARD_NUMBER + ")"+ " )");
        	
        	db.execSQL("CREATE TABLE " + TABLE_KUAI_XIN_CONSUMER_RECORD + " ("
                    + KuaiXinData.ConsumerRecordColumns._ID + " INTEGER PRIMARY KEY, "
                    + KuaiXinData.ConsumerRecordColumns.CARD_ID + " INTEGER, "
                    + KuaiXinData.ConsumerRecordColumns.LINE_NUMBER + " TEXT, "
                    + KuaiXinData.ConsumerRecordColumns.BUS_NUMBER + " TEXT, "
                    + KuaiXinData.ConsumerRecordColumns.DATE + " TEXT, "
                    + KuaiXinData.ConsumerRecordColumns.CONSUMPTION + " TEXT, "
                    + KuaiXinData.ConsumerRecordColumns.RESIDUAL + " TEXT, "
                    + KuaiXinData.ConsumerRecordColumns.TYPE + " TEXT, "
                    + "FOREIGN KEY" + " (" + KuaiXinData.ConsumerRecordColumns.CARD_ID + ") "
                    + "REFERENCES " + TABLE_KUAI_XIN_BUS_CARD + "(" + KuaiXinData.BusCardColumns._ID + ") ON DELETE CASCADE, "
                    + "UNIQUE (" + KuaiXinData.ConsumerRecordColumns.CARD_ID + ", "
                    + KuaiXinData.ConsumerRecordColumns.DATE + " )"+ " )");
        	
        	db.execSQL("CREATE TABLE " + TABLE_KUAI_XIN_BUS_ROUTE + " ("
                    + KuaiXinData.BusRouteColumns._ID + " INTEGER PRIMARY KEY, "
                    + KuaiXinData.BusRouteColumns.LINE_NUMBER + " TEXT, "
                    + KuaiXinData.BusRouteColumns.DIRECTION + " TEXT, "
                    + KuaiXinData.BusRouteColumns.START_TIME + " TEXT, "
                    + KuaiXinData.BusRouteColumns.END_TIME + " TEXT, "
                    + KuaiXinData.BusRouteColumns.STATIONS + " TEXT, "
                    + KuaiXinData.BusRouteColumns.LAST_UPDATE_TIME + " INTEGER, "
                    + "UNIQUE (" + KuaiXinData.BusRouteColumns.LINE_NUMBER + ", " 
                    + KuaiXinData.BusRouteColumns.DIRECTION + ")"+ " )");
        	
        	db.execSQL("CREATE TABLE " + TABLE_KUAI_XIN_BUS_STATION + " ("
                    + KuaiXinData.BusStationColumns._ID + " INTEGER PRIMARY KEY, "
                    + KuaiXinData.BusStationColumns.NAME + " TEXT, "
                    + KuaiXinData.BusStationColumns.GPS_NUMBER + " TEXT, "
                    + "UNIQUE (" + KuaiXinData.BusStationColumns.GPS_NUMBER + ")"+ " )");
            
        	db.execSQL("CREATE TABLE " + TABLE_KUAI_XIN_BUS_ROUTE_STATION + " ("
                    + KuaiXinData.BusRouteStationColumns.ROUTE_ID + " INTEGER, "
                    + KuaiXinData.BusRouteStationColumns.STATION_ID + " INTEGER, "
                    + KuaiXinData.BusRouteStationColumns.INDEX + " INTEGER, "
                    + "FOREIGN KEY" + " (" + KuaiXinData.BusRouteStationColumns.ROUTE_ID + ") " 
                    + "REFERENCES " + TABLE_KUAI_XIN_BUS_ROUTE + "(" + KuaiXinData.BusRouteColumns._ID + ") ON DELETE CASCADE, "
                    + "FOREIGN KEY" + " (" + KuaiXinData.BusRouteStationColumns.STATION_ID + ") "
                    + "REFERENCES " + TABLE_KUAI_XIN_BUS_STATION + "(" + KuaiXinData.BusStationColumns._ID + ") ON DELETE CASCADE, "
                    + "UNIQUE (" + KuaiXinData.BusRouteStationColumns.ROUTE_ID + ", "
                    + KuaiXinData.BusRouteStationColumns.STATION_ID + " )"+ " )");
        	
        	db.execSQL("CREATE TABLE " + TABLE_BAI_DU_BUS_ROUTE + " ("
                    + BaiDuData.BusRouteColumns._ID + " INTEGER PRIMARY KEY, "
                    + BaiDuData.BusRouteColumns.LINE_NUMBER + " TEXT, "
                    + BaiDuData.BusRouteColumns.UID + " TEXT, "
                    + BaiDuData.BusRouteColumns.NAME + " TEXT, "
                    + BaiDuData.BusRouteColumns.LAST_UPDATE_TIME + " INTEGER, "
                    + "UNIQUE (" + BaiDuData.BusRouteColumns.UID + ")"+ " )");
        	
        	db.execSQL("CREATE TABLE " + TABLE_BAI_DU_BUS_STATION + " ("
                    + BaiDuData.BusStationColumns._ID + " INTEGER PRIMARY KEY, "
                    + BaiDuData.BusStationColumns.NAME + " TEXT, "
                    + BaiDuData.BusStationColumns.LATITUDE + " TEXT, "
                    + BaiDuData.BusStationColumns.LONGITUDE + " TEXT, "
                    + "UNIQUE (" + BaiDuData.BusStationColumns.LATITUDE + ", "
                    + BaiDuData.BusStationColumns.LONGITUDE + ")"+ " )");
            
        	db.execSQL("CREATE TABLE " + TABLE_BAI_DU_BUS_ROUTE_STATION + " ("
                    + BaiDuData.BusRouteStationColumns.ROUTE_ID + " INTEGER, "
                    + BaiDuData.BusRouteStationColumns.STATION_ID + " INTEGER, "
                    + BaiDuData.BusRouteStationColumns.INDEX + " INTEGER, "
                    + "FOREIGN KEY" + " (" + BaiDuData.BusRouteStationColumns.ROUTE_ID + ") " 
                    + "REFERENCES " + TABLE_BAI_DU_BUS_ROUTE + "(" + BaiDuData.BusRouteColumns._ID + ") ON DELETE CASCADE, "
                    + "FOREIGN KEY" + " (" + BaiDuData.BusRouteStationColumns.STATION_ID + ") "
                    + "REFERENCES " + TABLE_BAI_DU_BUS_STATION + "(" + BaiDuData.BusStationColumns._ID + ") ON DELETE CASCADE, "
                    + "UNIQUE (" + BaiDuData.BusRouteStationColumns.ROUTE_ID + ", "
                    + BaiDuData.BusRouteStationColumns.STATION_ID + " )"+ " )");
        }

        // Called whenever newVersion != oldVersion
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	// drops the old tables
        	db.execSQL("drop table if exists " + TABLE_KUAI_XIN_BUS_CARD);
            db.execSQL("drop table if exists " + TABLE_KUAI_XIN_CONSUMER_RECORD); 
            db.execSQL("drop table if exists " + TABLE_KUAI_XIN_BUS_ROUTE); 
            db.execSQL("drop table if exists " + TABLE_KUAI_XIN_BUS_STATION); 
            db.execSQL("drop table if exists " + TABLE_KUAI_XIN_BUS_ROUTE_STATION); 
            db.execSQL("drop table if exists " + TABLE_BAI_DU_BUS_ROUTE); 
            db.execSQL("drop table if exists " + TABLE_BAI_DU_BUS_STATION); 
            db.execSQL("drop table if exists " + TABLE_BAI_DU_BUS_ROUTE_STATION); 
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
