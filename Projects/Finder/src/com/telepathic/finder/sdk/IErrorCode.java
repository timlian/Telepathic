package com.telepathic.finder.sdk;

import android.R.integer;

public interface IErrorCode {

    // Bai du error code definition
    public static int ERROR_LOCATION_FAILED = 200;

    public static int ERROR_NETWORK_CONNECT = 2;

    public static int ERROR_NETWORK_DATA = 3;

    public static int ERROR_PERMISSION_DENIED = 300;

    public static int ERROR_RESULT_NOT_FOUND = 100;

    public static int ERROR_ROUTE_ADDR = 4;

    // Kuai xin error code definition
    public static int ERROR_GPS_NUMBER_NOT_PRESENT = 700;
    
    public static int ERROR_LINE_NUMBER_NOT_PRESENT = 400;

    // Generic error code definition
    public static int ERROR_UNKNOWN = -1;

    public static int ERROR_NO_NETWORK = -2;

    public static int ERROR_NO_VALID_DATA = -3;
    
    public static int ERROR_TIME_OUT = -4;

}
