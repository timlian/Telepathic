
package com.telepathic.finder.sdk.traffic;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKSearch;
import com.telepathic.finder.sdk.ITrafficListeners.ConsumerRecordsListener;
import com.telepathic.finder.sdk.ITrafficMonitor;
import com.telepathic.finder.sdk.ITrafficService;
import com.telepathic.finder.sdk.ITrafficeMessage;
import com.telepathic.finder.sdk.traffic.entity.BusCard;
import com.telepathic.finder.sdk.traffic.entity.BusRoute;
import com.telepathic.finder.sdk.traffic.entity.BusStation;
import com.telepathic.finder.sdk.traffic.entity.ConsumerRecord;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData;
import com.telepathic.finder.sdk.traffic.store.ITrafficeStore.BusCardColumns;
import com.telepathic.finder.sdk.traffic.store.ITrafficeStore.ConsumerRecordColumns;
import com.telepathic.finder.sdk.traffic.store.TrafficeStore;
import com.telepathic.finder.sdk.traffic.task.BusStationLinesRequest;
import com.telepathic.finder.sdk.traffic.task.GetBusLineTask;
import com.telepathic.finder.sdk.traffic.task.GetBusLocationRequest;
import com.telepathic.finder.sdk.traffic.task.GetBusStationRequest;
import com.telepathic.finder.sdk.traffic.task.GetConsumerRecordRequest;
import com.telepathic.finder.sdk.traffic.task.NetworkManager;
import com.telepathic.finder.util.Utils;

public class TrafficManager {
	private static final String TAG = TrafficManager.class.getSimpleName();
	
    private static TrafficManager mInstance;

    private Context mContext;

    private MKSearch mMapSearch;

    private NetworkManager mNetWorkAdapter;
    
    private TrafficeStore mTrafficeStore;

    private TrafficeMonitor mTrafficeMonitor;
    
    private Handler mMessageHandler;

    private MyConsumerRecordsListener mConsumerRecordsListener;
    
    private ExecutorService mExecutorService;

    private TrafficManager(BMapManager manager, Context appContext, Handler msgHandler) {
    	mContext = appContext;
        mNetWorkAdapter = new NetworkManager();
        mTrafficeStore =  TrafficeStore.getDefaultStore(mContext);
        mMapSearch = new MKSearch();
        mTrafficeMonitor = new TrafficeMonitor();
        mMessageHandler = msgHandler;
        mExecutorService = Executors.newCachedThreadPool();
    }

    public static synchronized TrafficManager getTrafficManager(BMapManager manager,
            Context appContext, Handler handler) {
        if (mInstance == null) {
            mInstance = new TrafficManager(manager, appContext, handler);
        }
        return mInstance;
    }
    
    public ITrafficService getTrafficService() {
        return new TrafficeService();
    }

    private class TrafficeService implements ITrafficService {

        @Override
        public void searchBusLine(String city, String lineNumber) {
            mMapSearch.poiSearchInCity(city, lineNumber);
        }

        @Override
        public void searchBusRoute(String city, String routeUid) {
            mMapSearch.busLineSearch(city, routeUid);
        }

        @Override
        public void getBusStationLines(final String gpsNumber) {
        	mExecutorService.execute(new Runnable() {
        		private String[] mLineNames;
				@Override
				public void run() {
					Future<BusStation> result = mExecutorService.submit(new GetBusStationRequest("", gpsNumber));
		        	try {
		        		BusStation station = result.get();
						Utils.debug(TAG, "station name: " + station.getName());
						Future<String[]> lineNames= mExecutorService.submit(new BusStationLinesRequest(station.getName(), gpsNumber));
						String[] lines = lineNames.get();
						List<Future<BusStation>> results = new CopyOnWriteArrayList<Future<BusStation>>();
						for(String lineNumber: lines) {
							results.add(mExecutorService.submit(new GetBusStationRequest(lineNumber, gpsNumber)));
							mExecutorService.submit(new GetBusLineTask(mContext,lineNumber));
						}
						while (results.size() > 0) {
							for (Future<BusStation> f : results)
								if (f.isDone()) {
									results.remove(f);
									Thread.yield();
								}
						}
						notifyDone();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
				}
				
				private void notifyDone() {
		        	Message msg = Message.obtain();
		        	msg.arg1 = ITrafficeMessage.GET_BUS_STATION_LINES_DONE;
		        	msg.arg2 = 0;
		        	mMessageHandler.sendMessage(msg);
	        	}
				
			});
        	
        }

        // @Override
        // public void getBusLineRoute(String LineNumber, BusLineListener
        // listener) {
        // // TODO Auto-generated method stub
        //
        // }
        
        @Override
        public void getBusLocation(String lineNumber, String anchorStation, String lastStation) {

        }

        @Override
        public void getConsumerRecords(String cardId, int count) {
            if (mConsumerRecordsListener == null) {
                mConsumerRecordsListener = new MyConsumerRecordsListener();
                mTrafficeMonitor.add(mConsumerRecordsListener);
            }
            GetConsumerRecordRequest request = new GetConsumerRecordRequest(mTrafficeMonitor,
                    cardId, count);
            mNetWorkAdapter.execute(request);
        }

        @Override
        public void translateToStation(String lineNumber, String gpsNumber) {
            // TODO Auto-generated method stub

        }

        @Override
        public void translateToStation(String gpsNumber) {
//            mExecutorService.execute(new Runnable() {
//				@Override
//				public void run() {
//					for(int i = 10000; i < 20000; i++) {
//						Future<BusStation> result = mExecutorService.submit(new GetBusStationRequest("", String.format("%1$05d", i)));
//			        	try {
//			        		BusStation station = result.get();
//			        	} catch (Exception e) {
//			        		
//			        	}
//					}					
//				}
//			});
        	mExecutorService.submit(new GetBusLineTask(mContext,"102"));
//        	mExecutorService.execute(new Runnable() {
//				@Override
//				public void run() {
//					String[] gpsNumbers = {"50022", "50023", "50016", "50019", "50014", "50015",
//											"50012", "50013", "50010", "50011", "50403", "50404",
//											"41498", "41499", "41496", "41497", "40370", "40999",
//											"40236", "40237", "40234", "40235", "30339", "30340",
//											"30335", "30336", "30322", "30323", "20226", "20224",
//											"20225", "20222", "20223", "10523", "10486", "10487"
//							              };
//					for(String gpsNumber : gpsNumbers) {
//						GetBusStationRequest request = new GetBusStationRequest("102", gpsNumber);
//						try {
//							request.call();
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//					
//				}
//			});

        }

        @Override
        public ITrafficMonitor getTrafficMonitor() {
            return mTrafficeMonitor;
        }
        
        @Override
        public TrafficeStore getTrafficeStore() {
        	return mTrafficeStore;
        }
        
        @Override
        public void getBusLocation(BusRoute route) {
            GetBusLocationRequest request = new GetBusLocationRequest(mTrafficeMonitor, route);
            mNetWorkAdapter.execute(request);
        }

    }

    private class MyConsumerRecordsListener implements ConsumerRecordsListener {
        @Override
        public void onReceived(BusCard busCard) {
        	saveConsumerRecords(busCard);
        	notifyDataChanged();
        	Utils.copyAppDatabaseFiles(mContext.getPackageName());
        }
        
        private void notifyDataChanged() {
//        	Message msg = Message.obtain();
//        	msg.arg1 = ITrafficeMessage.CONSUMER_RECORD_UPDATED;
//        	msg.arg2 = 0;
//        	mMessageHandler.sendMessage(msg);
        }
        
        private void saveConsumerRecords(BusCard busCard) {
        	ContentResolver resolver = mContext.getContentResolver();
        	ContentValues values = new ContentValues();
        	values.put(BusCardColumns.CARD_NUMBER, busCard.getCardNumber());
        	values.put(BusCardColumns.RESIDUAL_COUNT, busCard.getResidualCount());
        	values.put(BusCardColumns.RESIDUAL_AMOUNT, busCard.getResidualAmount());
        	Uri uri = resolver.insert(ITrafficData.BusCard.CONTENT_URI, values);
        	long cardId = Long.parseLong(uri.getLastPathSegment());
        	for (ConsumerRecord record : busCard.getConsumerRecords()) {
        		values.clear();
        		values.put(ConsumerRecordColumns.CARD_ID, cardId);
        		values.put(ConsumerRecordColumns.LINE_NUMBER, record.getLineNumber());
        		values.put(ConsumerRecordColumns.BUS_NUMBER, record.getBusNumber());
        		values.put(ConsumerRecordColumns.DATE, Utils.formatDate(record.getConsumerTime()));
        		values.put(ConsumerRecordColumns.CONSUMPTION, record.getConsumption());
        		values.put(ConsumerRecordColumns.RESIDUAL, record.getResidual());
        		values.put(ConsumerRecordColumns.TYPE, record.getType().toString());
            	mTrafficeStore.insertConsumerRecord(values);
            	resolver.insert(ITrafficData.ConsumerRecord.CONTENT_URI, values);
            }
        	resolver.notifyChange(ITrafficData.BusCard.CONTENT_URI, null);
        	//resolver.notifyChange(ITrafficData.ConsumerRecord.CONTENT_URI, null);
        }
    }

    
}
