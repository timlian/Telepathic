
package com.telepathic.finder.sdk.traffic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.MKPoiInfo;
import com.baidu.mapapi.MKRoute;
import com.telepathic.finder.sdk.ITrafficService;
import com.telepathic.finder.sdk.ITrafficeMessage;
import com.telepathic.finder.sdk.traffic.entity.BusCard;
import com.telepathic.finder.sdk.traffic.entity.BusLine;
import com.telepathic.finder.sdk.traffic.entity.BusLine.Direction;
import com.telepathic.finder.sdk.traffic.entity.BusStationLines;
import com.telepathic.finder.sdk.traffic.task.GetBusCardTask;
import com.telepathic.finder.sdk.traffic.task.GetBusLineTask;
import com.telepathic.finder.sdk.traffic.task.GetBusLocationTask;
import com.telepathic.finder.sdk.traffic.task.GetBusStationLinesTask;
import com.telepathic.finder.sdk.traffic.task.SearchBusLineTask;
import com.telepathic.finder.sdk.traffic.task.SearchBusRouteTask;
import com.telepathic.finder.sdk.traffic.task.TaskResult;
import com.telepathic.finder.sdk.traffic.task.TranslateToStationTask;
import com.telepathic.finder.util.Utils;

public class TrafficManager {
	private static final String TAG = TrafficManager.class.getSimpleName();
    private static TrafficManager mInstance;
    private Context mContext;
    private TrafficStore mTrafficStore;
    private Handler mMessageHandler;
    private ExecutorService mExecutorService;
    private BMapManager mMapManager;

    private TrafficManager(BMapManager manager, Context appContext, Handler msgHandler) {
    	mContext = appContext;
    	mMapManager = manager;
        mMessageHandler = msgHandler;
        mExecutorService = Executors.newCachedThreadPool();
        mTrafficStore =  new TrafficStore(mContext, mExecutorService);
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
		public void searchBusLine(final String city, final String lineNumber) {
    		 mExecutorService.execute(new Runnable() {
 				@Override
 				public void run() {
 					SearchBusLineTask searchTask = new SearchBusLineTask(mMapManager, city, lineNumber);
 					searchTask.startTask();
 					try {
 						searchTask.waitTaskDone();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					} finally {
 						// Notify the search bus line operation finished.
	 					TaskResult<ArrayList<MKPoiInfo>> taskResult = searchTask.getTaskResult();
						Message msg = Message.obtain();
			        	msg.arg1 = ITrafficeMessage.SEARCH_BUS_LINE_DONE;
			        	msg.arg2 = taskResult.getErrorCode();
			        	msg.obj  = taskResult.getResult();
			        	mMessageHandler.sendMessage(msg);
 					}
 				}
 			});
		}
    	
        @Override
        public void searchBusRoute(final String city, final String routeUid) {
            mExecutorService.execute(new Runnable() {
            	@Override
            	public void run() {
            		SearchBusRouteTask searchTask = new SearchBusRouteTask(mMapManager, city, routeUid);
 					searchTask.startTask();
 					try {
 						searchTask.waitTaskDone();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					} finally {
 						// Notify the search bus route operation finished.
	 					TaskResult<MKRoute> taskResult = searchTask.getTaskResult();
						Message msg = Message.obtain();
			        	msg.arg1 = ITrafficeMessage.SEARCH_BUS_ROUTE_DONE;
			        	msg.arg2 = taskResult.getErrorCode();
			        	msg.obj  = taskResult.getResult();
			        	mMessageHandler.sendMessage(msg);
 					}
            	}
			});
            
        }

        @Override
        public void getBusStationLines(final String gpsNumber) {
        	mExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					final BusStationLines stationLines = new BusStationLines();
					synchronized (stationLines) {
						stationLines.setGpsNumber(gpsNumber);
					}
					// Translate the specified gps number to corresponding station name.
					Future<String> result1 = mExecutorService.submit(new TranslateToStationTask("", gpsNumber));
					String stationName = null;
					try {
						stationName = result1.get();
						synchronized (stationLines) {
							stationLines.setStationName(stationName);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
						return ;
					} catch (ExecutionException e) {
						e.printStackTrace();
						return ;
					}
					// Get the bus line numbers according to the gps number and station name.
					Future<String[]> result2 = mExecutorService.submit(new GetBusStationLinesTask(stationName, gpsNumber));
					String[] lineNumbers = null;
					try {
						lineNumbers = result2.get();
					} catch (InterruptedException e) {
						e.printStackTrace();
						return ;
					} catch (ExecutionException e) {
						e.printStackTrace();
						return ;
					}
					// Get the bus line details according to the line numbers.
					final List<Future<String>>  result3 = new CopyOnWriteArrayList<Future<String>>();
					final List<Future<BusLine>> result4 = new CopyOnWriteArrayList<Future<BusLine>>();
					final CountDownLatch latch = new CountDownLatch(2 * lineNumbers.length);
					
					// Post the bus station lines result.
					mExecutorService.execute(new Runnable() {
						@Override
						public void run() {
							try {
								latch.await();
							} catch (InterruptedException e) {
								e.printStackTrace();
								return ;
							}
							// Notify the get bus station lines operation finished.
							Message msg = Message.obtain();
				        	msg.arg1 = ITrafficeMessage.GET_BUS_STATION_LINES_DONE;
				        	msg.arg2 = 0;
				        	msg.obj = stationLines;
				        	mMessageHandler.sendMessage(msg);
						}
					});
					
					for(String lineNumber: lineNumbers) {
						result3.add(mExecutorService.submit(new TranslateToStationTask(lineNumber, gpsNumber)));
						result4.add(mExecutorService.submit(new GetBusLineTask(mContext,lineNumber)));
					}
					// wait to process the result3
					mExecutorService.execute(new Runnable() {
						@Override
						public void run() {
							while (result3.size() > 0) {
								for (Future<String> result : result3)
									if (result.isDone()) {
										try {
											String[] stationLine  = result.get().split(",");
											String lineNumber = stationLine[0];
											String stationName = stationLine[1];
											String direction = stationLine[2];
											synchronized (stationLines) {
												stationLines.setLineRoute(lineNumber, Direction.fromString(direction));
											}
										} catch (InterruptedException e) {
											e.printStackTrace();
											return ;
										} catch (ExecutionException e) {
											e.printStackTrace();
											return ;
										}
										result3.remove(result);
										latch.countDown();
										Thread.yield();
									}
							}
						}
					});
					
					// wait to process the result4
					mExecutorService.execute(new Runnable() {
						@Override
						public void run() {
							while (result4.size() > 0) {
								for (Future<BusLine> result : result4)
									if (result.isDone()) {
										try {
											BusLine line = result.get();
											synchronized (stationLines) {
												stationLines.setBusLine(line);
											}
										} catch (InterruptedException e) {
											e.printStackTrace();
											return ;
										} catch (ExecutionException e) {
											e.printStackTrace();
											return ;
										}
										result4.remove(result);
										latch.countDown();
										Thread.yield();
									}
							}
						}
					});
				}
			});
        }

        @Override
        public void getBusCardRecords(final String cardId, final int count) {
            mExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					TaskResult<BusCard> result = null;
					Future<TaskResult<BusCard>> taskResult = mExecutorService.submit(new GetBusCardTask(cardId, count));
					 try {
						result = taskResult.get();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					} finally {
						Message msg = Message.obtain();
			        	msg.arg1 = ITrafficeMessage.GET_BUS_CARD_RECORDS_DONE;
			        	msg.arg2 = result.getErrorCode();
			        	msg.obj  = result.getErrorMessage();
			        	mMessageHandler.sendMessage(msg);
					}
					if (result.getErrorCode() == 0) {
						mTrafficStore.store(result.getResult(), true);
					}
				 } 
			});
        }

		@Override
		public void getBusLocation(final String lineNumber, final ArrayList<String> route) {
			mExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					GetBusLocationTask task = new GetBusLocationTask(lineNumber, route);
					while (!task.isDone()) {
						try {
							task.waitTaskDone();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			});
			
		}


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

        }


    }
