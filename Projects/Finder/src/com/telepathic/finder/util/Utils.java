
package com.telepathic.finder.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.telepathic.finder.R;
import com.telepathic.finder.sdk.IErrorCode;

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    private static final boolean DEBUG = false;

    private static final String BUS_LINE_NUM_EXPRESSION = "\\d{1,3}([a-zA-Z])?|([k|K])\\d{1,3}";

    private static final String BUS_CARD_NUM_EXPRESSION = "\\d{8}";

    private static final String BUS_STATION_GPS_NUMBER = "\\d{5}";

    private static final String START_WITH_ZERO_EXPRESSION = "^0+";

    private static final String STATION_NAME_SUFFIX = "站";

    public static final String CARD_ID_CACHE = "card_id_cache";

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final SimpleDateFormat HIGH_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSSS");

    private static final String PACKAGENAME_SOGOU_MI_INPUTMETHOD = "com.sogou.inputmethod.mi";

    private static final String PACKAGENAME_SAMSUNG_INPUTMETHOD = "com.samsung.inputmethod";

    private static final String PACKAGENAME_SNDA_INPUTMETHOD = "com.snda.input";

    private static final String PACKAGENAME_GOOGLE_PINYIN_INPUTMETHOD = "com.google.android.inputmethod.pinyin";

    private Utils() {
    }

    /**
     * Find if the device has installed the input method that can not input character or start search.
     * @param context
     * @return
     */
    public static boolean hasSpecialInputMethod(Context context) {
        if (context != null) {
            List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
            for(PackageInfo info : packages) {
                if(info.packageName.equalsIgnoreCase(PACKAGENAME_SOGOU_MI_INPUTMETHOD)
                        || info.packageName.equalsIgnoreCase(PACKAGENAME_SAMSUNG_INPUTMETHOD)
                        || info.packageName.equalsIgnoreCase(PACKAGENAME_SNDA_INPUTMETHOD)
                        || info.packageName.equalsIgnoreCase(PACKAGENAME_GOOGLE_PINYIN_INPUTMETHOD)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void hideSoftKeyboard(Context c, View v) {
        InputMethodManager imm = (InputMethodManager)c
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    public static void showSoftKeyboard(Context c, EditText v) {
        InputMethodManager imm = (InputMethodManager)c
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

    public static boolean isValidGpsNumber(String number) {
        boolean ret = false;
        if (number != null) {
            final String gpsNumber = number.trim();
            if (number.length() != 0) {
                ret = gpsNumber.matches(BUS_STATION_GPS_NUMBER);
            }
        }
        return ret;
    }

    public static ArrayList<String> parseBusLineNumber(String text) {
        Pattern p = Pattern.compile(BUS_LINE_NUM_EXPRESSION);
        Matcher m = p.matcher(text);
        ArrayList<String> busLineNumbers = new ArrayList<String>();
        while (m.find()) {
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

        if (null != list) {
            for (Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
                String element = iterator.next();
                if (hashSet.add(element)) {
                    newlist.add(element);
                }
            }
            list.clear();
            list.addAll(newlist);
        }
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

    public static String formatTime(Date date) {
        return HIGH_DATE_FORMAT.format(date);
    }


    public static String completeStationName(String station) {
        String stationName;
        if (station.endsWith(STATION_NAME_SUFFIX)) {
            stationName = station;
        } else {
            stationName = station + STATION_NAME_SUFFIX;
        }
        return stationName;
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
            for (int i = 0; i < prefFiles.length; i++) {
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
    public static void printCursorContent(String logTag, Cursor cursor) {
        if (cursor == null) {
            Log.d(logTag, "Cursor is NULL!");
            return;
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
                        Log.d(logTag, "Get value from " + cursor.getColumnName(columnIndex)
                                + " failed. Caused by " + e.getLocalizedMessage());
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
        for (int rowIndex = 0; rowIndex < maxRowIndex; rowIndex++) {
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
        StringBuilder builder = new StringBuilder();
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

    public static boolean isValid(Cursor cursor) {
        return (cursor != null && cursor.getCount() > 0) ? true : false;
    }

    /**
     * Translate the dp(density-independent pixels) to px(pixels), according to
     * the resolution of device;
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dpValue * scale + 0.5f);
    }

    /**
     * Translate the px(pixels) to dp(density-independent pixels), according to
     * the resolution of device;
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(pxValue / scale + 0.5f);
    }

    public static boolean isSameMonth(Date date) {
        boolean result = false;
        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar1.setTimeInMillis(System.currentTimeMillis());
        calendar2.setTime(date);
        if (calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
                && calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH)) {
            result = true;
        }
        return result;
    }

    public static boolean hasActiveNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null) {
            return info.isAvailable();
        }
        return false;
    }

    public static String getPhoneNumber(Context context) {
        String phoneNumber = "";
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        phoneNumber = tm.getLine1Number();
        return phoneNumber;
    }

    public static String getErrorMessage(Resources res, int errorCode, String errorText) {
        String retMessage = null;
        switch (errorCode) {
            case IErrorCode.ERROR_NO_NETWORK:
                retMessage = res.getString(R.string.error_no_network);
                break;
            case IErrorCode.ERROR_UNKNOWN:
                retMessage = res.getString(R.string.error_unknown);
                break;
            case IErrorCode.ERROR_NO_VALID_DATA:
                retMessage = res.getString(R.string.error_no_data);
                break;
            case IErrorCode.ERROR_TIME_OUT:
                retMessage = res.getString(R.string.error_time_out);
                break;
            case IErrorCode.ERROR_LOCATION_FAILED:
                retMessage = res.getString(R.string.error_location_falied);
                break;
            case IErrorCode.ERROR_NETWORK_CONNECT:
                retMessage = res.getString(R.string.error_network_connect);
                break;
            case IErrorCode.ERROR_NETWORK_DATA:
                retMessage = res.getString(R.string.error_network_data);
                break;
            case IErrorCode.ERROR_PERMISSION_DENIED:
                retMessage = res.getString(R.string.error_permission_denied);
                break;
            case IErrorCode.ERROR_RESULT_NOT_FOUND:
                retMessage = res.getString(R.string.error_result_not_found);
                break;
            case IErrorCode.ERROR_ROUTE_ADDR:
                retMessage = res.getString(R.string.error_route_addr);
                break;
            case IErrorCode.ERROR_GPS_NUMBER_NOT_PRESENT:
                retMessage = res.getString(R.string.error_gps_number_not_present);
                break;
            case IErrorCode.ERROR_STATION_NAME_NOT_PRESENT:
                retMessage = res.getString(R.string.error_station_name_not_present);
                break;
            default:
                Logger.logTrace(String.valueOf(errorCode) + ": " + errorText);
                retMessage = errorText;
                break;
        }
        return retMessage;
    }

}
