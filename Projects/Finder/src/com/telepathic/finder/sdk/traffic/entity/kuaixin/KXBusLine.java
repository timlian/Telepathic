package com.telepathic.finder.sdk.traffic.entity.kuaixin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KXBusLine {
    /**
     * The line number
     */
    private String mLineNumber;
    /**
     * The bus routes
     */
    private List<KXBusRoute> mRoutes = new ArrayList<KXBusRoute>();

    public enum Direction {
        UP("上行"),
        DOWN("下行"),
        CIRCLE("环行");
        private final String mLabel;

        private Direction(String label) {
            mLabel = label;
        }

        public static Direction fromString(String label) {
            for(Direction direction : Direction.values()) {
                if (direction.mLabel.equals(label)) {
                    return direction;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return mLabel;
        }
    }

    public KXBusLine(String lineNumber) {
        mLineNumber = lineNumber;
    }

    public String getLineNumber() {
        return mLineNumber;
    }

    public List<KXBusRoute> getAllRoutes() {
        return Collections.unmodifiableList(mRoutes);
    }

    public KXBusRoute getRoute(Direction direction) {
        KXBusRoute retRoute = null;
        for(KXBusRoute route : mRoutes) {
            if (route.getDirection().equals(direction)) {
                retRoute = route;
                break;
            }
        }
        return retRoute;
    }

    public void addRoute(KXBusRoute route) {
        mRoutes.add(route);
    }
}
