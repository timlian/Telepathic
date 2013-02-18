package com.telepathic.finder.sdk.traffic.entity;

public abstract class KXBusLine {
    /**
     * The line number
     */
    private String mLineNumber;
    
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
    }
    
    public abstract void setRoute(Direction direction, KXBusRoute route);
    
    public abstract KXBusRoute getRoute(Direction direction);
    
    public String getLineNumber() {
        return mLineNumber;
    }
    
    public void setLineNumber(String lineNumber) {
        mLineNumber = lineNumber;
    }
}
