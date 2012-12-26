package com.telepathic.finder.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class Utils {

    private static final String BUS_LINE_NUM_EXPRESSION = "\\d{1,3}([aAbBcCdD])?";

    private static final String START_WITH_ZERO_EXPRESSION = "^0+";

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
}
