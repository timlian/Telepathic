
package com.telepathic.finder.sdk.traffic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.text.StaticLayout;
import android.util.Log;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.mapapi.search.MKRoute;
import com.telepathic.finder.R;
import com.telepathic.finder.sdk.ICompletionListener;
import com.telepathic.finder.sdk.IErrorCode;
import com.telepathic.finder.sdk.ITrafficService;
import com.telepathic.finder.sdk.ITrafficeMessage;
import com.telepathic.finder.sdk.traffic.entity.BusCard;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusLine;
import com.telepathic.finder.sdk.traffic.provider.ITrafficData;
import com.telepathic.finder.sdk.traffic.request.GetBusStationLinesRequest.StationLines;
import com.telepathic.finder.sdk.traffic.request.GetBusStationRequest.Station;
import com.telepathic.finder.sdk.traffic.request.GetBusTransferRouteRequest;
import com.telepathic.finder.sdk.traffic.request.GetStationNameRequest;
import com.telepathic.finder.sdk.traffic.request.RequestCallback;
import com.telepathic.finder.sdk.traffic.request.RequestExecutor;
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
    private BusLineObserver mBusLineObserver;
    private BusStationObserver mBusStationObserver;
    private ContentResolver mContentResolver;
    
    private static final int MAX_LINE_COUNT = 10;
    private static final int MAX_STATION_COUNT = 10;
    
    private static final String[] BUS_LINE_PROJECTION = {
        ITrafficData.BaiDuData.BusLine._ID
    };
    private static final int IDX_BUS_LIND_ID = 0;
    
    private static final String[] BUS_STATION_PROJECTION = {
        ITrafficData.KuaiXinData.BusStation._ID,
        ITrafficData.KuaiXinData.BusStation.NAME
    };
    private static final int IDX_BUS_STATION_ID = 0;
    private static final int IDX_BUS_STATION_NAME = 1;
    

    private TrafficManager(BMapManager manager, Context appContext, Handler msgHandler) {
        mContext = appContext;
        mMapManager = manager;
        mMessageHandler = msgHandler;
        mExecutorService = Executors.newCachedThreadPool();
        mTrafficStore =  new TrafficStore(mContext, mExecutorService);
        mTrafficConfig = new TrafficConfig();
        mBusLineObserver = new BusLineObserver();
        mBusStationObserver = new BusStationObserver();
        mContentResolver = mContext.getContentResolver();
        mContentResolver.registerContentObserver(ITrafficData.BaiDuData.BusLine.CONTENT_URI, false, mBusLineObserver);
        mContentResolver.registerContentObserver(ITrafficData.KuaiXinData.BusStation.CONTENT_URI, false, mBusStationObserver);
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
        public void searchBusLine(final String city, final String lineNumber, final ICompletionListener listener) {
            // The creation of search bus line task must be in the thread, which has looper.
            final SearchBusLineTask searchTask = new SearchBusLineTask(mMapManager, city, lineNumber);
            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!Utils.hasActiveNetwork(mContext)) {
                            notifyFailure(listener, IErrorCode.ERROR_NO_NETWORK, "No networking.");
                        }
                        searchTask.startTask();
                        searchTask.waitTaskDone();

                        TaskResult<ArrayList<MKPoiInfo>> taskResult = searchTask.getTaskResult();
                        if (taskResult != null) {
                            int errorCode = taskResult.getErrorCode();
                            if (errorCode != 0) {
                                notifyFailure(listener, errorCode, taskResult.getErrorMessage());
                            } else {
                                ArrayList<MKPoiInfo> line = taskResult.getContent();
                                 if (line != null && line.size() > 0) {
                                    mTrafficStore.store(lineNumber, line);
                                    notifySuccess(listener, null);
                                } else {
                                    notifyFailure(listener, IErrorCode.ERROR_NO_VALID_DATA, "No bus line info.");
                                }
                            }
                        } else {
                            notifyFailure(listener, IErrorCode.ERROR_UNKNOWN, "Exception: the search bus line task result is null.");
                        }
                    } catch (InterruptedException e) {
                        Utils.debug(TAG, "searchBusLine is interrupted.");
                    }
                    Utils.debug(TAG, "searchBusLine(" + city + ", " + lineNumber + ") finished.");
                }
            });
        }

        @Override
        public void searchBusRoute(final String city, final String routeUid, final ICompletionListener listener) {
            // The creation of search bus route task must be in the thread, which has looper.
            final SearchBusRouteTask searchTask = new SearchBusRouteTask(mMapManager, city, routeUid);
            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (!Utils.hasActiveNetwork(mContext)) {
                            notifyFailure(listener, IErrorCode.ERROR_NO_NETWORK, "No networking.");
                        }
                        searchTask.startTask();
                        searchTask.waitTaskDone();
                        TaskResult<MKRoute> taskResult = searchTask.getTaskResult();
                        if (taskResult != null) {
                            int errorCode = taskResult.getErrorCode();
                            if (errorCode != 0) {
                                notifyFailure(listener, errorCode, taskResult.getErrorMessage());
                            } else {
                                MKRoute route = taskResult.getContent();
                                if (route != null) {
                                    // store the bus route info.
                                    mTrafficStore.store(routeUid, taskResult.getContent());
                                    notifySuccess(listener, route);
                                } else {
                                    notifyFailure(listener, IErrorCode.ERROR_NO_VALID_DATA, "No bus route info.");
                                }
                            }
                        } else {
                            notifyFailure(listener, IErrorCode.ERROR_UNKNOWN, "Exception: the search bus route task result is null.");
                        }
                    } catch (InterruptedException e) {
                        Utils.debug(TAG, "searchBusRoute is interrupted.");
                    }
                    Utils.debug(TAG, "searchBusRoute(" + city + ", " + routeUid + ") finished.");
                }
            });
        }


        @Override
        public void getBusStationLines(final String stationName, final ICompletionListener listener) {
            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                    	int errorCode = 0;
                    	String errorText = "";
                    	if (!Utils.hasActiveNetwork(mContext)) {
                            errorCode = IErrorCode.ERROR_NO_NETWORK;
                        }
                    	// retrieve station lines from server
                    	List<StationLines> tempList = null;
                    	if (errorCode == 0) {
                    		GetBusStationLinesTask task = new GetBusStationLinesTask(stationName);
                    		task.startTask();
                    		task.waitTaskDone();
                    		errorCode = task.getTaskResult().getErrorCode();
                    		if (errorCode == 0) {
                    			tempList = task.getTaskResult().getContent();
                    			if (tempList == null || tempList.size() == 0) {
                    				errorCode = IErrorCode.ERROR_NO_VALID_DATA;
                    				errorText = "no valid data";
                    			}
                    		} else {
                    			errorText = task.getTaskResult().getErrorMessage();
                    		}
                    	}
                    	final List<StationLines> stationList = tempList;
                    	// retrieve the bus lines related to the specified station.
                    	if (errorCode == 0) {
                    		ArrayList<String> lineNumbers = new ArrayList<String>();
                    		for(StationLines station : stationList) {
                    			for(String lineNo : station.getLines()) {
                    				if (!lineNumbers.contains(lineNo)) {
                    					lineNumbers.add(lineNo);
                    				}
                    			}
                    		}
                    		if (lineNumbers.size() == 0) {
                    			errorCode = IErrorCode.ERROR_UNKNOWN;
                    			errorText = "line number is empty!!!";
                    		}
							if (errorCode == 0) {
								final CountDownLatch busLineLatch = new CountDownLatch(lineNumbers.size());
								final int error = 0;
								final String text = "";
								for (String lineNumber : lineNumbers) {
									final GetBusLineTask getLineTask = new GetBusLineTask(lineNumber);
									getLineTask.setCallback(new Runnable() {
										@Override
										public void run() {
										int	error = getLineTask.getTaskResult().getErrorCode();
											if (error == 0) {
												KXBusLine line = getLineTask.getTaskResult().getContent();
												if (line != null) {
													mTrafficStore.store(line);
											} else {
												//text = getLineTask.getTaskResult().getErrorMessage();
											}
											busLineLatch.countDown();
										}
									}});
									getLineTask.startTask();
								}
								busLineLatch.await();
								errorCode = error;
								errorText = text;
							}
                    	}
                    	
                    	Utils.debug(TAG, "Error code: " + errorCode);
                    	// corrected 
                    	Utils.debug(TAG, "start get direction.");
                    	int count = 0;
            			for(StationLines station : stationList) {
            				count += station.getLines().size();
            			}
            			
            			final CountDownLatch lineDirectionLatch = new CountDownLatch(count);
            			final Object lock = new Object();
            			for(StationLines station : stationList) {
            				final String gpsNumber = station.getGpsNumber();
            				for(String lineNumber : station.getLines()) {
            					 final TranslateToStationTask task = new TranslateToStationTask(lineNumber, gpsNumber);
            					 task.setCallback(new Runnable() {
            	                     @Override
            						public void run() {
            	                    	 synchronized (lock) {
            	                    		try {
	            								Utils.debug(TAG, "Thead " + Thread.currentThread().getId() + " enter critical section.");
	            								int errorCode = task.getTaskResult().getErrorCode();
	            								if (errorCode == 0) {
	            									Station station = task.getTaskResult().getContent();
	            									if (station != null) {
	            										for(int idx = 0; idx < stationList.size(); idx++) {
	            											String gpsNumber = stationList.get(idx).getGpsNumber();
	            											if (gpsNumber.equals(station.getGpsNumber())) {
	            												stationList.get(idx).setDirection(station.getLineNumber(), station.getDirection());
	            											}
	            										}
	            									}
	            								} 
	            								Utils.debug(TAG, "Thead " + Thread.currentThread().getId() + " leave critical section.");
	            								lineDirectionLatch.countDown();
            	                    		} catch (Exception e) {
            	                    			e.printStackTrace();
            	                    		}
            	                    	 }
            	                     }
            					});
            	                task.startTask();
            				}
            			}
            			lineDirectionLatch.await();
            			
                    	Utils.debug(TAG, "finished get direction.");
                    	mTrafficStore.store(stationList);
                    	if (errorCode == 0) {
                    		notifySuccess(listener, null);
                    	} else {
                    		notifyFailure(listener, errorCode, errorText);
                    	}
                    	
                    } catch (InterruptedException e) {
                        Utils.debug(TAG, "getBusStationLines is interrupted.");
                    }
                    Utils.debug(TAG, "getBusStationLines(" + stationName + ") finished");
                }
            });

        }
            
        @Override
        public void getBusCardRecords(final String cardNumber, final int count) {
            if (mExecutorService.isShutdown()) {
                mExecutorService = Executors.newCachedThreadPool();
            }
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
                            if (result != null) {
                                msg.arg2 = result.getErrorCode();
                                msg.obj  = result.getErrorMessage();
                                BusCard busCard = result.getContent();
                                if (busCard != null) {
                                    mTrafficStore.store(busCard, true);
                                }
                            } else {
                                msg.arg2 = ITrafficeMessage.GET_BUS_CARD_RECORDS_FAILED;
                                msg.obj = mContext.getResources().getString(R.string.get_bus_card_records_failed);
                            }
                            mMessageHandler.sendMessage(msg);
                        } else {
                            Message msg = Message.obtain();
                            msg.arg1 = ITrafficeMessage.GET_BUS_CARD_RECORDS_DONE;
                            mMessageHandler.sendMessage(msg);
                        }
                    } catch (InterruptedException e) {
                        Utils.debug(TAG, "getBusCardRecords is interrupted.");
                    }
                    Utils.debug(TAG, "getBusCardRecords(" + cardNumber + ", " + count + ") finished.");
                }
            });
        }

        @Override
        public void getBusLocation(final String lineNumber, final ArrayList<String> route) {
            if (mExecutorService.isShutdown()) {
                mExecutorService = Executors.newCachedThreadPool();
            }
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
                                Message msg = Message.obtain();
                                msg.arg1 = ITrafficeMessage.GET_BUS_LOCATION_DONE;
                                if (taskResult != null) {
                                    msg.arg2 = taskResult.getErrorCode();
                                    msg.obj = taskResult.getErrorMessage();
                                }
                                mMessageHandler.sendMessage(msg);
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

        @Override
		public void getBusTransferRoute(final String source, final String destination, final ICompletionListener listener) {
			mExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					if (!Utils.hasActiveNetwork(mContext)) {
						notifyFailure(listener, IErrorCode.ERROR_NO_NETWORK, "no network.");
						return ;
					}
					GetBusTransferRouteRequest request = new GetBusTransferRouteRequest(source, destination);
					RequestExecutor.execute(request, new RequestCallback() {
						@Override
						public void onSuccess(Object result) {
							notifySuccess(listener, result);
						}
						
						@Override
						public void onError(int errorCode, String errorMessage) {
							notifyFailure(listener, errorCode, errorMessage);
						}
					});
				}
			});
			
		}
        
        @Override
        public void shutDown() {
            if (mExecutorService != null) {
                mExecutorService.shutdownNow();
            }
        }

		@Override
		public void queryStationName(final String query, final ICompletionListener listener) {
			mExecutorService.execute(new Runnable() {
				@Override
				public void run() {
					if (Utils.hasActiveNetwork(mContext)) {
						GetStationNameRequest request = new GetStationNameRequest(query);
						RequestExecutor.execute(request, new RequestCallback() {
							@Override
							public void onSuccess(Object result) {
								notifySuccess(listener, result);
							}
							
							@Override
							public void onError(int errorCode, String errorMessage) {
								notifyFailure(listener, errorCode, errorMessage);
							}
						});
					} else {
						ArrayList<String> stationNames = null;
						String selection = ITrafficData.KuaiXinData.BusStation.NAME + " LIKE \'%" + query + "%\'";
                    	//String[] selectionArgs = new String[]{query};
                    	String sortOrder = ITrafficData.KuaiXinData.BusStation.LAST_UPDATE_TIME + " DESC ";
                    	Cursor cursor = mContentResolver.query(ITrafficData.KuaiXinData.BusStation.CONTENT_URI, BUS_STATION_PROJECTION, selection, null, sortOrder);
                    	if (cursor != null) {
                    		try {
                    			stationNames = new ArrayList<String>();
                    			while(cursor.moveToNext()) {
                    				stationNames.add(cursor.getString(IDX_BUS_STATION_NAME));
                    			}
                    		} finally {
                    			cursor.close();
                    		}
                    	}
                    	if (stationNames != null && stationNames.size() > 0) {
                    		notifySuccess(listener, stationNames);
                    	} else {
                    		notifyFailure(listener, IErrorCode.ERROR_NO_VALID_DATA, "no valid data");
                    	}
					}
				}
			});
		}
		
		private int getDirections(final List<StationLines> stationList) throws InterruptedException {
			int count = 0;
			for(StationLines station : stationList) {
				count += station.getLines().size();
			}
			if (count == 0) {
				return IErrorCode.ERROR_UNKNOWN;
			}
			final CountDownLatch lineDirectionLatch = new CountDownLatch(count);
			final int result = 0;
			for(StationLines station : stationList) {
				String gpsNumber = station.getGpsNumber();
				for(String lineNumber : station.getLines()) {
					 final TranslateToStationTask task = new TranslateToStationTask(lineNumber, gpsNumber);
					 task.setCallback(new Runnable() {
	                     @Override
						public void run() {
							synchronized (stationList) {
								Utils.debug(TAG, "Eneter: "+ Thread.currentThread());
								lineDirectionLatch.countDown();
								int errorCode = task.getTaskResult().getErrorCode();
								if (errorCode == 0) {
									Station station = task.getTaskResult().getContent();
									if (station != null) {
										setDirection(stationList, station);
									}
								} else {
									String errorMessage = task.getTaskResult().getErrorMessage();
									Log.d(TAG, "get bus line direction falied: errorCode = "
													+ errorCode
													+ ", caused by "
													+ errorMessage);
								}
								Utils.debug("Tim", "Received response####1: ");
								Utils.debug(TAG, "Leave: "+ Thread.currentThread());
							}
						}
					});
	                task.startTask();
				}
			}
			lineDirectionLatch.await();
			return result;
		}
		
		private void setDirection(List<StationLines> stationList, Station station) {
			for(int idx = 0; idx < stationList.size(); idx++) {
				String gpsNumber = stationList.get(idx).getGpsNumber();
				if (gpsNumber.equals(station.getGpsNumber())) {
					stationList.get(idx).setDirection(station.getLineNumber(), station.getDirection());
				}
			}
		}

    }

    private void notifySuccess(final ICompletionListener listener, final Object result) {
        if (listener != null) {
            mMessageHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onSuccess(result);
                }
            });
        }
    }

    private void notifyFailure(final ICompletionListener listener, final int errorCode, final String errorText) {
        if (listener != null) {
            mMessageHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onFailure(errorCode, errorText);
                }
            });
        }
    }
    
    private class BusLineObserver extends ContentObserver {

		public BusLineObserver() {
			super(null);
		}
		
		@Override
		public void onChange(boolean selfChange) {
			Utils.debug(TAG, "BusLineObserver get called: " + selfChange);
			deleteObsoleteBusLines();
		}
    }
    
    private class BusStationObserver extends ContentObserver {
    	public BusStationObserver() {
			super(null);
		}
		
		@Override
		public void onChange(boolean selfChange) {
			Utils.debug(TAG, "BusStationObserver get called: " + selfChange);
			deleteObsoleteBusStations();
		}
    }
    
    private void deleteObsoleteBusLines() {
        String sortOrder = ITrafficData.BaiDuData.BusLine.LAST_UPDATE_TIME + " DESC ";
        Cursor cursor = mContentResolver.query(ITrafficData.BaiDuData.BusLine.CONTENT_URI, BUS_LINE_PROJECTION, null, null, sortOrder);
        ArrayList<String> lineIds = null;
        if (cursor != null && cursor.moveToFirst()) {
        	lineIds = new ArrayList<String>();
        	try {
    			for(int pos = MAX_LINE_COUNT; cursor.moveToPosition(pos); pos++) {
    				lineIds.add(cursor.getString(IDX_BUS_LIND_ID));
    			}
        	} finally {
        		cursor.close();
        	}
        }
        if (lineIds != null && lineIds.size() > 0) {
        	String selection = ITrafficData.BaiDuData.BusLine._ID + "=?";
        	for(String id : lineIds) {
        		mContentResolver.delete(ITrafficData.BaiDuData.BusLine.CONTENT_URI, selection, new String[]{id});
        	}
        }
    }
    
    private void deleteObsoleteBusStations() {
        String sortOrder = ITrafficData.KuaiXinData.BusStation.LAST_UPDATE_TIME + " DESC ";
        Cursor cursor = mContentResolver.query(ITrafficData.KuaiXinData.BusStation.CONTENT_URI, BUS_STATION_PROJECTION, null, null, sortOrder);
        ArrayList<String> stationIds = null;
        if (cursor != null && cursor.moveToFirst()) {
        	stationIds = new ArrayList<String>();
        	try {
    			for(int pos = MAX_STATION_COUNT; cursor.moveToPosition(pos); pos++) {
    				stationIds.add(cursor.getString(IDX_BUS_STATION_ID));
    			}
        	} finally {
        		cursor.close();
        	}
        }
        if (stationIds != null && stationIds.size() > 0) {
        	String selection = ITrafficData.KuaiXinData.BusStation._ID + "=?";
        	for(String id : stationIds) {
        		mContentResolver.delete(ITrafficData.KuaiXinData.BusStation.CONTENT_URI, selection, new String[]{id});
        	}
        }
    }

}
