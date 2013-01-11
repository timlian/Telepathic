package com.telepathic.finder.test.sdk;

import java.util.ArrayList;
import java.util.HashMap;

import android.test.ApplicationTestCase;

import com.telepathic.finder.app.FinderApplication;
import com.telepathic.finder.sdk.ConsumerRecord;
import com.telepathic.finder.sdk.ConsumerRecord.ConsumerType;
import com.telepathic.finder.sdk.ConsumerRecordsListener;
import com.telepathic.finder.sdk.ConsumptionInfo;
import com.telepathic.finder.sdk.CountConsumerRecord;
import com.telepathic.finder.sdk.EWalletConsumerRecord;
import com.telepathic.finder.sdk.TrafficService;
import com.telepathic.finder.sdk.store.ConsumptionStore;
import com.telepathic.finder.util.Utils;


public class ConsumptionTest extends ApplicationTestCase<FinderApplication> {
    private final int CONSUMER_RECORD_COUNT = 20;
    private final String CARD_ID = "10808691";
    
    // consumer records constant keys
    private static final String KEY_CARD_ID        = "cardID";
    private static final String KEY_LINE_NUMBER    = "lineNumber";
    private static final String KEY_BUS_NUMBER     = "busNumber";
    private static final String KEY_CONSUMER_TIME  = "consumerTime";
    private static final String KEY_CONSUMER_COUNT = "consumerCount";
    private static final String KEY_RESIDUAL_COUNT = "residualCount";
    private static final String KEY_CONSUMER_AMOUNT = "consumerAmount";
    private static final String KEY_RESIDUAL_AMOUNT = "residualAmount";
    private static final String KEY_CONSUMPTION_TYPE = "type";
    
    private final String[] TEST_DATA =
    	{
    	    "{lineNumber=102; busNumber=031154; cardID=000110808691; consumerTime=2013-1-1 19:23:59; consumerCount=2; residualCount=28;   type=COUNT}",
    	    "{lineNumber=102; busNumber=031162; cardID=000110808691; consumerTime=2013-1-1 15:39:33; consumerCount=2; residualCount=30;   type=COUNT}" ,
    	    "{lineNumber=188; busNumber=031186; cardID=000110808691; consumerTime=2012-12-21 18:35:37; consumerCount=2; residualCount=6;  type=COUNT}",
    	    "{lineNumber=102; busNumber=031149; cardID=000110808691; consumerTime=2012-12-15 20:40:37; consumerCount=2; residualCount=8;  type=COUNT}",
    	    "{lineNumber=102; busNumber=031153; cardID=000110808691; consumerTime=2012-12-15 17:21:55; consumerCount=0; residualCount=10; type=COUNT}",
    	    "{lineNumber=185; busNumber=031144; cardID=000110808691; consumerTime=2012-12-15 17:08:52; consumerCount=2; residualCount=10; type=COUNT}",
    	    "{lineNumber=188; busNumber=031190; cardID=000110808691; consumerTime=2012-12-15 14:07:10; consumerCount=0; residualCount=12; type=COUNT}",
    	    "{lineNumber=185; busNumber=031195; cardID=000110808691; consumerTime=2012-12-15 12:14:36; consumerCount=2; residualCount=12; type=COUNT}",
    	    "{lineNumber=102; busNumber=031164; cardID=000110808691; consumerTime=2012-12-9 18:42:06; consumerCount=2; residualCount=14;  type=COUNT}",
    	    "{lineNumber=102; busNumber=031158; cardID=000110808691; consumerTime=2012-12-2 21:08:27; consumerCount=0; residualCount=16;  type=COUNT}",
    	    "{lineNumber=112; busNumber=039141; cardID=000110808691; consumerTime=2012-12-2 20:54:23; consumerCount=2; residualCount=16;  type=COUNT}",
    	    "{lineNumber=102; busNumber=031156; cardID=000110808691; consumerTime=2012-12-2 16:33:48; consumerCount=2; residualCount=18;  type=COUNT}",
    	    "{lineNumber=185; busNumber=031228; cardID=000110808691; consumerTime=2012-12-2 10:44:08; consumerAmount=1.80; residualAmount=46.40; type=EWALLET}",
    	    "{lineNumber=185; busNumber=031228; cardID=000110808691; consumerTime=2012-12-2 10:44:06; consumerAmount=1.80; residualAmount=48.20; type=EWALLET}",
    	    "{lineNumber=102; busNumber=031153; cardID=000110808691; consumerTime=2012-10-14 19:10:33; consumerAmount=1.80; residualAmount=3.20; type=EWALLET}",
    	    "{lineNumber=50; busNumber=049182; cardID=000110808691; consumerTime=2012-10-3 10:41:41; consumerAmount=1.80; residualAmount=5; type=EWALLET}",
    	    "{lineNumber=50; busNumber=049543; cardID=000110808691; consumerTime=2012-9-29 9:13:45; consumerAmount=1.80; residualAmount=6.80; type=EWALLET}",
    	    "{lineNumber=188; busNumber=031181; cardID=000110808691; consumerTime=2012-9-23 11:51:44; consumerAmount=1.80; residualAmount=12.20; type=EWALLET}",
    	    "{lineNumber=185; busNumber=034006; cardID=000110808691; consumerTime=2012-8-27 10:31:19; consumerAmount=1.80; residualAmount=14; type=EWALLET}",
    	    "{lineNumber=102; busNumber=031158; cardID=000110808691; consumerTime=2012-8-26 16:47:37; consumerAmount=1.80; residualAmount=23; type=EWALLET}",
    	};

    private FinderApplication mApp = null;
    private TrafficService mTrafficService = null;
    private ArrayList<ConsumerRecord> mTestConsumerRecords = null;
    private ConsumptionStore mConsumptionStore = null;

    public ConsumptionTest(Class<FinderApplication> applicationClass) {
        super(applicationClass);
    }

    public ConsumptionTest() {
        super(FinderApplication.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();
        mApp = getApplication();
        mTrafficService = TrafficService.getTrafficService(mApp.getMapManager(), getApplication());
        mConsumptionStore = ConsumptionStore.getDefaultStore(getApplication());
        createTestConsumerRecords();
    }

    private void createTestConsumerRecords() {
    	mTestConsumerRecords = new ArrayList<ConsumerRecord>();
        ConsumerRecord record = null;
        for(int i = 0; i < TEST_DATA.length; i++) {
            record = parseConsumerRecord(TEST_DATA[i]);
            mTestConsumerRecords.add(record);
        }
    }

    public void test_retrieve_consumer_records() {
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
    
    public void test_store_consumer_records() {
    	for(ConsumerRecord record : mTestConsumerRecords) {
    		mConsumptionStore.insertRecord(record);
    	}
    	ArrayList<ConsumerRecord> consumerRecords = mConsumptionStore.getConsumptionRecords(CARD_ID);
    	assertEquals(mTestConsumerRecords.size(), consumerRecords.size());
    	for(int i = 0; i < mTestConsumerRecords.size(); i++) {
    		assertEquals(mTestConsumerRecords.get(i), consumerRecords.get(i));
    	}
    }

    // {lineNumber=102; busNumber=031162; cardID=000110808691; consumerTime=2013-1-1 15:39:33; consumerCount=2; residualCount=30; }
    private ConsumerRecord parseConsumerRecord(String data) {
        String[] properties = data.trim().replaceAll("[{}]", "").split(";");
        HashMap<String, String> propertyHashMap = new HashMap<String, String>();
        for(int i = 0; i < properties.length; i++) {
        	String[] itemEntry = properties[i].trim().split("=");
        	propertyHashMap.put(itemEntry[0], itemEntry[1]);
        }
        
        ConsumerRecord record = null;
        String consumptionType = propertyHashMap.get(KEY_CONSUMPTION_TYPE);
		switch (ConsumerType.valueOf(consumptionType)) {
		case COUNT:
			record = new CountConsumerRecord();
			record.setConsumption(propertyHashMap.get(KEY_CONSUMER_COUNT));
			record.setResidual(propertyHashMap.get(KEY_RESIDUAL_COUNT));
			break;
		case EWALLET:
			record = new EWalletConsumerRecord();
			record.setConsumption(propertyHashMap.get(KEY_CONSUMER_AMOUNT));
			record.setResidual(propertyHashMap.get(KEY_RESIDUAL_AMOUNT));
			break;
		default:
			throw new RuntimeException("Unknown consumption type!!!");
		}
		record.setBusNumber(propertyHashMap.get(KEY_BUS_NUMBER));
		record.setLineNumber(propertyHashMap.get(KEY_LINE_NUMBER));
		record.setCardID(propertyHashMap.get(KEY_CARD_ID));
		record.setConsumerTime(Utils.parseDate(propertyHashMap.get(KEY_CONSUMER_TIME)));
        return record;
    }

    private class TestChargeRecordsListener implements ConsumerRecordsListener {
        private boolean isDone = false;

        public boolean done() {
            return isDone;
        }

        @Override
        public void onSuccess(ConsumptionInfo dataInfo) {
            assertNotNull(dataInfo.getRecordList());
            assertEquals(CONSUMER_RECORD_COUNT, dataInfo.getRecordList().size());
            for(int idx = 0; idx < CONSUMER_RECORD_COUNT; idx++) {
                assertEquals(mTestConsumerRecords.get(idx), dataInfo.getRecordList().get(idx));
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
