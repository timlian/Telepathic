package com.telepathic.finder.test.sdk.traffic.request;

import java.util.ArrayList;

import android.test.ApplicationTestCase;
import android.util.Log;

import com.telepathic.finder.app.FinderApplication;
import com.telepathic.finder.sdk.traffic.entity.BusCard;
import com.telepathic.finder.sdk.traffic.entity.ConsumerRecord;
import com.telepathic.finder.sdk.traffic.entity.CountConsumerRecord;
import com.telepathic.finder.sdk.traffic.entity.EWalletConsumerRecord;
import com.telepathic.finder.sdk.traffic.request.GetConsumerRecordsRequest;
import com.telepathic.finder.sdk.traffic.request.RequestCallback;
import com.telepathic.finder.sdk.traffic.request.RequestExecutor;
import com.telepathic.finder.util.Utils;

public class GetConsumerRecordsRequestTest extends ApplicationTestCase<FinderApplication> {
    private static final String TAG = "GetConsumerRecordsRequestTest";

    private BusCard mExpectedBusCard;

    public GetConsumerRecordsRequestTest(Class<FinderApplication> applicationClass) {
        super(applicationClass);
    }

    public GetConsumerRecordsRequestTest() {
        super(FinderApplication.class);
    }

    @Override
    protected void setUp() throws Exception {
        mExpectedBusCard = new BusCard();
        mExpectedBusCard.setResidualAmount("20.80");
        mExpectedBusCard.setResidualCount("0");
        mExpectedBusCard.setCardNumber("01545529");

        ArrayList<ConsumerRecord> consumerRecords = new ArrayList<ConsumerRecord>();
        EWalletConsumerRecord record1 = new EWalletConsumerRecord();
        record1.setBusNumber("031154");
        record1.setCardID("000101545529");
        record1.setConsumerTime(Utils.parseDate("2013-1-1 19:24:01"));
        record1.setConsumption("1.80");
        record1.setResidual("20.80");
        record1.setLineNumber("102");

        EWalletConsumerRecord record2 = new EWalletConsumerRecord();
        record2.setBusNumber("031162");
        record2.setCardID("000101545529");
        record2.setConsumerTime(Utils.parseDate("2013-1-1 15:39:34"));
        record2.setConsumption("1.80");
        record2.setResidual("22.60");
        record2.setLineNumber("102");

        CountConsumerRecord record3 = new CountConsumerRecord();
        record3.setLineNumber("102");
        record3.setBusNumber("031164");
        record3.setCardID("000101545529");
        record2.setConsumerTime(Utils.parseDate("2012-6-30 21:39:51"));
        record2.setConsumption("2");
        record2.setResidual("6");

        mExpectedBusCard.setConsumerRecords(consumerRecords);

        super.setUp();
    }

    public void test_retrieveConsumerRecords() {
        GetConsumerRecordsRequest request = new GetConsumerRecordsRequest("01545529", 10);
        RequestExecutor.execute(request, new RequestCallback() {

            @Override
            public void onSuccess(Object result) {
                BusCard card = (BusCard) result;
                assertNotNull(card);
                assertEquals(mExpectedBusCard.getCardNumber(), card.getCardNumber());
                assertEquals(mExpectedBusCard.getResidualAmount(), card.getResidualAmount());
                assertEquals(mExpectedBusCard.getResidualCount(), card.getResidualCount());
                assertEquals(10, card.getConsumerRecords().size());
                ArrayList<ConsumerRecord> actualRecords = card.getConsumerRecords();
                ArrayList<ConsumerRecord> expectedRecords = mExpectedBusCard.getConsumerRecords();
                for(int i = 0; i < expectedRecords.size(); i++) {
                    assertEquals(expectedRecords.get(i).getLineNumber(), actualRecords.get(i).getLineNumber());
                    assertEquals(expectedRecords.get(i).getBusNumber(), actualRecords.get(i).getBusNumber());
                    assertEquals(expectedRecords.get(i).getCardId(), actualRecords.get(i).getCardId());
                    assertEquals(expectedRecords.get(i).getConsumerTime(), actualRecords.get(i).getConsumerTime());
                    assertEquals(expectedRecords.get(i).getConsumption(), actualRecords.get(i).getConsumption());
                    assertEquals(expectedRecords.get(i).getResidual(), actualRecords.get(i).getResidual());
                }
            }

            @Override
            public void onError(int errorCode, String errorMessage) {
                Log.d(TAG, "get consumer records failed: error = " + errorCode + ", caused by " + errorMessage);
                // This card has no valid data now, and we can't find a new non-use card. so if the errorCode is -3 it is treated as this interface works fine.
                assertEquals(-3, errorCode);
                assertTrue(false);
            }
        });
    }
}
