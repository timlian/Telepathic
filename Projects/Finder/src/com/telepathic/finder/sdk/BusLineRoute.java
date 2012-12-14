package com.telepathic.finder.sdk;

import java.util.ArrayList;
import org.ksoap2.serialization.SoapObject;
import com.telepathic.finder.sdk.BusEntityKeys.BusLineRouteKeys;

public class BusLineRoute extends BusEntity implements BusLineRouteKeys {
    
    public BusLineRoute(SoapObject object) {
        parseRoute(object);
    }
    
    @SuppressWarnings("unchecked")
    public ArrayList<BusStation> getStations() {
        return (ArrayList<BusStation>) getValue(KEY_STATIONS);
    }
    
    public String getLineName() {
        return getStringValue(KEY_LINE_NAME);
    }
    
    public String getDepartureTime() {
        return getStringValue(KEY_DEPARTURE_TIME);
    }
    
    public String getCloseoffTime() {
        return getStringValue(KEY_CLOSE_OFF_TIME);
    }
    
    public String getType() {
        return getStringValue(KEY_TYPE);
    }
    
    private void parseRoute(SoapObject object) {
        setValue(KEY_LINE_NAME, object.getPrimitivePropertyAsString(KEY_LINE_NAME));
        setValue(KEY_DEPARTURE_TIME, object.getPrimitivePropertyAsString(KEY_DEPARTURE_TIME));
        setValue(KEY_CLOSE_OFF_TIME, object.getPrimitivePropertyAsString(KEY_CLOSE_OFF_TIME));
        setValue(KEY_TYPE, object.getPrimitivePropertyAsString(KEY_TYPE));
        String[] stationNames = object.getPrimitivePropertyAsString(KEY_STATIONS).split(",");
        String[] stationAliases = object.getPrimitivePropertyAsString(KEY_STATIONS_ALIASES).split(",");
        ArrayList<BusStation> stations = new ArrayList<BusStation>();
        for(int i = 0; i < stationNames.length; i++) {
            BusStation station = new BusStation();
            station.setName(stationNames[i]);
            station.setAlias(stationAliases[i]);
            station.setIndex(i);
            stations.add(station);
        }
        setValue(KEY_STATIONS, stations);
    }
}
