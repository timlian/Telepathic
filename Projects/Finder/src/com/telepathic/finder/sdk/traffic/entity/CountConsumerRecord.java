package com.telepathic.finder.sdk.traffic.entity;

public class CountConsumerRecord extends ConsumerRecord {
	 /**
     * 消费次数
     */
    private String mConsumerCount;
    /**
     * 剩余次数
     */
    private String mResidualCount;
    
    @Override
    public ConsumerType getType() {
    	return ConsumerType.COUNT;
    }
    
	@Override
	public String getConsumption() {
		return mConsumerCount;
	}

	@Override
	public void setConsumption(String consumption) {
		mConsumerCount = consumption;
	}

	@Override
	public String getResidual() {
		return mResidualCount;
	}

	@Override
	public void setResidual(String residual) {
		mResidualCount = residual;
	}
	
	@Override
	public boolean equals(Object object) {
		if(object == this) {
			return true;
		}
		if (!(object instanceof CountConsumerRecord)) {
			return false;
		}
		CountConsumerRecord record = (CountConsumerRecord) object;
		return super.equals(object)&& mConsumerCount.equals(record.mConsumerCount)
				                   && mResidualCount.equals(record.mResidualCount);
	}
	
	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + mConsumerCount.hashCode();
		result = 31 * result + mResidualCount.hashCode();
		return result;
	}
}
