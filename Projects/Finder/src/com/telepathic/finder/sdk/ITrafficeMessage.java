package com.telepathic.finder.sdk;


public interface ITrafficeMessage {

    int GET_BUS_CARD_RECORDS_DONE = 1;

    int GET_BUS_STATION_LINES_DONE = 2;

    int GET_BUS_LINE_DONE = 3;

    int SEARCH_BUS_LINE_DONE = 4;

    int SEARCH_BUS_ROUTE_DONE = 5;

    int GET_BUS_LOCATION_UPDATED = 6;

    int GET_BUS_LOCATION_DONE = 7;

    // The error code
    int GET_BUS_CARD_RECORDS_FAILED=1001;

}
