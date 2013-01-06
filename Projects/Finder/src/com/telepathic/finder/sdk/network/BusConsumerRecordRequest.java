package com.telepathic.finder.sdk.network;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import org.ksoap2.serialization.SoapObject;

import com.telepathic.finder.sdk.ConsumerRecord;
import com.telepathic.finder.sdk.ConsumerRecord.ConsumerType;
import com.telepathic.finder.sdk.ConsumerRecordsListener;
import com.telepathic.finder.util.Utils;

public class BusConsumerRecordRequest extends RPCRequest {
    private static final String METHOD_NAME = "getConsumerRecords";
    private static final String RESPONSE_NAME = "getConsumerRecordsResult";

    private static final String KEY_CARD_ID = "cardID";
    private static final String KEY_COUNT = "count";

    // consumer records constant keys
    private static final String KEY_LINE_NUMBER    = "lineNumber";
    private static final String KEY_BUS_NUMBER     = "busNumber";
    private static final String KEY_CONSUMER_TIME  = "consumerTime";
    private static final String KEY_CONSUMER_COUNT = "consumerCount";
    private static final String KEY_RESIDUAL_COUNT = "residualCount";
    private static final String KEY_CONSUMER_AMOUNT = "consumerAmount";
    private static final String KEY_RESIDUAL_AMOUNT = "residualAmount";


    private ConsumerRecordsListener mListener;

    public BusConsumerRecordRequest(String cardId, int count, ConsumerRecordsListener listener) {
        super(METHOD_NAME);
        addParameter(KEY_CARD_ID, cardId);
        addParameter(KEY_COUNT, String.valueOf(count + 1));
        mListener = listener;
    }

    @Override
    protected String getResponseName() {
        return RESPONSE_NAME;
    }

    @Override
    protected void handleError(String errorMessage) {
        if (mListener != null) {
            mListener.onError(errorMessage);
        }
    }

    /*
     * Consumer records response data entry example:
     *
     * {lineNumber=102; busNumber=031164; cardID=000101545529; consumerTime=2012-6-30 21:39:51; consumerCount=2; residualCount=6; code=200; msg=�ɹ�; }
     *
     */
    @Override
    protected void handleResponse(SoapObject newDataSet) {
        SoapObject dataEntry = null;
        ArrayList<ConsumerRecord> consumerRecords = new ArrayList<ConsumerRecord>();
        int startPos = 0, endPos = 0;
        ConsumerRecord lastRecord = null;
        for(int idx = 0; idx < newDataSet.getPropertyCount() ; idx++) {
            dataEntry = (SoapObject) newDataSet.getProperty(idx);
            ConsumerRecord record = new ConsumerRecord();
            record.setLineNumber(dataEntry.getPrimitivePropertyAsString(KEY_LINE_NUMBER));
            record.setBusNumber(dataEntry.getPrimitivePropertyAsString(KEY_BUS_NUMBER));
            record.setCardID(dataEntry.getPrimitivePropertyAsString(KEY_CARD_ID));
            try {
                final Date consumerDate = Utils.parseDate(dataEntry.getPrimitivePropertyAsString(KEY_CONSUMER_TIME));
                record.setConsumerTime(consumerDate);
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
            try {
                record.setConsumerCount(dataEntry.getPrimitivePropertyAsString(KEY_CONSUMER_COUNT));
                record.setResidualCount(dataEntry.getPrimitivePropertyAsString(KEY_RESIDUAL_COUNT));
                record.setConsumerType(ConsumerType.COUNT);
                if (lastRecord != null) {
                    record.setResidualAmount(String.valueOf(lastRecord.getResidualAmount()));
                    if(lastRecord.getConsumerType() != record.getConsumerType()) {
                        final int residualCount = record.getResidualCount();
                        updateResidualCount(consumerRecords, startPos, endPos, String.valueOf(residualCount));
                        final float amount = lastRecord.getConsumerAmount() + lastRecord.getResidualAmount();
                        record.setResidualAmount(String.valueOf(amount));
                        startPos = endPos;
                    }
                }
                endPos++;
            } catch (RuntimeException e) {
                record.setConsumerAmount(dataEntry.getPrimitivePropertyAsString(KEY_CONSUMER_AMOUNT));
                record.setResidualAmount(dataEntry.getPrimitivePropertyAsString(KEY_RESIDUAL_AMOUNT));
                record.setConsumerType(ConsumerType.ELECTRONIC_WALLET);
                if (lastRecord != null) {
                    record.setResidualCount(String.valueOf(lastRecord.getResidualCount()));
                    if (lastRecord.getConsumerType() != record.getConsumerType()) {
                        final float amount = record.getResidualAmount();
                        updateResidualAmount(consumerRecords, startPos, endPos, String.valueOf(amount));
                        final int count = lastRecord.getConsumerCount() + lastRecord.getResidualCount();
                        record.setResidualCount(String.valueOf(count));
                        startPos = endPos;
                    }
                }
                endPos++;
            }
            lastRecord = record;
            consumerRecords.add(record);
        }

        if (mListener != null) {
            mListener.onSuccess(consumerRecords);
        }
    }

    private static void updateResidualCount(ArrayList<ConsumerRecord> records, int start, int end, String residualCount) {
        ConsumerRecord record = null;
        for(int i = start; i < end; i++) {
            record = records.get(i);
            record.setResidualCount(residualCount);
        }
    }

    private static void updateResidualAmount(ArrayList<ConsumerRecord> records, int start, int end, String amount) {
        ConsumerRecord record = null;
        for(int i = start; i < end; i++) {
            record = records.get(i);
            record.setResidualAmount(amount);
        }
    }

}
