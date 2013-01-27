package com.telepathic.finder.sdk.traffic;

import java.util.ArrayList;

public class BusCard {
	/**
	 * The bus card number
	 */
	private String mCardNumber;
	/**
	 * The residual count
	 */
	private String mResidualCount;
	/**
	 * The residual amount
	 */
	private String mResidualAmount;
	/**
	 * The last consumption time
	 */
	private String mLastDate;
	/**
	 * The consumer records
	 */
	private ArrayList<ConsumerRecord> mConsumerRecords;
	
	public String getCardNumber() {
		return mCardNumber;
	}

}
