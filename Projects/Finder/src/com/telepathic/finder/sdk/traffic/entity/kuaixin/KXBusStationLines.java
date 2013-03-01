package com.telepathic.finder.sdk.traffic.entity.kuaixin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusLine.Direction;

public class KXBusStationLines {
    /**
     * The station name
     */
    private String mName;
    /**
     * The gps number of the station
     */
    private String mGpsNumber;
    /**
     * The bus lines
     */
    private List<KXBusLine> mLines = new ArrayList<KXBusLine>();
    /**
     * The line route map
     */
    private HashMap<String, Direction> mDirectionMap = new HashMap<String, Direction>();

    public String getName() {
        return mName;
    }

    public void setName(String stationName) {
        mName = stationName;
    }

    public String getGpsNumber() {
        return mGpsNumber;
    }

    public void setGpsNumber(String gpsNumber) {
        mGpsNumber = gpsNumber;
    }

    public List<KXBusLine> getAllBusLines() {
        return Collections.unmodifiableList(mLines);
    }

    public KXBusLine getBusLine(String lineNumber) {
        KXBusLine result = null;
        for (KXBusLine line : mLines) {
            if (line.getLineNumber().equals(lineNumber)) {
                result = line;
            }
        }
        return result;
    }

    public void addBusLine(KXBusLine line) {
        if (!mLines.contains(line)) {
            mLines.add(line);
        }
    }

    public void addLineDirection(String lineNumber, Direction direction) {
        mDirectionMap.put(lineNumber, direction);
    }

    public HashMap<String, Direction> getLineRouteMap() {
        return mDirectionMap;
    }

    public Direction getLineDirection(String lineNumber) {
        return mDirectionMap.get(lineNumber);
    }

    public boolean contains(String lineNumber, Direction direction) {
        boolean result = false;
        if (direction != null && direction == mDirectionMap.get(lineNumber)) {
            result = true;
        }
        return result;
    }

}
