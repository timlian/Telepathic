package com.telepathic.finder.sdk.store;

import android.provider.BaseColumns;

public class Store {
	
	private Store() { }
	
	public interface ConsumptionColumns extends BaseColumns {
		/**
		 * 卡号
		 */
		public static final String CARD_ID = "card_id";
		/**
		 * 公交线路号
		 */
		public static final String BUS_LINE_NUMBER = "bus_number";
		/**
		 * 车牌号
		 */
		public static final String LICENSE_PLATE_NUMBER = "license_plate_number";
		/**
		 * 消费时间
		 */
		public static final String CONSUMPTION_TIME = "consumption_time";
		/**
		 * 消费
		 */
		public static final String CONSUMPTION = "consumption";
		/**
		 * 剩余 
		 */
		public static final String RESIDUAL = "residual";
		/**
		 * 消费类型
		 */
		public static final String CONSUMPTION_TYPE = "consumption_type";
	}
	
}
