package com.telepathic.finder.sdk.traffic.task;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import org.ksoap2.serialization.SoapObject;

import android.content.ContentValues;
import android.content.Context;

import com.telepathic.finder.sdk.traffic.entity.BusLine;
import com.telepathic.finder.sdk.traffic.entity.BusLine.Direction;
import com.telepathic.finder.sdk.traffic.entity.BusStation;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData;

public class GetBusLineTask  implements Callable<BusLine> {
	private static final String TAG = GetBusLineTask.class.getSimpleName();
	
    private BusLine mResult;
    private Context mContext;
    private String mLineNumber;
    
    public GetBusLineTask(Context context, String lineNumber) {
        mContext = context;
        mLineNumber = lineNumber;
    }
    
    @Override
	public BusLine call() throws Exception {
		NetworkManager.execute(new GetBusLineRequest());
		store(mResult);
		return mResult;
	}
  
    private void store(BusLine busLine) {
    	for(Direction direction : busLine.getRouteMap().keySet()) {
    		ContentValues values = new ContentValues();
    		values.put(ITrafficData.BusRoute.LINE_NUMBER, busLine.getLineNumber());
    		values.put(ITrafficData.BusRoute.DIRECTION, direction.toString());
    		values.put(ITrafficData.BusRoute.START_TIME, busLine.getStartTime());
    		values.put(ITrafficData.BusRoute.END_TIME, busLine.getEndTime());
    		values.put(ITrafficData.BusRoute.FIRST_STATION, busLine.getFirstStation(direction));
    		values.put(ITrafficData.BusRoute.LAST_STATION, busLine.getLastStation(direction));
    		values.put(ITrafficData.BusRoute.STATIONS, busLine.getRouteStations(direction));
    		mContext.getContentResolver().insert(ITrafficData.BusRoute.CONTENT_URI, values);
    	}
    }
    
    private class GetBusLineRequest extends RPCBaseRequest {
    	private static final String METHOD_NAME = "getBusLineRoute";
        // parameter keys
        private static final String KEY_BUS_LINE = "busLine";
        // response keys 
        private static final String KEY_LINE_NAME = "lineName";
        private static final String KEY_DEPARTURE_TIME ="departureTime";
        private static final String KEY_CLOSE_OFF_TIME = "closeOffTime";
        private static final String KEY_DIRECTION = "type";
        private static final String KEY_STATIONS = "stations";
        
		GetBusLineRequest() {
			super(METHOD_NAME);
			addParameter(KEY_BUS_LINE, mLineNumber);
		}

		@Override
		void handleError(int errorCode, String errorMessage) {
			// TODO Auto-generated method stub
		}

		/*
	     * Line route response data entry example:
	     *
	     * {lineName=111; departureTime=06:00; closeOffTime=22:00; type=锟斤拷锟斤拷; stations=锟斤拷锟斤拷站锟桔合斤拷通锟斤拷纽站,盛锟斤拷一路锟斤拷站,锟斤拷锟斤拷锟斤拷锟秸�桐锟斤拷锟斤拷小锟斤拷站,锟斤拷锟斤拷路站,锟较撅拷锟斤拷路站,锟较撅拷锟斤拷路站,锟较撅拷锟斤拷路锟斤拷站,锟较撅拷锟斤拷路锟斤拷业路锟斤拷站,锟斤拷业路锟斤拷锟斤拷路锟斤拷锟斤拷站,锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷站,锟斤拷锟斤拷路锟斤拷锟侥讹拷站,锟斤拷锟斤拷楼站,锟斤拷锟斤拷路锟斤拷一锟斤拷锟斤拷站,锟斤拷锟斤拷锟斤拷锟秸�锟斤拷锟斤拷路锟斤拷锟斤拷路锟斤拷站,锟斤拷水锟斤拷站,锟斤拷锟斤拷路锟斤拷一锟斤拷站,锟斤拷锟斤拷路锟解华锟斤拷锟斤拷锟秸�锟铰筹拷锟斤拷路锟斤拷站,锟斤拷锟斤拷路锟斤拷站,锟斤拷锟斤拷路锟斤拷站,锟斤拷锟斤拷锟斤拷锟斤拷站,锟斤拷锟斤拷锟斤拷锟斤拷站,锟斤拷锟斤拷小锟斤拷站,锟斤拷路同锟斤拷路锟斤拷站,锟斤拷路站,锟斤拷路锟斤拷站,锟斤拷路锟斤拷犀锟斤拷锟斤拷锟脚讹拷站,锟斤拷锟斤拷路站,锟竭家达拷站,锟斤拷锟脚达拷站,锟竭硷拷小锟斤拷站; stationAliases= , , , , , , , , , , , , , , , , , , , , , , , , , , , , , , , , ; code=200; msg=锟缴癸拷; }
	     *
	     */
	    @Override
	    void handleResponse(SoapObject newDataSet) {
	        SoapObject dataEntry = null;
			final int count = newDataSet.getPropertyCount();
			mResult = new BusLine();
			for (int idx = 0; idx < count; idx++) {
				dataEntry = (SoapObject) newDataSet.getProperty(idx);
				mResult.setLineNumber(dataEntry.getPrimitivePropertyAsString(KEY_LINE_NAME));
				mResult.setStartTime(dataEntry.getPrimitivePropertyAsString(KEY_DEPARTURE_TIME));
				mResult.setEndTime(dataEntry.getPrimitivePropertyAsString(KEY_CLOSE_OFF_TIME));
				String[] stationNames = dataEntry.getPrimitivePropertyAsString(KEY_STATIONS).split(",");
				ArrayList<BusStation> route = new ArrayList<BusStation>();
				for(String name : stationNames) {
					route.add(new BusStation(name));
				}
				String direction = dataEntry.getPrimitivePropertyAsString(KEY_DIRECTION);
				mResult.addRoute(Direction.fromString(direction), route);
			}
	    }
    }
    
}
