package com.telepathic.finder.sdk;

public class EWalletConsumerRecord extends ConsumerRecord {
	 /**
     * 消费金额
     */
    private String mConsumerAmount;
    /**
     * 剩余金额
     */
    private String mResidualAmount;
    
	@Override
	public ConsumerType getType() {
		return ConsumerType.EWALLET;
	}

	@Override
	public String getConsumption() {
		return mConsumerAmount;
	}

	@Override
	public void setConsumption(String consumption) {
		mConsumerAmount = consumption;
	}

	@Override
	public String getResidual() {
		return mResidualAmount;
	}

	@Override
	public void setResidual(String residual) {
		mResidualAmount = residual;
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}
		if (!(object instanceof EWalletConsumerRecord)) {
			return false;
		}
		EWalletConsumerRecord record = (EWalletConsumerRecord) object;
		return super.equals(object) && mConsumerAmount.equals(record.mConsumerAmount)
									&& mResidualAmount.equals(record.mResidualAmount);
	}
	
	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + mConsumerAmount.hashCode();
		result = 31 * result + mResidualAmount.hashCode();
		return  result;
	}

}
