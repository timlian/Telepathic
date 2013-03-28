package com.telepathic.finder.sdk;

import java.util.ArrayList;

public interface ITrafficService {
    /**
     * Search the bus line information.
     *
     * @param city   The name of the city, which the bus line belongs to.
     * @param lineNumber The unique identity of the bus line.
     * @param listener The listener will be called, when the search finished.
     */
    void searchBusLine(String city, String lineNumber, ICompletionListener listener);
    
    /**
     * Query bus line number from traffic data provider.
     * 
     * @param keyword The selection argument.
     * @param listener The listener will be called, when the query finished.
     */
    void queryBDLineNumber(String keyword, IQueryListener listener);

    /**
     * Search the bus route information.
     *
     * @param city   The name of the city, which the bus route belongs to.
     * @param routeUid The unique identity of the bus route.
     * @param listener The listener will be called, when the search finished.
     */
    void searchBusRoute(String city, String routeUid, ICompletionListener listener);

    /**
     * Retrieve the bus location.
     *
     * @param lineNumber The unique identity of the bus line.
     * @param route 
     */
    void getBusLocation(String lineNumber, ArrayList<String> route, ILocationListener listener);

    /**
     * Retrieve the consumer records.
     *
     * @param cardId The unique identity of the bus card.
     * @param count  The max number of records.
     * @param listener 
     */
    void getBusCardRecords(String cardNumber, int count, ICompletionListener listener);
    
    /**
     * Query bus card number from traffic data provider.
     * 
     * @param keyword The selection argument.
     * @param listener The listener will be called, when the query finished.
     */
    void queryCardNumber(String keyword, IQueryListener listener);

    /**
     * Retrieve all the bus line numbers related to the specified bus station.
     *
     * @param gpsNumber The gps number of bus station.
     * @param listener The listener will be called, when the operation finished.
     */
    void getBusStationLines(String stationName, ICompletionListener listener);
    
    /**
     * Translate the gps number to station name.
     * 
     * @param gpsNumber The bus station's GPS number.
     * @param listener The listener will be called, when the translation finished.
     */
    void translateToStationName(String gpsNumber, ICompletionListener listener);
    
    /**
     * Query station name.
     * 
     * @param keyword The selection argument.
     * @param listener The listener will be called, when the query finished.
     */
    void queryStationName(String keyword, ICompletionListener listener);
    
    /**
     * Query station name.
     * 
     * @param keywork The selection argument.
     * @param listener The listener will be called, when the query finished.
     */
    void queryGpsNumber(String keyword, IQueryListener listener);
    
    /**
     * Retrieve the bus transfer routes.
     * 
     * @param source The source station name.
     * @param detination The destination name.
     * @param listener The listener will be called, when the operation finished.
     */
    void getBusTransferRoute(String source, String detination, ICompletionListener listener);
    
    /**
     * Retrieve the bus line information.
     * 
     * @param lineNumber The bus line number.
     * @param listener listener The listener will be called, when the operation finished.
     */
    void getBusLine(String lineNumber, ICompletionListener listener);
    
    /**
     * Query bus line number from traffic data provider.
     * 
     * @param keyword The selection argument.
     * @param listener The listener will be called, when the query finished.
     */
    void queryKXLineNumber(String keyword, IQueryListener listener);
    
    /**
     * Shut down the traffic service.
     */
    void shutDown();
    
}
