package com.telepathic.finder.sdk;

import java.util.Hashtable;

abstract class BusEntity {
    private Hashtable<String, Object> contents = new Hashtable<String, Object>();

    protected Object getValue(String key) {
        return contents.get(key);
    }

    protected String getStringValue(String key) {
        return (String) contents.get(key);
    }

    protected void setValue(String key, Object value) {
        contents.put(key, value);
    }
}
