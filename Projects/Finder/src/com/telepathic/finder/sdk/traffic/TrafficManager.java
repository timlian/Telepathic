
package com.telepathic.finder.sdk.traffic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.search.MKPoiInfo;
import com.baidu.mapapi.search.MKRoute;
import com.telepathic.finder.sdk.ITrafficService;
import com.telepathic.finder.sdk.ITrafficeMessage;
import com.telepathic.finder.sdk.traffic.entity.BusCard;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusLine.Direction;
import com.telepathic.finder.sdk.traffic.entity.kuaixin.KXBusStationLines;
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
    private ScheduledThreadPoolExecutor mTaskScheduler;
    private BMapManager mMapManager;
    private TrafficConfig mTrafficConfig;

    private TrafficManager(BMapManager manager, Context appContext, Handler msgHandler) {
        mContext = appContext;
        mMapManager = manager;
        mMessageHandler = msgHandler;
        mExecutorService = Executors.newCachedThreadPool();
        mTaskScheduler = new ScheduledThreadPoolExecutor(1);
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
            final SearchBusLineTask searchTask = new SearchBusLineTask(mMapManager, city, lineNumber);
            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
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
            final SearchBusRouteTask searchTask = new SearchBusRouteTask(mMapManager, city, routeUid);
            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
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

        @Override        public void getBusStationLines(final String gpsNumber) {
            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        final KXBusStationLines stationLines = new KXBusStationLines();
                        stationLines.setGpsNumber(gpsNumber);
                        // Translate the specified gps number to corresponding station name.
                        TranslateToStationTask translateTask = new TranslateToStationTask("", gpsNumber);
                        translateTask.startTask();
                        translateTask.waitTaskDone();
                        TaskResult<String> translateRusult = translateTask.getTaskResult();
                        String stationName = null;
                        if (translateRusult != null) {
                            stationName = translateRusult.getResult();
                        }
                        if (stationName == null || stationName.equals("")) {
                            return ;
                        }
                        stationLines.setName(stationName);
                        // Get the bus line numbers according to the gps number and station name.
                        GetBusStationLinesTask getLineNumbersTask = new GetBusStationLinesTask(stationName, gpsNumber);
                        getLineNumbersTask.startTask();
                        getLineNumbersTask.waitTaskDone();
                        TaskResult<List<String>> getLineNumbersResult = getLineNumbersTask.getTaskResult();
                        List<String> lineNumbers = null;
                        if (getLineNumbersResult != null) {
                            lineNumbers = getLineNumbersResult.getResult();
                        }
                        if (lineNumbers == null || lineNumbers.size() == 0) {
                            return ;
                        }

                        // Get the bus line details according to the line numbers.
                        final CountDownLatch latch = new CountDownLatch(2 * lineNumbers.size());

                        for(String lineNumber: lineNumbers) {
                            final TranslateToStationTask getDirectionTask = new TranslateToStationTask(lineNumber, gpsNumber);
                            getDirectionTask.setCallback(new Runnable() {
                                @Override
                                public void run() {
                                    synchronized (stationLines) {
                                        String[] stationLine  = getDirectionTask.getTaskResult().getResult().split(",");
                                        String lineNumber = stationLine[0];
                                        String stationName2 = stationLine[1];
                                        String direction = stationLine[2];
                                        stationLines.addLineDirection(lineNumber, Direction.fromString(direction));
                                    }
                                    latch.countDown();
                                }
                            });
                            getDirectionTask.startTask();

                            final GetBusLineTask getLineTask = new GetBusLineTask(lineNumber);
                            getLineTask.setCallback(new Runnable() {
                                @Override
                                public void run() {
                                    synchronized (stationLines) {
                                        stationLines.addBusLine(getLineTask.getTaskResult().getResult());
                                    }
                                    latch.countDown();
                                }
                            });
                            getLineTask.startTask();
                        }

                        latch.await();

                        // Notify the get bus station lines operation finished.
                        Message msg = Message.obtain();
                        msg.arg1 = ITrafficeMessage.GET_BUS_STATION_LINES_DONE;
                        msg.arg2 = 0;
                        msg.obj = stationLines;
                        mMessageHandler.sendMessage(msg);
                        // store the data
                        long startTime = System.currentTimeMillis();
                        mTrafficStore.store(stationLines);
                        long endTime = System.currentTimeMillis();
                        Utils.debug(TAG, "store consume time: " + String.valueOf(endTime - startTime) + " in ms");
                    } catch (InterruptedException e) {
                        Utils.debug(TAG, "getBusStationLines is interrupted.");
                    }
                    Utils.copyAppDatabaseFiles(mContext.getPackageName());
                    Utils.debug(TAG, "getBusStationLines(" + gpsNumber + ") finished");
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
            mTaskScheduler.scheduleAtFixedRate(new Runnable() {
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
            }, 0, 20, TimeUnit.SECONDS);
        }

        @Override
        public void shutDown() {
            Utils.debug(TAG, "shutdown()");
            if (mExecutorService != null) {
                mExecutorService.shutdownNow();
            }
            if (mTaskScheduler != null) {
                mTaskScheduler.shutdownNow();
            }
        }
    }

}
