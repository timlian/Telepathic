package com.telepathic.finder.sdk;

import java.util.Date;

import com.telepathic.finder.sdk.exception.IllegalConsumerTypeException;
import com.telepathic.finder.util.Utils;

/**
 * Note: This class has a natural ordering that is inconsistent with equals.
 *
 * @author Tim.Lian
 *
 */
public class ConsumerRecord implements Comparable<ConsumerRecord> {

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

    /**
     * 消费次数
     */
    private int mConsumerCount;

    /**
     * 消费金额
     */
    private float mConsumerAmount;

    /**
     * 剩余次数
     */
    private int mResidualCount;

    /**
     * 剩余金额
     */
    private float mResidualAmount;

    /**
     * 消费类型
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
                || Float.compare(mResidualAmount, record.getResidualAmount()) != 0) {
            return false;
        }

        if (mConsumerType == ConsumerType.COUNT) {
            if (mConsumerCount != record.getConsumerCount()) {
                return false;
            }

        } else if (mConsumerType == ConsumerType.ELECTRONIC_WALLET) {
            if (Float.compare(mConsumerAmount,  record.getConsumerAmount()) != 0) {
                return false;
            }
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
        if (mConsumerType == ConsumerType.COUNT) {
            result =  31 * result + mConsumerCount;
        }
        if (mConsumerType == ConsumerType.ELECTRONIC_WALLET) {
            result = 31 * result + Float.floatToIntBits(mConsumerAmount);
        }
        result = 31 * result + mResidualCount;
        result = 31 * result + Float.floatToIntBits(mResidualAmount);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Line Number: " + mLineNumber + ", ");
        builder.append("Bus Number: " + mBusNumber + ", ");
        builder.append("Card ID: " + mCardId + ", ");
        builder.append("Consumer Time: " + Utils.formatDate(mConsumerTime) + ", ");
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
