package com.telepathic.finder.sdk;

import com.telepathic.finder.sdk.exception.IllegalConsumerTypeException;

/**
 * Note: This class has a natural ordering that is inconsistent with equals.
 *
 * @author Tim.Lian
 *
 */
public class ConsumerRecord implements Comparable<ConsumerRecord> {
    /**
     * ����·�ߺ�
     */
    private String mLineNumber;

    /**
     * �������ƺ�
     */
    private String mBusNumber;

    /**
     * ��������
     */
    private String mCardId;

    /**
     * ����ʱ��
     */
    private String mConsumerTime;

    /**
     * ���Ѵ���
     */
    private int mConsumerCount;

    /**
     * ���ѽ��
     */
    private float mConsumerAmount;
    
    /**
     * ʣ�����
     */
    private int mResidualCount;
    
    /**
     * ʣ����
     */
    private float mResidualAmount;
    
    /**
     * ��������
     */
    private ConsumerType mConsumerType;
    
    public enum ConsumerType {
    	COUNT, ELECTRONIC_WALLET;
    }


    public String getLineNumber() {
        return mLineNumber;
    }

    public void setLineNumber(String lineNumber) {
        mLineNumber = lineNumber;
    }

    public String getBusNumber() {
        return mBusNumber;
    }

    public void setBusNumber(String busNumber) {
        mBusNumber = busNumber;
    }

    public String getCardId() {
        return mCardId;
    }

    public void setCardId(String cardId) {
        mCardId = cardId;
    }

    public String getConsumerTime() {
        return mConsumerTime;
    }

    public void setConsumerTime(String consumerTime) {
        mConsumerTime = consumerTime;
    }

    public int getConsumerCount() {
    	if (mConsumerType == ConsumerType.ELECTRONIC_WALLET) {
    		throw new IllegalConsumerTypeException();
    	}
        return mConsumerCount;
    }

    public void setConsumerCount(int consumerCount) {
        mConsumerCount = consumerCount;
    }

    public int getResidualCount() {
        return mResidualCount;
    }

    public void setResidualCount(int residualCount) {
        mResidualCount = residualCount;
    }
    
    public float getConsumerAmount() {
    	if (mConsumerType == ConsumerType.COUNT) {
    		throw new IllegalConsumerTypeException();
    	}
    	return mConsumerAmount;
    }
    
    public void setConsumerAmount(float amount) {
    	mConsumerAmount = amount;
    }
    
    public float getResidualAmount() {
    	return mResidualAmount;
    }
    
    public void setResidualAmount(float amount) {
    	mResidualAmount = amount; 
    }
    
    public ConsumerType getConsumerType() {
    	return mConsumerType;
    }
    
    public void setConsumerType(ConsumerType type) {
    	mConsumerType = type;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Line Number: " + mLineNumber + ", ");
        builder.append("Bus Number: " + mBusNumber + ", ");
        builder.append("Card ID: " + mCardId + ", ");
        builder.append("Consumer Time: " + mConsumerTime + ", ");
        builder.append("Consumer Amount: " + mConsumerAmount + ", ");
        builder.append("Residual Amount: " + mResidualAmount);
        return builder.toString();
    }

    @Override
    public int compareTo(ConsumerRecord another) {
        return (0 - mConsumerTime.compareTo(another.getConsumerTime()));
    }

}
