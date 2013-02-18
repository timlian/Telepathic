package com.telepathic.finder.sdk.traffic.entity;

public class KXCircleBusLine extends KXBusLine {
    private KXBusRoute mCircleRoute;
    
    @Override
    public void setRoute(Direction direction, KXBusRoute route) {
        if (Direction.CIRCLE == direction) {
            setCircleRoute(route);
        }
    }
    
    private void setCircleRoute(KXBusRoute circleRoute) {
        mCircleRoute = circleRoute;
    }

    @Override
    public KXBusRoute getRoute(Direction direction) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
