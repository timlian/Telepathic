package com.telepathic.finder.sdk;

import java.util.ArrayList;

public interface ITrafficService {
    /**
     * Search the bus line route detail information according to the specified line number.
     *
     * @param city   The name of the city, which the bus route belongs to.
     * @param lineNumber The unique identity of the bus route.
     */
    void searchBusLine(String city, String lineNumber);

    /**
     * Search the bus line route detail information according to the specified line number.
     *
     * @param city   The name of the city, which the bus route belongs to.
     * @param lineNumber The unique identity of the bus route.
     */
    void searchBusRoute(String city, String routeUid);


    /**
     * Retrieve the distance between the bus's current position and the specified position in station units.
     *
     * @param route
     */
    void getBusLocation(String lineNumber, ArrayList<String> route);

    /**
     * Retrieve the consumer records.
     *
     * @param cardId The unique identity of the bus card.
     * @param count  The max number of records.
     */
    void getBusCardRecords(String cardNumber, int count);

    /**
     * Retrieve all the bus line numbers related to the specified bus station.
     *
     * @param gpsNumber The gps number of bus station.
     */
    void getBusStationLines(String gpsNumber);
    /**
     * Shut down the traffic service.
     */
    void shutDown();
}
