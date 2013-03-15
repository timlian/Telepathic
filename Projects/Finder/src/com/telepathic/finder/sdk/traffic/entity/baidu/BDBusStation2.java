package com.telepathic.finder.sdk.traffic.entity.baidu;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.baidu.mapapi.search.MKRoute;
import com.baidu.mapapi.search.MKStep;
import com.baidu.platform.comapi.basestruct.GeoPoint;

public class BDBusStation2 extends MKStep {
    /**
     * The name of the bus station
     */
    private String mName;
    /**
     * The latitude of the bus station's position
     */
    private String mLatitude;
    /**
     * The longitude of the bus station's position
     */
    private String mLongitude;
    
    public BDBusStation2() {
    	
    }
    
	@Override
	public String getContent() {
		return mName;
	}
	
	@Override
	public GeoPoint getPoint() {
		return new GeoPoint(Integer.parseInt(mLatitude), Integer.parseInt(mLongitude));
	}
	
    public void setName(String name) {
    	mName = name;
    }
    
    public void setLatitude(String latitude) {
    	mLatitude = latitude;
    }
    
    public void setLongitude(String longitude) {
    	mLongitude = longitude;
    }
    
    public String getName() {
        return mName;
    }

    public String getLatitude() {
        return mLatitude;
    }

    public String getLongitude() {
        return mLongitude;
    }
    
    public MKStep toMkStep() {
    	MKStep step = new MKStep();
    	Class<?> stepClass = MKStep.class;
		try {
			Method setPoint   = stepClass.getDeclaredMethod("a", GeoPoint.class);
			Method setContent = stepClass.getDeclaredMethod("a", String.class);
			setPoint.setAccessible(true);
			setContent.setAccessible(true);
			setPoint.invoke(step, new GeoPoint(Integer.parseInt(mLatitude), Integer.parseInt(mLongitude)));
			setContent.invoke(step, mName);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
    	return step;
    }
}
