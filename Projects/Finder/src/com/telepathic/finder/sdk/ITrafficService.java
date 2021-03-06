package com.telepathic.finder.sdk;

import com.telepathic.finder.sdk.TrafficListener.BusLocationListener;

public interface ITrafficService {

    /**
     * Get the specified bus line route information.
     *
     * @param busLine The valid bus line number.
     * @return
     */
    public void getBusLineRoute(String busLine, BusLineListener listener);

    /**
     *
     * @param lineNumber
     * @param gpsNumber
     * @param lastStation
     * @return
     */
    public void getBusLocation(String lineNumber, String gpsNumber, String lastStation, BusLocationListener listener);

    /**
     *
     * @param cardId
     * @param count
     */
    public void retrieveConsumerRecords(String cardId, int count, ConsumerRecordsListener listener);

}
