package com.telepathic.finder.sdk.traffic.entity;

public class KXNormalBusLine extends KXBusLine {
    private KXBusRoute mUpRoute;
    private KXBusRoute mDownRoute;
    
    private void setUpRoute(KXBusRoute upRoute) {
        mUpRoute = upRoute;
    }
    
    private void setDownRoute(KXBusRoute downRoute) {
        mDownRoute = downRoute;
    }
    
    public KXBusRoute getUpRoute() {
        return mUpRoute;
    }
    
    public KXBusRoute getDownRoute() {
        return mDownRoute;
    }

    @Override
    public void setRoute(Direction direction, KXBusRoute route) {
        switch (direction) {
        case UP:
            setUpRoute(route);
            break;
        case DOWN:
            setDownRoute(route);
            break;
        default:
            break;
        }
    }

    @Override
    public KXBusRoute getRoute(Direction direction) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
