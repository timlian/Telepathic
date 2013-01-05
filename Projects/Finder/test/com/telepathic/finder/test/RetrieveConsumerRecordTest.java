package com.telepathic.finder.test;

import java.util.ArrayList;

import com.telepathic.finder.app.FinderApplication;
import com.telepathic.finder.sdk.ChargeRecordsListener;
import com.telepathic.finder.sdk.ConsumerRecord;
import com.telepathic.finder.sdk.TrafficService;

import android.test.ApplicationTestCase;
import android.util.Log;

public class RetrieveConsumerRecordTest extends ApplicationTestCase<FinderApplication> {
	private static final String TAG = "RetrieveConsumerRecordTest";
	
	private static final int CONSUMER_RECORD_COUNT = 30;
	private static final String CARD_ID = "10808691";
	
	private FinderApplication mApp = null;
	private TrafficService mTrafficService = null;
	
	public RetrieveConsumerRecordTest(Class<FinderApplication> applicationClass) {
		super(applicationClass);
	}

	public RetrieveConsumerRecordTest() {
		super(FinderApplication.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		createApplication();
		mApp = getApplication();
		mTrafficService = TrafficService.getTrafficService(mApp.getMapManager());
		super.setUp();
	}

	public void test_retrieve_consumer_records() { 
		TestChargeRecordsListener testChargeRecordsListener = new TestChargeRecordsListener();
		mTrafficService.getChargeRecords(CARD_ID, CONSUMER_RECORD_COUNT, testChargeRecordsListener);
		while(!testChargeRecordsListener.done()) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private class TestChargeRecordsListener implements ChargeRecordsListener {
		private boolean isDone = false;
		
		public boolean done() {
			return isDone;
		}
		
		@Override
		public void onSuccess(ArrayList<ConsumerRecord> consumerRecords) {
			assertNotNull(consumerRecords);
			assertEquals(CONSUMER_RECORD_COUNT, consumerRecords.size());
			for(ConsumerRecord record : consumerRecords) {
				Log.d(TAG, record.toString());
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
