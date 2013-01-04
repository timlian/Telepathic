package com.telepathic.finder.sdk;

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
    private String mConsumerTime;

    /**
     * 消费次数
     */
    private String mConsumerCount;

    /**
     * 剩余次数
     */
    private String mResidualCount;


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

    public String getConsumerCount() {
        return mConsumerCount;
    }

    public void setConsumerCount(String consumerCount) {
        mConsumerCount = consumerCount;
    }

    public String getResidualCount() {
        return mResidualCount;
    }

    public void setResidualCount(String residualCount) {
        mResidualCount = residualCount;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Line Number: " + mLineNumber + ", ");
        builder.append("Bus Number: " + mBusNumber + ", ");
        builder.append("Card ID: " + mCardId + ", ");
        builder.append("Consumer Time: " + mConsumerTime + ", ");
        builder.append("Consumer Count: " + mConsumerCount + ", ");
        builder.append("Residual Count: " + mResidualCount);
        return builder.toString();
    }

    @Override
    public int compareTo(ConsumerRecord another) {
        return mConsumerTime.compareTo(another.getConsumerTime());
    }

}
