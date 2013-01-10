package com.telepathic.finder.sdk.network;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import org.ksoap2.serialization.SoapObject;

import com.telepathic.finder.sdk.ConsumerRecord;
import com.telepathic.finder.sdk.ConsumerRecord.ConsumerType;
import com.telepathic.finder.sdk.ConsumerRecordsListener;
import com.telepathic.finder.sdk.CountConsumerRecord;
import com.telepathic.finder.sdk.EWalletConsumerRecord;
import com.telepathic.finder.sdk.store.ConsumptionStore;
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
    private ConsumptionStore mStore;

    public BusConsumerRecordRequest(String cardId, int count, ConsumerRecordsListener listener, ConsumptionStore store) {
        super(METHOD_NAME);
        addParameter(KEY_CARD_ID, cardId);
        addParameter(KEY_COUNT, String.valueOf(count + 1));
        mStore = store;
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
        ConsumerRecord record = null;
        for(int idx = 0; idx < newDataSet.getPropertyCount() ; idx++) {
            dataEntry = (SoapObject) newDataSet.getProperty(idx);
            try {
            	dataEntry.getPrimitivePropertyAsString(KEY_CONSUMER_COUNT);
            	record = new CountConsumerRecord();
            } catch (RuntimeException e) {
            	try {
            		dataEntry.getPrimitivePropertyAsString(KEY_RESIDUAL_AMOUNT);
            		record = new EWalletConsumerRecord();
            	} catch (RuntimeException ex) {
            		throw new RuntimeException("Unknown consumer record structure!!!");
            	}
            }
            record.setLineNumber(dataEntry.getPrimitivePropertyAsString(KEY_LINE_NUMBER));
            record.setBusNumber(dataEntry.getPrimitivePropertyAsString(KEY_BUS_NUMBER));
            record.setCardID(dataEntry.getPrimitivePropertyAsString(KEY_CARD_ID));
            try {
                final Date consumerDate = Utils.parseDate(dataEntry.getPrimitivePropertyAsString(KEY_CONSUMER_TIME));
                record.setConsumerTime(consumerDate);
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
            if (record.getType() == ConsumerType.COUNT) {
            	record.setConsumption(dataEntry.getPrimitivePropertyAsString(KEY_CONSUMER_COUNT));
                record.setResidual(dataEntry.getPrimitivePropertyAsString(KEY_RESIDUAL_COUNT));
                if (lastRecord != null) {
                    record.setResidual(String.valueOf(lastRecord.getResidual()));
                    if(lastRecord.getType() != record.getType()) {
                    	updateResidual(consumerRecords, startPos, endPos, record.getResidual());
                        final float amount = Float.valueOf(lastRecord.getConsumption()) + Float.valueOf(lastRecord.getResidual());
                        record.setResidual(String.valueOf(amount));
                        startPos = endPos;
                    }
                }
                endPos++;
            }
            if (record.getType() == ConsumerType.EWALLET) {
            	 record.setConsumption(dataEntry.getPrimitivePropertyAsString(KEY_CONSUMER_AMOUNT));
                 record.setResidual(dataEntry.getPrimitivePropertyAsString(KEY_RESIDUAL_AMOUNT));
                 if (lastRecord != null) {
                     record.setResidual(String.valueOf(lastRecord.getResidual()));
                     if (lastRecord.getType() != record.getType()) {
                    	 updateResidual(consumerRecords, startPos, endPos, record.getResidual());
                         final int count = Integer.valueOf(lastRecord.getConsumption()) + Integer.valueOf(lastRecord.getResidual());
                         record.setResidual(String.valueOf(count));
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
        
        for (ConsumerRecord consumerRecord : consumerRecords) {
        	mStore.insertRecord(consumerRecord);
        }
    }

	private static void updateResidual(ArrayList<ConsumerRecord> records,
			int start, int end, String residualCount) {
		ConsumerRecord record = null;
		for (int i = start; i < end; i++) {
			record = records.get(i);
			record.setResidual(residualCount);
		}
	}

}
