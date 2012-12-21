package com.telepathic.finder.sdk;

import java.util.HashMap;

public class BusRoutesStore {
    
    private HashMap<String, BusRoute> mRoutes;
    
    BusRoutesStore() {
        mRoutes = new HashMap<String, BusRoute>();
    }
    
    public void add(String uid, BusRoute route) {
        mRoutes.put(uid, route);
    }
    
    public BusRoute get(String uid) {
        return mRoutes.get(uid);
    }
}
