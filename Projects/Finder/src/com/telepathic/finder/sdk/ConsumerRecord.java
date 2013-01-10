package com.telepathic.finder.sdk;

import java.util.Date;

import com.telepathic.finder.util.Utils;

/**
 * Note: This class has a natural ordering that is inconsistent with equals.
 *
 * @author Tim.Lian
 *
 */
public abstract class ConsumerRecord implements Comparable<ConsumerRecord> {

    /**
     * 公交路线号
     */
    private String mLineNumber;

    /**
     * 公交车牌号
     */
    private String mBusNumber;

    /**
     * 公交卡号
     */
    private String mCardId;

    /**
     * 消费时间
     */
    private Date mConsumerTime;

    
    public enum ConsumerType {
        COUNT, EWALLET;
    }


    public abstract ConsumerType getType();
    
    public abstract String getConsumption();
    
    public abstract void setConsumption(String consumption);
    
    public abstract String getResidual();
    
    public abstract void setResidual(String residual);
    
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

    @Override
    public boolean equals(Object object) {
    	 if (object == this) {
             return true;
         }
        if(!(object instanceof ConsumerRecord)) {
            return false;
        }
        
        ConsumerRecord record = (ConsumerRecord)object;
        if (getType() != record.getType()) {
            return false;
        }
        if (!mLineNumber.equals(record.mLineNumber)
                || !mBusNumber.equals(mBusNumber)
                || !mCardId.equals(record.getCardId())
                || !mConsumerTime.equals(record.getConsumerTime())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 86;
        result = 31 * result + mLineNumber.hashCode();
        result = 31 * result + mBusNumber.hashCode();
        result = 31 * result + mCardId.hashCode();
        result = 31 * result + mConsumerTime.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Line Number: " + mLineNumber + ", ");
        builder.append("Bus Number: " + mBusNumber + ", ");
        builder.append("Card ID: " + mCardId + ", ");
        builder.append("Consumer Time: " + Utils.formatDate(mConsumerTime) + ", ");
        return builder.toString();
    }

    @Override
    public int compareTo(ConsumerRecord another) {
        return mConsumerTime.compareTo(another.getConsumerTime());
    }

}
