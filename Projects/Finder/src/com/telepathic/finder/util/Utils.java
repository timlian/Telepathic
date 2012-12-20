package com.telepathic.finder.util;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class Utils {

    private static final String BUS_LINE_NUM_ECPRESSION = "\\d{1,3}([aAbB])?";
    
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
    
    public static ArrayList<String> getBusLineNumber(String text) {
        Pattern p = Pattern.compile(BUS_LINE_NUM_ECPRESSION); 
        Matcher m = p.matcher(text); 
        ArrayList<String> busLineNumbers = new ArrayList<String>();
        while(m.find()) { 
            busLineNumbers.add(m.group());
        }
        return busLineNumbers;
    }
}
