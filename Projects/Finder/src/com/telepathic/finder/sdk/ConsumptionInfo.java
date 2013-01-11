package com.telepathic.finder.sdk;

import java.util.ArrayList;

public class ConsumptionInfo {
	/**
	 * 剩余次数
	 */
	private String mResidualCount;
	/**
	 * 剩余金额
	 */
	private String mResidualAmount;
	/**
	 * 消费记录
	 */
	private ArrayList<ConsumerRecord> mRecordList;
	
	public String getResidualCount() {
		return mResidualCount;
	}
	
	public void setResidualCount(String residualCount) {
		mResidualCount = residualCount;
	}
	
	public String getResidualAmount() {
		return mResidualAmount;
	}
	
	public void setResidualAmount(String residualAmount) {
		mResidualAmount = residualAmount;
	}
	
	public ArrayList<ConsumerRecord> getRecordList() {
		return mRecordList;
	}
	
	public void setRecordList(ArrayList<ConsumerRecord> recordList) {
		mRecordList = recordList;
	}

}
