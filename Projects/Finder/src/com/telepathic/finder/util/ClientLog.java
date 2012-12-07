/**
 * Copyright © 2012 Myriad Group AG. All Rights Reserved.
 */

package com.telepathic.finder.util;

import android.util.Log;

public class ClientLog {

    public static final boolean DEBUG = true;

    public static void debug(String tag, String info) {
        Log.d(tag, info);
    }

    public static void error(String tag, String errorMsg) {
        Log.e(tag, errorMsg);
    }
}
