/**
 * Copyright (C) 2012 Telepathic LTD. All Rights Reserved.
 * 
 * * Author: Tim Lian
 */

package com.telepathic.finder.sdk.traffic.store;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.telepathic.finder.sdk.traffic.BusCard;
import com.telepathic.finder.sdk.traffic.ConsumerRecord;
import com.telepathic.finder.sdk.traffic.ConsumerRecord.ConsumerType;
import com.telepathic.finder.sdk.traffic.CountConsumerRecord;
import com.telepathic.finder.sdk.traffic.EWalletConsumerRecord;
import com.telepathic.finder.sdk.traffic.store.ITrafficeStore.BusCardColumns;
import com.telepathic.finder.sdk.traffic.store.ITrafficeStore.BusRouteColumns;
import com.telepathic.finder.sdk.traffic.store.ITrafficeStore.BusRouteStationColumns;
import com.telepathic.finder.sdk.traffic.store.ITrafficeStore.BusStationColumns;
import com.telepathic.finder.sdk.traffic.store.ITrafficeStore.ConsumerRecordColumns;
import com.telepathic.finder.util.Utils;

public class TrafficeStore {

    private static final String TAG = TrafficeStore.class.getSimpleName();

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "finder.db";
    
    private static final String TABLE_BUS_CARD = "busCard";
    private static final String TABLE_CONSUMER_RECORD = "consumerRecord";
    private static final String TABLE_BUS_ROUTE = "busRoute";
    private static final String TABLE_BUS_STATION = "busStation";
    private static final String TABLE_BUS_ROUTE_STATION = "busRouteStation";
    
	private static final String BUS_CARD_JOIN_CONSUMER_RECORD = 
			TABLE_BUS_CARD + " LEFT OUTER JOIN " + TABLE_CONSUMER_RECORD + " ON "
			+ "(" + TABLE_BUS_CARD + "." + BusCardColumns._ID + "=" + ConsumerRecordColumns.CARD_ID + ")";

    //conlumn index
	private static final int IDX_CARD_NUMBER = 0;
    private static final int IDX_LINE_NUMBER = 1;
    private static final int IDX_BUS_NUMBER = 2;
    private static final int IDX_CONSUMPTION_TIME = 3;
    private static final int IDX_CONSUMPTION = 4;
    private static final int IDX_RESIDUAL = 5;
    private static final int IDX_CONSUMPTION_TYPE = 6;

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

    public long insertBusCard(ContentValues busCard) {
    	return insertOrReplace(busCard, TABLE_BUS_CARD);
    }
    
    public long insertConsumerRecord(ContentValues record) {
        return insertOrReplace(record, TABLE_CONSUMER_RECORD);
    }
    
    public long insertBusRoute(ContentValues route) {
    	return insertOrReplace(route, TABLE_BUS_ROUTE);
    }
    
    public long insertBusStation(ContentValues station) {
    	return insertOrReplace(station, TABLE_BUS_STATION);
//    	if (rowId == -1) {
//    		synchronized (mLock) {
//    			SQLiteDatabase db = dbHelper.getWritableDatabase();
//    			try {
//    				String[] projection = new String[] { BusStationColumns._ID };
//    				String where = BusStationColumns.NAME + "=?";
//    				String[] whereArgs = new String[] {station.getAsString(BusStationColumns.NAME)};
//    				Cursor cursor = db.query(TABLE_BUS_STATION, projection, where, whereArgs, null, null, null);
//    				if (cursor != null) {
//    					try {
//    						cursor.moveToFirst();
//    						rowId = cursor.getLong(0);
//    					} catch (Exception e) {
//    						Utils.error(TAG, e.getLocalizedMessage());
//    					} finally {
//    						cursor.close();
//    					}
//    				}
//    			} catch (Exception e) {
//    				Utils.error(TAG, e.getLocalizedMessage());
//    			} finally {
//    				db.close();
//    			}
//			}
//    	}
 //   	return rowId;
    }
    
    public long insertBusRouteStation(ContentValues routeStation) {
    	return insertOrReplace(routeStation, TABLE_BUS_ROUTE_STATION);
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

    private long insertOrReplace(ContentValues values, String table) {
    	long rowId = -1;
    	synchronized (mLock) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            try {
            	rowId = db.insertWithOnConflict(table, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            } catch (Exception e) {
            	Utils.debug(TAG, "insertOrReplace failed: " + e.getLocalizedMessage());
            } finally {
                db.close();
            }
    	}
        return rowId;
    }

    public BusCard getConsumptionInfo(String cardNumber) {
        ArrayList<ConsumerRecord> consumerRecords = getConsumerRecords(cardNumber);
        BusCard busCard = getBusCard(cardNumber);
        busCard.setConsumerRecords(consumerRecords);
        return busCard;
    }

	public BusCard getBusCard(String cardNumber) {
		BusCard busCard = new BusCard();
		synchronized (mLock) {
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			try {
				String[] projection = new String[] {
						BusCardColumns.CARD_NUMBER,
						BusCardColumns.RESIDUAL_COUNT,
						BusCardColumns.RESIDUAL_AMOUNT,
						BusCardColumns.LAST_DATE };
				String where = BusCardColumns.CARD_NUMBER + "=?";
				String[] whereArgs = new String[] { cardNumber };
				Cursor cursor = db.query(TABLE_BUS_CARD, projection, where, whereArgs, null, null, null);
				if (cursor != null) {
					try {
						cursor.moveToFirst();
						final int idxCardNumber = cursor.getColumnIndex(BusCardColumns.CARD_NUMBER);
						final int idxResidualCount = cursor.getColumnIndex(BusCardColumns.RESIDUAL_COUNT);
						final int idxResidualAmount = cursor.getColumnIndex(BusCardColumns.RESIDUAL_AMOUNT);
						final int idxLastDate = cursor.getColumnIndex(BusCardColumns.LAST_DATE);
						busCard.setCardNumber(cursor.getString(idxCardNumber));
						busCard.setResidualCount(cursor.getString(idxResidualCount));
						busCard.setResidualAmount(cursor.getString(idxResidualAmount));
						busCard.setLastDate(Utils.parseDate(cursor.getString(idxLastDate)));
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						cursor.close();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				db.close();
			}
		}
		return busCard;
	}
    
    public ArrayList<ConsumerRecord> getConsumerRecords(String cardId) {
        synchronized (mLock) {
            ArrayList<ConsumerRecord> records = new ArrayList<ConsumerRecord>();
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            try {
            	String[] projection = new String[] {
            			BusCardColumns.CARD_NUMBER,
            			ConsumerRecordColumns.LINE_NUMBER,
            			ConsumerRecordColumns.BUS_NUMBER,
            			ConsumerRecordColumns.DATE,
            			ConsumerRecordColumns.CONSUMPTION,
            			ConsumerRecordColumns.RESIDUAL,
            			ConsumerRecordColumns.TYPE
            	};
                Cursor cursor = db.query(BUS_CARD_JOIN_CONSUMER_RECORD, projection,
                        BusCardColumns.CARD_NUMBER + " like " + "\'%" + cardId
                        + "%\'", null, null, null,
                        ConsumerRecordColumns.DATE + " DESC");
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
                            record.setCardID(cursor.getString(IDX_CARD_NUMBER));
                            record.setBusNumber(cursor.getString(IDX_BUS_NUMBER));
                            record.setLineNumber(cursor.getString(IDX_LINE_NUMBER));
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
        	 String createTableSql1 = "CREATE TABLE " + TABLE_BUS_CARD + " ("
                     + BusCardColumns._ID + " INTEGER PRIMARY KEY, "
                     + BusCardColumns.CARD_NUMBER + " text, "
                     + BusCardColumns.RESIDUAL_COUNT + " text, "
                     + BusCardColumns.RESIDUAL_AMOUNT + " text, "
                     + BusCardColumns.LAST_DATE + " text, "
                     + "UNIQUE (" + BusCardColumns.CARD_NUMBER + ")"+ " )";
        	 
            String createTableSql2 = "CREATE TABLE " + TABLE_CONSUMER_RECORD + " ("
                    + ConsumerRecordColumns._ID + " INTEGER PRIMARY KEY, "
                    + ConsumerRecordColumns.CARD_ID + " INTEGER, "
                    + ConsumerRecordColumns.LINE_NUMBER + " text, "
                    + ConsumerRecordColumns.BUS_NUMBER + " text, "
                    + ConsumerRecordColumns.DATE + " text, "
                    + ConsumerRecordColumns.CONSUMPTION + " text, "
                    + ConsumerRecordColumns.RESIDUAL + " text, "
                    + ConsumerRecordColumns.TYPE + " text, "
                    + "FOREIGN KEY" + " (" + ConsumerRecordColumns.CARD_ID + ") "
                    + "REFERENCES " + TABLE_BUS_CARD + "(" + BusCardColumns._ID + ") ON DELETE CASCADE, "
                    + "UNIQUE (" + ConsumerRecordColumns.CARD_ID + ", "
                    + ConsumerRecordColumns.DATE + " )"+ " )";
            
            String createTableSql3 = "CREATE TABLE " + TABLE_BUS_ROUTE + " ("
                    + BusRouteColumns._ID + " INTEGER PRIMARY KEY, "
                    + BusRouteColumns.DIRECTION + " text, "
                    + BusRouteColumns.LINE_NUMBER + " text, "
                    + BusRouteColumns.DEPARTURE_TIME + " text, "
                    + BusRouteColumns.CLOSE_OFF_TIME + " text, "
                    + "UNIQUE (" + BusRouteColumns.LINE_NUMBER + ", "
                    + BusRouteColumns.DIRECTION + " )"+ " )";
            
            String createTableSql4 = "CREATE TABLE " + TABLE_BUS_STATION + " ("
                    + BusStationColumns._ID + " INTEGER PRIMARY KEY, "
                    + BusStationColumns.NAME + " text, "
                    + BusStationColumns.GPS_NUMBER + " text, "
                    + BusStationColumns.LONGITUDE + " text, "
                    + BusStationColumns.LATITUDE + " text, "
                    + "UNIQUE (" + BusStationColumns.GPS_NUMBER + ")"+ " )";
            
            String createTableSql5 = "CREATE TABLE " + TABLE_BUS_ROUTE_STATION + " ("
                    + BusRouteStationColumns.ROUTE_ID + " INTEGER, "
                    + BusRouteStationColumns.STATION_ID + " INTEGER, "
                    + BusRouteStationColumns.INDEX + " INTEGER, "
                    + "FOREIGN KEY" + " (" + BusRouteStationColumns.ROUTE_ID + ") " 
                    + "REFERENCES " + TABLE_BUS_ROUTE + "(" + BusRouteColumns._ID + ") ON DELETE CASCADE, "
                    + "FOREIGN KEY" + " (" + BusRouteStationColumns.STATION_ID + ") "
                    + "REFERENCES " + TABLE_BUS_STATION + "(" + BusStationColumns._ID + ") ON DELETE CASCADE, "
                    + "UNIQUE (" + BusRouteStationColumns.ROUTE_ID + ", "
                    + BusRouteStationColumns.STATION_ID + " )"+ " )";
            
            Utils.debug(TAG, "onCreated sql: " + createTableSql1);
            db.execSQL(createTableSql1);
            Utils.debug(TAG, "onCreated sql: " + createTableSql2);
            db.execSQL(createTableSql2);
            Utils.debug(TAG, "onCreated sql: " + createTableSql3);
            db.execSQL(createTableSql3);
            Utils.debug(TAG, "onCreated sql: " + createTableSql4);
            db.execSQL(createTableSql4);
            Utils.debug(TAG, "onCreated sql: " + createTableSql5);
            db.execSQL(createTableSql5);
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
