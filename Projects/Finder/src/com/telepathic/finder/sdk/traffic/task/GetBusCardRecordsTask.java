package com.telepathic.finder.sdk.traffic.task;

import java.util.ArrayList;

import org.ksoap2.serialization.SoapObject;

import com.telepathic.finder.sdk.traffic.entity.BusCard;
import com.telepathic.finder.sdk.traffic.entity.ConsumerRecord;
import com.telepathic.finder.sdk.traffic.entity.CountConsumerRecord;
import com.telepathic.finder.sdk.traffic.entity.EWalletConsumerRecord;
import com.telepathic.finder.util.Utils;

public class GetBusCardRecordsTask extends BaseTask<BusCard> {
	private final String mCardNumber;
	private final int mCount;
	private BusCard mBusCard;
	private int mErrorCode;
	private String mErrorMessage;
	
	public GetBusCardRecordsTask(String cardNumber, int count) {
		super("GetBusCardRecordsTask");
		mCardNumber = cardNumber;
		mCount = count;
	}

	@Override
	protected void doWork() {
		NetworkManager.execute(new GetConsumerRecordsRequest());
		TaskResult<BusCard> result = new TaskResult<BusCard>();
		result.setErrorCode(mErrorCode);
		result.setErrorMessage(mErrorMessage);
		result.setResult(mBusCard);
		setTaskResult(result);
	}
	
    private class GetConsumerRecordsRequest extends RPCBaseRequest {
		private static final String METHOD_NAME = "getConsumerRecords";
		// consumer records constant keys
		private static final String KEY_CARD_ID = "cardID";
		private static final String KEY_COUNT = "count";
		private static final String KEY_LINE_NUMBER = "lineNumber";
		private static final String KEY_BUS_NUMBER = "busNumber";
		private static final String KEY_CONSUMER_TIME = "consumerTime";
		private static final String KEY_CONSUMER_COUNT = "consumerCount";
		private static final String KEY_RESIDUAL_COUNT = "residualCount";
		private static final String KEY_CONSUMER_AMOUNT = "consumerAmount";
		private static final String KEY_RESIDUAL_AMOUNT = "residualAmount";

		GetConsumerRecordsRequest() {
			super(METHOD_NAME);
			addParameter(KEY_CARD_ID, mCardNumber);
			addParameter(KEY_COUNT, String.valueOf(mCount + 1));
		}

		@Override
		void handleError(int errorCode, String errorMessage) {
			mErrorCode = errorCode;
			mErrorMessage = errorMessage;
		}

		/*
		 * Consumer records response data entry example:
		 * 
		 * {lineNumber=102; busNumber=031164; cardID=000101545529;
		 * consumerTime=2012-6-30 21:39:51; consumerCount=2; residualCount=6;
		 * code=200; msg=成功; }
		 */
		@Override
		void handleResponse(SoapObject newDataSet) {
			SoapObject dataEntry = null;
			ArrayList<ConsumerRecord> consumerRecords = new ArrayList<ConsumerRecord>();
			ConsumerRecord record = null;
			mBusCard = new BusCard();
			int count = newDataSet.getPropertyCount();
			if (count > 0) {
				String cardId = ((SoapObject) newDataSet.getProperty(0))
						.getPropertyAsString(KEY_CARD_ID);
				mBusCard.setCardNumber(cardId.substring(4));
			}
			for (int idx = 0; idx < count; idx++) {
				dataEntry = (SoapObject) newDataSet.getProperty(idx);
				try {
					dataEntry.getPrimitivePropertyAsString(KEY_CONSUMER_COUNT);
					record = new CountConsumerRecord();
				} catch (RuntimeException e) {
					try {
						dataEntry
								.getPrimitivePropertyAsString(KEY_RESIDUAL_AMOUNT);
						record = new EWalletConsumerRecord();
					} catch (RuntimeException ex) {
						throw new RuntimeException(
								"Unknown consumer record structure!!!");
					}
				}
				record.setLineNumber(dataEntry
						.getPrimitivePropertyAsString(KEY_LINE_NUMBER));
				record.setBusNumber(dataEntry
						.getPrimitivePropertyAsString(KEY_BUS_NUMBER));
				record.setCardID(dataEntry
						.getPrimitivePropertyAsString(KEY_CARD_ID));
				record.setConsumerTime(Utils.parseDate(dataEntry
						.getPrimitivePropertyAsString(KEY_CONSUMER_TIME)));
				switch (record.getType()) {
				case COUNT:
					record.setConsumption(dataEntry
							.getPrimitivePropertyAsString(KEY_CONSUMER_COUNT));
					record.setResidual(dataEntry
							.getPrimitivePropertyAsString(KEY_RESIDUAL_COUNT));
					if (mBusCard.getResidualCount() == null) {
						mBusCard.setResidualCount(dataEntry
								.getPrimitivePropertyAsString(KEY_RESIDUAL_COUNT));
					}
					break;
				case EWALLET:
					record.setConsumption(dataEntry
							.getPrimitivePropertyAsString(KEY_CONSUMER_AMOUNT));
					record.setResidual(dataEntry
							.getPrimitivePropertyAsString(KEY_RESIDUAL_AMOUNT));
					if (mBusCard.getResidualAmount() == null) {
						mBusCard.setResidualAmount(dataEntry
								.getPrimitivePropertyAsString(KEY_RESIDUAL_AMOUNT));
					}
					break;
				default:
					throw new RuntimeException("Unknown consumer type !!!");
				}
				consumerRecords.add(record);
			}
			mBusCard.setConsumerRecords(consumerRecords);
		}
	}

}
