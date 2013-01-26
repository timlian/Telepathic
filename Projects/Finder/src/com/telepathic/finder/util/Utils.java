package com.telepathic.finder.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class Utils {
	private static final String TAG = Utils.class.getSimpleName();
	
    private static final boolean DEBUG = true;

    private static final String BUS_LINE_NUM_EXPRESSION = "\\d{1,3}([aAbBcCdD])?";
    
    private static final String BUS_CARD_NUM_EXPRESSION = "\\d{8}";

    private static final String START_WITH_ZERO_EXPRESSION = "^0+";

    public static final String CARD_ID_CACHE = "card_id_cache";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private Utils() {}

    public static void hideSoftKeyboard(Context c, EditText v) {
        InputMethodManager imm = (InputMethodManager) c
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    public static void showSoftKeyboard(Context c,EditText v) {
        InputMethodManager imm = (InputMethodManager) c
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(v, InputMethodManager.SHOW_FORCED);
        }
    }

    public static boolean isValidBusLineNumber(String number) {
        boolean ret = false;
        if (number != null) {
            final String busLineNumber = number.trim();
            if (number.length() != 0) {
                ret = busLineNumber.matches(BUS_LINE_NUM_EXPRESSION);
            }
        }
        return ret;
    }

    public static boolean isValidBusCardNumber(String number) {
        boolean ret = false;
        if (number != null) {
            final String busCardNumber = number.trim();
            if (number.length() != 0) {
                ret = busCardNumber.matches(BUS_CARD_NUM_EXPRESSION);
            }
        }
        return ret;
    }
    public static ArrayList<String> parseBusLineNumber(String text) {
        Pattern p = Pattern.compile(BUS_LINE_NUM_EXPRESSION);
        Matcher m = p.matcher(text);
        ArrayList<String> busLineNumbers = new ArrayList<String>();
        while(m.find()) {
            busLineNumbers.add(m.group());
        }
        return busLineNumbers;
    }

    public static String formatRecognizeData(String source) {
        String temp = source.trim().toUpperCase().replace(" ", "");
        temp = temp.replaceFirst(START_WITH_ZERO_EXPRESSION, "");
        if (temp.length() > 4) {
            temp = temp.substring(0, 4);
        }
        if (temp.endsWith("·")) {
            temp = temp.replace("·", "");
        }
        Pattern p = Pattern.compile(BUS_LINE_NUM_EXPRESSION);
        Matcher m = p.matcher(temp);
        if (m.matches()) {
            return temp;
        }
        return null;
    }

    public static ArrayList<String> removeDuplicateWithOrder(ArrayList<String> list) {
        HashSet<String> hashSet = new HashSet<String>();
        List<String> newlist = new ArrayList<String>();

        for (Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
            String element = iterator.next();
            if (hashSet.add(element)) {
                newlist.add(element);
            }
        }
        list.clear();
        list.addAll(newlist);
        return list;
    }

    public static Date parseDate(String text) {
        Date result = null;
        if (text == null || text.length() == 0) {
            throw new IllegalArgumentException("Date text is empty.");
        }
        try {
            result = DATE_FORMAT.parse(text);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String formatDate(Date date) {
        return DATE_FORMAT.format(date);
    }

    /**
     * Copy the application database files to external storage
     */
	public static void copyAppDatabaseFiles(String packageName) {
		try {
			File dbDirectory = new File("/data/data/" + packageName + "/databases/");
			if (dbDirectory.exists()) {
				File[] dbFiles = dbDirectory.listFiles();
				for (int i = 0; i < dbFiles.length; i++) {
					copyFileToExternalStorage(dbFiles[i]);
				}
			}
		} catch (IOException e) {
			Utils.error(TAG, e.getLocalizedMessage());
		}
	}

    /**
     * Copy the application normal files to external storage
     */
    public static void copyAppPreferenceFiles(String packageName) throws IOException {
        File prefsDirectory = new File("/data/data/" + packageName + "/shared_prefs/");
        if (prefsDirectory.exists()) {
            File[] prefFiles = prefsDirectory.listFiles();
            for(int i = 0; i < prefFiles.length; i++) {
                copyFileToExternalStorage(prefFiles[i]);
            }
        }
    }

    private static void copyFileToExternalStorage(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        String outFileName = Environment.getExternalStorageDirectory() + "/" + file.getName();
        OutputStream output = new FileOutputStream(outFileName);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = fis.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }
        output.flush();
        output.close();
        fis.close();
    }

    /**
     * Print the content of the cursor
     *
     * @param cursor, The cursor, which content needs to be printed
     * @param logTag, The log tag
     */
    public static void printCursorContent(Cursor cursor, String logTag) {
        if (cursor == null) {
            Log.d(logTag, "Cursor is NULL!");
            return ;
        }
        final int columnSpace = 2;
        ArrayList<Integer> columnWidth = new ArrayList<Integer>();
        for (int columnIndex = 0; columnIndex < cursor.getColumnCount(); columnIndex++) {
            String value = cursor.getColumnName(columnIndex);
            int maxWidth = value.length();
            if (cursor.moveToFirst()) {
                do {
                    try {
                        value = cursor.getString(columnIndex);
                    } catch (Exception e) {
                        value = "BLOB";
                        Log.d(logTag, "Get value from " + cursor.getColumnName(columnIndex) + " failed. Caused by " + e.getLocalizedMessage());
                    }
                    if (!TextUtils.isEmpty(value) && value.length() > maxWidth) {
                        maxWidth = value.length();
                    }
                } while (cursor.moveToNext());
            }
            columnWidth.add(maxWidth + columnSpace);
        }
        ArrayList<ArrayList<String>> tableContent = new ArrayList<ArrayList<String>>();
        for (int columnIndex = 0; columnIndex < cursor.getColumnCount(); columnIndex++) {
            ArrayList<String> columnContent = new ArrayList<String>();
            String value = cursor.getColumnName(columnIndex);
            columnContent.add(appendColumnSpaces(value, columnWidth.get(columnIndex)));
            if (cursor.moveToFirst()) {
                do {
                    try {
                        value = cursor.getString(columnIndex);
                    } catch (Exception e) {
                        value = "BLOB";
                    }
                    columnContent.add(appendColumnSpaces(value, columnWidth.get(columnIndex)));
                } while (cursor.moveToNext());
            }
            tableContent.add(columnContent);
        }
        // Including the header
        int maxRowIndex = cursor.getCount() + 1;
        for(int rowIndex = 0; rowIndex < maxRowIndex; rowIndex++) {
            StringBuilder rowValues = new StringBuilder();
            for (int columnIndex = 0; columnIndex < cursor.getColumnCount(); columnIndex++) {
                ArrayList<String> columnValues = tableContent.get(columnIndex);
                rowValues.append(columnValues.get(rowIndex));
            }
            Log.d(logTag, rowValues.toString());
        }
        // set the cursor back the first item
        cursor.moveToFirst();
    }

    public static void printObjectId(Object obj, String logTag) {
        Log.d(logTag, "Object ID: " + System.identityHashCode(obj));
    }

    private static String appendColumnSpaces(String value, int columnWidth) {
        StringBuilder builder =  new StringBuilder();
        int spaceCount;
        if (value == null) {
            builder.append("null");
            spaceCount = columnWidth - 4;
        } else {
            builder.append(value);
            spaceCount = columnWidth - value.length();
        }
        for (int i = 0; i < spaceCount; i++) {
            builder.append(" ");
        }
        return builder.toString();
    }

    public static ArrayList<String> getCachedCards(Context c) {
        SharedPreferences sharedPreferences = c.getSharedPreferences(CARD_ID_CACHE, Context.MODE_PRIVATE);

        Map<String, ?> map = sharedPreferences.getAll();
        ArrayList<String> list = new ArrayList<String>();
        if (map.keySet() != null) {
            list.addAll(map.keySet());
        }
        return list;
    }

    public static void addCachedCards(Context c, String cardId){
        SharedPreferences preferences = c.getSharedPreferences(Utils.CARD_ID_CACHE, Context.MODE_PRIVATE);
        preferences.edit().putString(cardId, cardId).commit();
    }

    public static void debug(String tag, String info) {
        if (DEBUG) {
            Log.i(tag, info);
        }
    }

    public static void error(String tag, String errorMsg) {
        if (DEBUG) {
            Log.e(tag, errorMsg);
        }
    }
}
