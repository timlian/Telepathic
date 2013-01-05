package com.telepathic.finder.sdk;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

import com.telepathic.finder.sdk.exception.IllegalConsumerTypeException;

/**
 * Note: This class has a natural ordering that is inconsistent with equals.
 *
 * @author Tim.Lian
 *
 */
public class ConsumerRecord implements Comparable<ConsumerRecord> {
	
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
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
    private Date mConsumerTime;

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

    public void setCardID(String cardId) {
        mCardId = cardId;
    }

    public Date getConsumerTime() {
        return mConsumerTime;
    }

    public void setConsumerTime(Date consumerTime) {
    	mConsumerTime = consumerTime;
    }

    public int getConsumerCount() {
    	if (mConsumerType == ConsumerType.ELECTRONIC_WALLET) {
    		throw new IllegalConsumerTypeException();
    	}
        return mConsumerCount;
    }

    public void setConsumerCount(String consumerCount) {
        mConsumerCount = Integer.parseInt(consumerCount);
    }

    public int getResidualCount() {
        return mResidualCount;
    }

    public void setResidualCount(String residualCount) {
        mResidualCount = Integer.parseInt(residualCount);
    }
    
    public float getConsumerAmount() {
    	if (mConsumerType == ConsumerType.COUNT) {
    		throw new IllegalConsumerTypeException();
    	}
    	return mConsumerAmount;
    }
    
    public void setConsumerAmount(String amount) {
    	mConsumerAmount = Float.parseFloat(amount);
    }
    
    public float getResidualAmount() {
    	return mResidualAmount;
    }
    
    public void setResidualAmount(String amount) {
    	mResidualAmount = Float.parseFloat(amount); 
    }
    
    public ConsumerType getConsumerType() {
    	return mConsumerType;
    }
    
    public void setConsumerType(ConsumerType type) {
    	mConsumerType = type;
    }

    @Override
    public boolean equals(Object object) {
    	if (object == null) {
    		return false;
    	}
    	if(!(object instanceof ConsumerRecord)) {
    		return false;
    	}
    	ConsumerRecord record = (ConsumerRecord)object;
    	if (this == record) {
    		return true;
    	}
    	if (mConsumerType != record.getConsumerType()) {
    		return false;
    	}
		if (!mLineNumber.equals(record.mLineNumber)
				|| !mBusNumber.equals(mBusNumber)
				|| !mCardId.equals(record.getCardId())
				|| !mConsumerTime.equals(record.getConsumerTime())) {
			return false;
		}
		
		if (mResidualCount != record.getResidualCount()
				|| mResidualAmount != record.getResidualAmount()) {
			return false;
		}
		
		if (mConsumerType == ConsumerType.COUNT) {
			if (mConsumerCount != record.getConsumerCount()) {
				return false;
			}
		} else if (mConsumerType == ConsumerType.ELECTRONIC_WALLET) {
			if (mConsumerAmount != record.getConsumerAmount()) {
				return false;
			}
		}
		
		return true;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Line Number: " + mLineNumber + ", ");
        builder.append("Bus Number: " + mBusNumber + ", ");
        builder.append("Card ID: " + mCardId + ", ");
        builder.append("Consumer Time: " + DATE_FORMAT.format(mConsumerTime) + ", ");
        builder.append("Consumer Amount: " + mConsumerAmount + ", ");
        builder.append("Residual Amount: " + mResidualAmount + ", ");
        builder.append("Residual Count: " + mResidualCount);
        return builder.toString();
    }

    @Override
    public int compareTo(ConsumerRecord another) {
        return mConsumerTime.compareTo(another.getConsumerTime());
    }

}
