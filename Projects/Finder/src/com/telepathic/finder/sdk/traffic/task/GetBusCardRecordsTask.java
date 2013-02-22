package com.telepathic.finder.sdk.traffic.task;

import com.telepathic.finder.sdk.traffic.entity.BusCard;
import com.telepathic.finder.sdk.traffic.request.GetConsumerRecordsRequest;
import com.telepathic.finder.sdk.traffic.request.RequestCallback;
import com.telepathic.finder.sdk.traffic.request.RequestExecutor;

public class GetBusCardRecordsTask extends BaseTask<BusCard> {
	private final String mCardNumber;
	private final int mCount;
	
	public GetBusCardRecordsTask(String cardNumber, int count) {
		super("GetBusCardRecordsTask");
		mCardNumber = cardNumber;
		mCount = count;
	}

	@Override
	protected void doWork() {
		GetConsumerRecordsRequest request = new GetConsumerRecordsRequest(mCardNumber, mCount);
		final TaskResult<BusCard> taskResult = new TaskResult<BusCard>();
		RequestExecutor.execute(request, new RequestCallback() {
			@Override
			public void onError(int errorCode, String errorMessage) {
				taskResult.setErrorCode(errorCode);
				taskResult.setErrorMessage(errorMessage);
				setTaskResult(taskResult);
			}

			@Override
			public void onSuccess(Object result) {
				taskResult.setResult((BusCard)result);
				setTaskResult(taskResult);
			}
		});
	}
   
}
