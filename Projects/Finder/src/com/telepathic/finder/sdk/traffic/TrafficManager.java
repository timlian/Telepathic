
package com.telepathic.finder.sdk.traffic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.Context;
import android.database.Cursor;
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
import com.telepathic.finder.sdk.traffic.provider.ITrafficData;
import com.telepathic.finder.sdk.traffic.task.GetBusCardRecordsTask;
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
    private TrafficConfig mTrafficConfig;

    private TrafficManager(BMapManager manager, Context appContext, Handler msgHandler) {
    	mContext = appContext;
    	mMapManager = manager;
        mMessageHandler = msgHandler;
        mExecutorService = Executors.newCachedThreadPool();
        mTrafficStore =  new TrafficStore(mContext, mExecutorService);
        mTrafficConfig = new TrafficConfig();
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
					try {
						SearchBusLineTask searchTask = new SearchBusLineTask(mMapManager, city, lineNumber);
						searchTask.startTask();
						searchTask.waitTaskDone();
						// Notify the search bus line operation finished.
						TaskResult<ArrayList<MKPoiInfo>> taskResult = searchTask.getTaskResult();
						Message msg = Message.obtain();
						msg.arg1 = ITrafficeMessage.SEARCH_BUS_LINE_DONE;
						msg.arg2 = taskResult.getErrorCode();
						msg.obj = taskResult.getResult();
						mMessageHandler.sendMessage(msg);
						// store bus line info.
					    mTrafficStore.store(lineNumber, taskResult.getResult());
					} catch (InterruptedException e) {
						Utils.debug(TAG, "searchBusLine is interrupted.");
					}
					Utils.debug(TAG, "searchBusLine(" + city + ", " + lineNumber + ") finished.");
					Utils.copyAppDatabaseFiles(mContext.getPackageName());
				}
			});
		}
    	
        @Override
		public void searchBusRoute(final String city, final String routeUid) {
			mExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						SearchBusRouteTask searchTask = new SearchBusRouteTask(mMapManager, city, routeUid);
						searchTask.startTask();
						searchTask.waitTaskDone();
						// Notify the search bus route operation finished.
						TaskResult<MKRoute> taskResult = searchTask.getTaskResult();
						Message msg = Message.obtain();
						msg.arg1 = ITrafficeMessage.SEARCH_BUS_ROUTE_DONE;
						msg.arg2 = taskResult.getErrorCode();
						msg.obj = taskResult.getResult();
						mMessageHandler.sendMessage(msg);
						// store the bus route info.
						mTrafficStore.store(routeUid, taskResult.getResult());
					} catch (InterruptedException e) {
						Utils.debug(TAG, "searchBusRoute is interrupted.");
					}
					Utils.debug(TAG, "searchBusRoute(" + city + ", " + routeUid + ") finished.");
					Utils.copyAppDatabaseFiles(mContext.getPackageName());
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
						result4.add(mExecutorService.submit(new GetBusLineTask(lineNumber)));
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
					// store the data
					mTrafficStore.store(stationLines);
					Utils.copyAppDatabaseFiles(mContext.getPackageName());
				}
			});
        	
        }

        @Override
        public void getBusCardRecords(final String cardNumber, final int count) {
			mExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						boolean needUpdate = true;
						String[] projection = new String[]{ITrafficData.KuaiXinData.BusCard.LAST_UPDATE_TIME};
						String selection = ITrafficData.KuaiXinData.BusCard.CARD_NUMBER + "=?";
						String[] selectionArgs = new String[]{cardNumber};
						Cursor cursor = mContext.getContentResolver().query(ITrafficData.KuaiXinData.BusCard.CONTENT_URI, projection, selection, selectionArgs, null);
						if (cursor != null && cursor.moveToFirst()) {
							long lastUpdateTime = cursor.getLong(0);
							long interval = System.currentTimeMillis() - lastUpdateTime;
							if (interval <= mTrafficConfig.getBusCardUpdateInterval()) {
								needUpdate = false;
							}
						}
						if (needUpdate) {
							GetBusCardRecordsTask task = new GetBusCardRecordsTask(cardNumber, count);
							task.startTask();
							task.waitTaskDone();
							TaskResult<BusCard> result = task.getTaskResult();
							Message msg = Message.obtain();
							msg.arg1 = ITrafficeMessage.GET_BUS_CARD_RECORDS_DONE;
							msg.arg2 = result.getErrorCode();
							msg.obj  = result.getErrorMessage();
							mMessageHandler.sendMessage(msg);
							BusCard busCard = result.getResult();
							if (busCard != null) {
								mTrafficStore.store(busCard, true);
							}
						} else {
							Message msg = Message.obtain();
							msg.arg1 = ITrafficeMessage.GET_BUS_CARD_RECORDS_DONE;
							mMessageHandler.sendMessage(msg);
						}
					} catch (InterruptedException e) {
						Utils.debug(TAG, "getBusCardRecords is interrupted.");
					} 
					Utils.debug(TAG, "getBusCardRecords(" + cardNumber + ", " + count + ") finished.");
					Utils.copyAppDatabaseFiles(mContext.getPackageName());
				}
			});
        }

		@Override
		public void getBusLocation(final String lineNumber, final ArrayList<String> route) {
			mExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					try {
						final BlockingQueue<Integer> queue = new LinkedBlockingQueue<Integer>();
						GetBusLocationTask task = new GetBusLocationTask(lineNumber, route, queue);
						final Integer lastLocation = -1;
						task.startTask();
						task.setLastLocation(lastLocation);
						while (!Thread.interrupted()) {
							Integer location = null;
							location = queue.take();
							if (location != lastLocation) {
								Message msg = Message.obtain();
								msg.arg1 = ITrafficeMessage.GET_BUS_LOCATION_UPDATED;
								msg.arg2 = 0;
								msg.obj = location;
								mMessageHandler.sendMessage(msg);
							} else {
								TaskResult<Integer> taskResult = task.getTaskResult();
								if (taskResult != null) {
									Message msg = Message.obtain();
									msg.arg1 = ITrafficeMessage.GET_BUS_LOCATION_DONE;
									msg.arg2 = taskResult.getErrorCode();
									msg.obj = taskResult.getErrorMessage();
									mMessageHandler.sendMessage(msg);
								}
								Thread.currentThread().interrupt();
							}
						}
					} catch (InterruptedException ex) {
						Utils.debug(TAG, "getBusLocation is interrupted.");
					}
					Utils.debug(TAG, "getBusLocation finished.");
				}
			});
		}

	}

}
