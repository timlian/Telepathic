package com.telepathic.finder.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import android.test.ApplicationTestCase;

import com.telepathic.finder.app.FinderApplication;
import com.telepathic.finder.sdk.ConsumerRecord;
import com.telepathic.finder.sdk.ConsumerRecord.ConsumerType;
import com.telepathic.finder.sdk.ConsumerRecordsListener;
import com.telepathic.finder.sdk.TrafficService;
import com.telepathic.finder.util.Utils;

public class RetrieveConsumerRecordTest extends ApplicationTestCase<FinderApplication> {
    private static final int CONSUMER_RECORD_COUNT = 20;
    private static final String CARD_ID = "10808691";

    private FinderApplication mApp = null;
    private TrafficService mTrafficService = null;
    private ArrayList<ConsumerRecord> mBenchmark = null;

    public RetrieveConsumerRecordTest(Class<FinderApplication> applicationClass) {
        super(applicationClass);
    }

    public RetrieveConsumerRecordTest() {
        super(FinderApplication.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();
        mApp = getApplication();
        mTrafficService = TrafficService.getTrafficService(mApp.getMapManager());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private void createBenchmark() {
        mBenchmark = new ArrayList<ConsumerRecord>();
        ConsumerRecord record = null;
        String[] sourceData = getSourceData();
        for(int i = 0; i < sourceData.length; i++) {
            record = parseConsumerRecord(sourceData[i]);
            mBenchmark.add(record);
        }
    }

    public void test_retrieve_consumer_records() {
        createBenchmark();

        TestChargeRecordsListener testChargeRecordsListener = new TestChargeRecordsListener();
        mTrafficService.retrieveConsumerRecords(CARD_ID, CONSUMER_RECORD_COUNT, testChargeRecordsListener);
        while(!testChargeRecordsListener.done()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static String[] getSourceData() {
        String[] sourceData =
            {
                "{lineNumber=102; busNumber=031154; cardID=000110808691; consumerTime=2013-1-1 19:23:59; consumerCount=2; residualCount=28; residualAmount=46.40;}",
                "{lineNumber=102; busNumber=031162; cardID=000110808691; consumerTime=2013-1-1 15:39:33; consumerCount=2; residualCount=30;  residualAmount=46.40;}" ,
                "{lineNumber=188; busNumber=031186; cardID=000110808691; consumerTime=2012-12-21 18:35:37; consumerCount=2; residualCount=6; residualAmount=46.40;}",
                "{lineNumber=102; busNumber=031149; cardID=000110808691; consumerTime=2012-12-15 20:40:37; consumerCount=2; residualCount=8; residualAmount=46.40;}",
                "{lineNumber=102; busNumber=031153; cardID=000110808691; consumerTime=2012-12-15 17:21:55; consumerCount=0; residualCount=10; residualAmount=46.40;}",
                "{lineNumber=185; busNumber=031144; cardID=000110808691; consumerTime=2012-12-15 17:08:52; consumerCount=2; residualCount=10; residualAmount=46.40;}",
                "{lineNumber=188; busNumber=031190; cardID=000110808691; consumerTime=2012-12-15 14:07:10; consumerCount=0; residualCount=12; residualAmount=46.40;}",
                "{lineNumber=185; busNumber=031195; cardID=000110808691; consumerTime=2012-12-15 12:14:36; consumerCount=2; residualCount=12; residualAmount=46.40;}",
                "{lineNumber=102; busNumber=031164; cardID=000110808691; consumerTime=2012-12-9 18:42:06; consumerCount=2; residualCount=14;  residualAmount=46.40;}",
                "{lineNumber=102; busNumber=031158; cardID=000110808691; consumerTime=2012-12-2 21:08:27; consumerCount=0; residualCount=16;  residualAmount=46.40;}",
                "{lineNumber=112; busNumber=039141; cardID=000110808691; consumerTime=2012-12-2 20:54:23; consumerCount=2; residualCount=16;  residualAmount=46.40;}",
                "{lineNumber=102; busNumber=031156; cardID=000110808691; consumerTime=2012-12-2 16:33:48; consumerCount=2; residualCount=18;  residualAmount=46.40;}",
                "{lineNumber=185; busNumber=031228; cardID=000110808691; consumerTime=2012-12-2 10:44:08; consumerAmount=1.80; residualAmount=46.40; residualCount=20;}",
                "{lineNumber=185; busNumber=031228; cardID=000110808691; consumerTime=2012-12-2 10:44:06; consumerAmount=1.80; residualAmount=48.20; residualCount=20;}",
                "{lineNumber=102; busNumber=031153; cardID=000110808691; consumerTime=2012-10-14 19:10:33; consumerAmount=1.80; residualAmount=3.20; residualCount=20;}",
                "{lineNumber=50; busNumber=049182; cardID=000110808691; consumerTime=2012-10-3 10:41:41; consumerAmount=1.80; residualAmount=5; residualCount=20;}",
                "{lineNumber=50; busNumber=049543; cardID=000110808691; consumerTime=2012-9-29 9:13:45; consumerAmount=1.80; residualAmount=6.80; residualCount=20;}",
                "{lineNumber=188; busNumber=031181; cardID=000110808691; consumerTime=2012-9-23 11:51:44; consumerAmount=1.80; residualAmount=12.20; residualCount=20;}",
                "{lineNumber=185; busNumber=034006; cardID=000110808691; consumerTime=2012-8-27 10:31:19; consumerAmount=1.80; residualAmount=14; residualCount=20;}",
                "{lineNumber=102; busNumber=031158; cardID=000110808691; consumerTime=2012-8-26 16:47:37; consumerAmount=1.80; residualAmount=23; residualCount=20;}",
            };
        return sourceData;
    }
    // {lineNumber=102; busNumber=031162; cardID=000110808691; consumerTime=2013-1-1 15:39:33; consumerCount=2; residualCount=30; }
    private static ConsumerRecord parseConsumerRecord(String data) {
        ConsumerRecord record = new ConsumerRecord();
        String[] properties = data.trim().replaceAll("[{}]", "").split(";");
        String name  = null;
        String value = null;
        for(String property : properties) {
            String[] item = property.trim().split("=");
            if(item.length == 2) {
                name  = item[0];
                value = item[1];
                if (record.getConsumerType() == null) {
                    if (name.equals("consumerCount")) {
                        record.setConsumerType(ConsumerType.COUNT);
                    } else if (name.equals("consumerAmount")) {
                        record.setConsumerType(ConsumerType.ELECTRONIC_WALLET);
                    }
                }
                Method method;
                try {
                    final String methodName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
                    if (methodName.equals("setConsumerTime")) {
                        method = ConsumerRecord.class.getMethod(methodName, Date.class);
                        method.invoke(record, Utils.parseDate(value));
                    } else {
                        method = ConsumerRecord.class.getMethod(methodName, String.class);
                        method.invoke(record, value);
                    }

                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return record;
    }

    private class TestChargeRecordsListener implements ConsumerRecordsListener {
        private boolean isDone = false;

        public boolean done() {
            return isDone;
        }

        @Override
        public void onSuccess(ArrayList<ConsumerRecord> consumerRecords) {
            assertNotNull(consumerRecords);
            assertEquals(CONSUMER_RECORD_COUNT, consumerRecords.size());
            for(int idx = 0; idx < CONSUMER_RECORD_COUNT; idx++) {
                assertEquals(mBenchmark.get(idx), consumerRecords.get(idx));
            }
            isDone = true;
        }

        @Override
        public void onError(String errorMessage) {
            assertFalse(errorMessage, true);
            isDone = true;
        }
    }

}
