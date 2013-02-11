package com.telepathic.finder.sdk.traffic.task;

public class TaskResult<Result> {
	/**
	 * An error code
	 */
	private int mErrorCode;
	/**
	 * The error message
	 */
	private String mErrorMessage;
	/**
	 * The actual result
	 */
	private Result mResult;
	
	public int getErrorCode() {
		return mErrorCode;
	}
	
	public void setErrorCode(int errorCode) {
		mErrorCode = errorCode;
	}
	
	public String getErrorMessage() {
		return mErrorMessage;
	}
	
	public void setErrorMessage(String errorMessage) {
		mErrorMessage = errorMessage;
	}
	
	public Result getResult() {
		return mResult;
	}
	
	public void setResult(Result result) {
		mResult = result;
	}

}
