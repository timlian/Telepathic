package com.telepathic.finder.sdk;

import com.telepathic.finder.sdk.traffic.entity.BusRoute;
import com.telepathic.finder.sdk.traffic.store.TrafficeStore;

public interface ITrafficService {
    /**
     * Search the bus line according to the specified bus line number.
     * 
     * @param city  The name of the city, which the bus line belongs to.
     * @param lineNumber The bus line number.
     */
    public void searchBusLine(String city, String lineNumber);

    /**
     * Search the bus line route detail information according to the specified route UID.
     * 
     * @param city   The name of the city, which the bus route belongs to.
     * @param routeUid The unique identity of the bus route.
     */
    public void searchBusRoute(String city, String routeUid);

    /**
     * Retrieve all the bus line numbers related to the specified bus station.
     * 
     * @param stationName The bus station name.
     */
    public void getBusStationLines(String stationName);

    /**
     * Retrieve the specified bus line route information.
     *
     * @param busLine The bus line number.
     * @return
     */
    // public void getBusLineRoute(String LineNumber, BusLineListener listener);

    /**
     * Retrieve the distance between the bus's current position and the specified position in station units.
     *
     * @param lineNumber  The bus line number.
     * @param anchorStation  The station name of the anchor position.
     * @param lastStation The last station name.
     * @return
     */
    public void getBusLocation(String lineNumber, String anchorStation, String lastStation);

    /**
     * 
     * @param route
     */
    public void getBusLocation(BusRoute route);

    /**
     * Retrieve the consumer records.
     * 
     * @param cardId The unique identity of the bus card.
     * @param count  The max number of records.
     */
    public void getConsumerRecords(String cardId, int count);

    /**
     * Retrieve the bus station name and direction, according to the specified bus line and GPS number.
     * 
     * @param lineNumber The bus line number
     * @param gpsNumber  The GPS number of the bus station.
     */
    public void translateToStation(String lineNumber, String gpsNumber);

    /**
     * Retrieve the bus station name, according to the specified GPS number.
     * 
     * @param gpsNumber The GPS number of the bus station.
     */
    public void translateToStation(String gpsNumber);

    /**
     * Get the traffice monitor
     * @return
     */
    public ITrafficMonitor getTrafficMonitor();
    /**
     * 
     * @return
     */
    public TrafficeStore getTrafficeStore();
}
