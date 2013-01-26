/**
 * 
 */

package com.telepathic.finder.sdk.traffic.store;

import java.util.ArrayList;

import android.R.interpolator;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.telepathic.finder.sdk.traffic.ConsumerRecord;
import com.telepathic.finder.sdk.traffic.ConsumerRecord.ConsumerType;
import com.telepathic.finder.sdk.traffic.ConsumptionInfo;
import com.telepathic.finder.sdk.traffic.CountConsumerRecord;
import com.telepathic.finder.sdk.traffic.EWalletConsumerRecord;
import com.telepathic.finder.sdk.traffic.store.ITrafficeStore.BusRouteColumns;
import com.telepathic.finder.sdk.traffic.store.ITrafficeStore.BusRouteStationColumns;
import com.telepathic.finder.sdk.traffic.store.ITrafficeStore.BusStationColumns;
import com.telepathic.finder.sdk.traffic.store.ITrafficeStore.ConsumerRecordsColumns;
import com.telepathic.finder.util.Utils;

/**
 * @author Tim.Lian
 */
public class TrafficeStore {

    private static final String TAG = TrafficeStore.class.getSimpleName();

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "finder.db";
    
    private static final String TABLE_CONSUMER_RECORD = "consumerRecord";
    private static final String TABLE_BUS_ROUTE = "busRoute";
    private static final String TABLE_BUS_STATION = "busStation";
    private static final String TABLE_BUS_ROUTE_STATION = "busRouteStation";

    //conlumn index
    private static final int IDX_ROW_ID  = 0;
    private static final int IDX_CARD_ID = 1;
    private static final int IDX_BUS_LINE_NUMBER = 2;
    private static final int IDX_LICENSE_PLATE_NUMBER = 3;
    private static final int IDX_CONSUMPTION_TIME = 4;
    private static final int IDX_CONSUMPTION = 5;
    private static final int IDX_RESIDUAL = 6;
    private static final int IDX_CONSUMPTION_TYPE = 7;

    private final DbHelper dbHelper;

    private static TrafficeStore defaultStore;

    private final Object mLock = new Object();

    public static synchronized TrafficeStore getDefaultStore(Context context) {
        if (defaultStore == null) {
            defaultStore = new TrafficeStore(context);
        }
        return defaultStore;
    }

    private TrafficeStore(Context context) {
        dbHelper = new DbHelper(context);
        Log.i(TAG, "Initialized data");
    }

    public long insertRecord(ConsumerRecord record) {
        synchronized (mLock) {
            long result = -1;
            if (record != null) {
                ContentValues values = new ContentValues();
                values.put(ConsumerRecordsColumns.CARD_NUMBER, record.getCardId());
                values.put(ConsumerRecordsColumns.LINE_NUMBER, record.getLineNumber());
                values.put(ConsumerRecordsColumns.BUS_NUMBER, record.getBusNumber());
                values.put(ConsumerRecordsColumns.DATE, Utils.formatDate(record.getConsumerTime()));
                values.put(ConsumerRecordsColumns.CONSUMPTION, record.getConsumption());
                values.put(ConsumerRecordsColumns.RESIDUAL, record.getResidual());
                values.put(ConsumerRecordsColumns.TYPE, record.getType().toString());
                result = insertOrIgnore(values, TABLE_CONSUMER_RECORD);
            }
            return result;
        }
    }
    
    public long insertBusRoute(ContentValues route) {
    	return insertOrIgnore(route, TABLE_BUS_ROUTE);
    }
    
    public long insertBusStation(ContentValues station) {
    	long rowId = insertOrIgnore(station, TABLE_BUS_STATION);
    	if (rowId == -1) {
    		synchronized (mLock) {
    			SQLiteDatabase db = dbHelper.getWritableDatabase();
    			try {
    				String[] projection = new String[] { BusStationColumns._ID };
    				String where = BusStationColumns.NAME + "=?";
    				String[] whereArgs = new String[] {station.getAsString(BusStationColumns.NAME)};
    				Cursor cursor = db.query(TABLE_BUS_STATION, projection, where, whereArgs, null, null, null);
    				if (cursor != null) {
    					try {
    						cursor.moveToFirst();
    						rowId = cursor.getLong(0);
    					} catch (Exception e) {
    						Utils.error(TAG, e.getLocalizedMessage());
    					} finally {
    						cursor.close();
    					}
    				}
    			} catch (Exception e) {
    				Utils.error(TAG, e.getLocalizedMessage());
    			} finally {
    				db.close();
    			}
			}
    	}
    	return rowId;
    }
    
    public long insertBusRouteStation(ContentValues routeStation) {
    	return insertOrIgnore(routeStation, TABLE_BUS_ROUTE_STATION);
    }

    public void deleteAllRecords() {
        synchronized (mLock) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            try {
                db.delete(TABLE_CONSUMER_RECORD, null, null);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                db.close();
            }
        }
    }

    private long insertOrIgnore(ContentValues values, String table) {
    	long rowId = -1;
    	synchronized (mLock) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            try {
            	rowId = db.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            } catch (Exception e) {
            	Utils.debug(TAG, "insertOrIgnore failed: " + e.getLocalizedMessage());
            } finally {
                db.close();
            }
    	}
        return rowId;
    }

    public ConsumptionInfo getConsumptionInfo(String cardId) {
        ArrayList<ConsumerRecord> consumerRecords = getConsumptionRecords(cardId);
        ConsumptionInfo info = new ConsumptionInfo();
        if (consumerRecords.size() > 0) {
            info.setCardId(cardId);
        }
        for (ConsumerRecord record : consumerRecords) {
            switch(record.getType()){
                case COUNT:
                    if (info.getResidualCount() == null) {
                        info.setResidualCount(record.getResidual());
                    }
                    break;
                case EWALLET:
                    if (info.getResidualAmount() == null) {
                        info.setResidualAmount(record.getResidual());
                    }
                    break;
                default:
                    throw new RuntimeException("Unknown consumer type !!!");
            }
            if (info.getResidualAmount() != null && info.getResidualCount() != null) {
                break;
            }
        }
        info.setRecordList(consumerRecords);
        return info;
    }

    public ArrayList<ConsumerRecord> getConsumptionRecords(String cardId) {
        synchronized (mLock) {
            ArrayList<ConsumerRecord> records = new ArrayList<ConsumerRecord>();
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            try {
                Cursor cursor = db.query(TABLE_CONSUMER_RECORD, null,
                        ConsumerRecordsColumns.CARD_NUMBER + " like " + "\'%" + cardId
                        + "%\'", null, null, null,
                        ConsumerRecordsColumns.DATE + " DESC");
                //cursor.setNotificationUri(null, null);
                if (cursor != null) {
                    try {
                        ConsumerRecord record = null;
                        while (cursor.moveToNext()) {
                            ConsumerType type = ConsumerType.valueOf(cursor.getString(IDX_CONSUMPTION_TYPE));
                            if (type == ConsumerType.COUNT) {
                                record = new CountConsumerRecord();
                            }
                            if (type == ConsumerType.EWALLET) {
                                record = new EWalletConsumerRecord();
                            }
                            record.setCardID(cursor.getString(IDX_CARD_ID));
                            record.setBusNumber(cursor.getString(IDX_LICENSE_PLATE_NUMBER));
                            record.setLineNumber(cursor.getString(IDX_BUS_LINE_NUMBER));
                            record.setConsumerTime(Utils.parseDate(cursor.getString(IDX_CONSUMPTION_TIME)));
                            record.setConsumption(cursor.getString(IDX_CONSUMPTION));
                            record.setResidual(cursor.getString(IDX_RESIDUAL));
                            records.add(record);
                        }
                    } finally {
                        cursor.close();
                    }

                }
            } finally {
                db.close();
            }
            return records;
        }
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
            String createTableSql1 = "CREATE TABLE " + TABLE_CONSUMER_RECORD + " ("
                    + ConsumerRecordsColumns._ID + " integer primary key, "
                    + ConsumerRecordsColumns.CARD_NUMBER + " text, "
                    + ConsumerRecordsColumns.LINE_NUMBER + " text, "
                    + ConsumerRecordsColumns.BUS_NUMBER + " text, "
                    + ConsumerRecordsColumns.DATE + " text, "
                    + ConsumerRecordsColumns.CONSUMPTION + " text, "
                    + ConsumerRecordsColumns.RESIDUAL + " text, "
                    + ConsumerRecordsColumns.TYPE + " text, "
                    + "UNIQUE (" + ConsumerRecordsColumns.CARD_NUMBER + ", "
                    + ConsumerRecordsColumns.DATE + " )"+ " )";
            
            String createTableSql2 = "CREATE TABLE " + TABLE_BUS_ROUTE + " ("
                    + BusRouteColumns._ID + " integer primary key, "
                    + BusRouteColumns.DIRECTION + " text, "
                    + BusRouteColumns.LINE_NUMBER + " text, "
                    + BusRouteColumns.DEPARTURE_TIME + " text, "
                    + BusRouteColumns.CLOSE_OFF_TIME + " text)";
            
            String createTableSql3 = "CREATE TABLE " + TABLE_BUS_STATION + " ("
                    + BusStationColumns._ID + " integer primary key, "
                    + BusStationColumns.NAME + " text, "
                    + BusStationColumns.GPS_NUMBER + " text, "
                    + BusStationColumns.LONGITUDE + " text, "
                    + BusStationColumns.LATITUDE + " text, "
                    + "UNIQUE (" + BusStationColumns.NAME + ")"+ " )";
            
            String createTableSql4 = "CREATE TABLE " + TABLE_BUS_ROUTE_STATION + " ("
                    + BusRouteStationColumns.ROUTE_ID + " INTEGER, "
                    + BusRouteStationColumns.STATION_ID + " INTEGER, "
                    + BusRouteStationColumns.INDEX + " INTEGER, "
                    + "FOREIGN KEY" + " (" + BusRouteStationColumns.ROUTE_ID + ") " 
                    + "REFERENCES " + TABLE_BUS_ROUTE + "(" + BusRouteColumns._ID + "), "
                    + "FOREIGN KEY" + " (" + BusRouteStationColumns.STATION_ID + ") "
                    + "REFERENCES " + TABLE_BUS_STATION + "(" + BusStationColumns._ID + ") )";
            
            Utils.debug(TAG, "onCreated sql: " + createTableSql1);
            db.execSQL(createTableSql1);
            Utils.debug(TAG, "onCreated sql: " + createTableSql2);
            db.execSQL(createTableSql2);
            Utils.debug(TAG, "onCreated sql: " + createTableSql3);
            db.execSQL(createTableSql3);
            Utils.debug(TAG, "onCreated sql: " + createTableSql4);
            db.execSQL(createTableSql4);
        }

        // Called whenever newVersion != oldVersion
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	Utils.debug(TAG, "onUpgrade");
        	// drops the old tables
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
