/**
 * 
 */

package com.telepathic.finder.sdk.store;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.telepathic.finder.sdk.ConsumerRecord;
import com.telepathic.finder.sdk.ConsumerRecord.ConsumerType;
import com.telepathic.finder.sdk.CountConsumerRecord;
import com.telepathic.finder.sdk.EWalletConsumerRecord;
import com.telepathic.finder.sdk.store.Store.ConsumptionColumns;
import com.telepathic.finder.util.Utils;

/**
 * @author Tim.Lian
 */
public class ConsumptionStore {

    private static final String TAG = ConsumptionStore.class.getSimpleName();

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "finder.db";
    private static final String TABLE_NAME = "consumption";
    
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

    private static ConsumptionStore defaultStore;

    public static synchronized ConsumptionStore getDefaultStore(Context context) {
        if (defaultStore == null) {
            defaultStore = new ConsumptionStore(context);
        }
        return defaultStore;
    }

    private ConsumptionStore(Context context) {
        dbHelper = new DbHelper(context);
        Log.i(TAG, "Initialized data");
    }

    public long insertRecord(ConsumerRecord record) {
    	long result = -1;
    	if (record != null) {
	    	ContentValues values = new ContentValues();
	    	values.put(ConsumptionColumns.CARD_ID, record.getCardId());
	    	values.put(ConsumptionColumns.BUS_LINE_NUMBER, record.getLineNumber());
	    	values.put(ConsumptionColumns.LICENSE_PLATE_NUMBER, record.getBusNumber());
	    	values.put(ConsumptionColumns.CONSUMPTION_TIME, Utils.formatDate(record.getConsumerTime()));
	    	values.put(ConsumptionColumns.CONSUMPTION, record.getConsumption());
	    	values.put(ConsumptionColumns.RESIDUAL, record.getResidual());
	    	values.put(ConsumptionColumns.CONSUMPTION_TYPE, record.getType().toString());
	    	result = insertOrIgnore(values);
    	}
    	return result;
    }
    
    public void deleteAllRecords() {
    	SQLiteDatabase db = dbHelper.getWritableDatabase();
    	try {
        	db.delete(TABLE_NAME, null, null);
    	} catch (Exception e) {
    		e.printStackTrace();
    	} finally {
    		db.close();
    	}
    }
    
    private long insertOrIgnore(ContentValues values) {
        long retID = -1;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            retID = db.insertWithOnConflict(TABLE_NAME, null, values,
                    SQLiteDatabase.CONFLICT_IGNORE);
        } catch (Exception e) {
            Log.e(TAG, "Insertion failed: " + e.getLocalizedMessage());
        } finally {
        	if (retID == -1) {
        		Log.e(TAG, "Insertion failed: " + values.toString());
        	}
            db.close();
        }
        return retID;
    }
    
	public ArrayList<ConsumerRecord> getConsumptionRecords(String cardId) {
		ArrayList<ConsumerRecord> records = new ArrayList<ConsumerRecord>();
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		try {
			Cursor cursor = db.query(TABLE_NAME, null,
					ConsumptionColumns.CARD_ID + " like " + "\'%" + cardId
							+ "%\'", null, null, null,
					ConsumptionColumns.CONSUMPTION_TIME + " DESC");
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
    
    class DbHelper extends SQLiteOpenHelper {
        static final String TAG = "DbHelper";

        Context context;

        public DbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
            this.context = context;
        }

        // Called only once, first time the DB is created
        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = "CREATE TABLE " + TABLE_NAME + " (" 
                    + ConsumptionColumns._ID + " integer primary key, "
                    + ConsumptionColumns.CARD_ID + " text, "
                    + ConsumptionColumns.BUS_LINE_NUMBER + " text, " 
            		+ ConsumptionColumns.LICENSE_PLATE_NUMBER + " text, " 
                    + ConsumptionColumns.CONSUMPTION_TIME + " text, "
                    + ConsumptionColumns.CONSUMPTION + " text, "
                    + ConsumptionColumns.RESIDUAL + " text, "
                    + ConsumptionColumns.CONSUMPTION_TYPE + " text, " 
                    + "UNIQUE (" + ConsumptionColumns.CARD_ID + ", " 
                    + ConsumptionColumns.CONSUMPTION_TIME + " )"+ " )";
            db.execSQL(sql);
            Log.d(TAG, "onCreated sql: " + sql);
        }

        // Called whenever newVersion != oldVersion
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists " + TABLE_NAME); // drops the old
            // database
            Log.d(TAG, "onUpgrade");
            onCreate(db);
        }
    }
}
