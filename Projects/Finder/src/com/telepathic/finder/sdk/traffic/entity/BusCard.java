package com.telepathic.finder.sdk.traffic.entity;

import java.util.ArrayList;
import java.util.Date;

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
    private Date mLastDate;
    /**
     * The consumer records
     */
    private ArrayList<ConsumerRecord> mConsumerRecords;

    public String getCardNumber() {
        return mCardNumber;
    }

    public void setCardNumber(String cardNumber) {
        mCardNumber = cardNumber;
    }

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

    public Date getLastDate() {
        return mLastDate;
    }

    public void setLastDate(Date date) {
        mLastDate = date;
    }

    public ArrayList<ConsumerRecord> getConsumerRecords() {
        return mConsumerRecords;
    }

    public void setConsumerRecords(ArrayList<ConsumerRecord> consumerRecords) {
        mConsumerRecords = consumerRecords;
    }

}
