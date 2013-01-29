package com.telepathic.finder.sdk.traffic.provider;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public interface ITrafficData {
	
	String AUTHORITY = "com.telepathic.finder.provider";
	
	interface BusCardColumns extends BaseColumns {
		/**
		 * The card number.
		 */
		String CARD_NUMBER = "card_number";
		/**
		 * The residual count
		 */
		String RESIDUAL_COUNT = "residual_count";
		/**
		 * The residual amount
		 */
		String RESIDUAL_AMOUNT = "residual_amount";
		/**
		 * The last consumption date
		 */
		String LAST_DATE = "last_date";
	}
	
	static class BusCard implements BusCardColumns {
		/**
		 * The content uri of this table
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://com.telepathic.finder.provider/busCard");
		/**
		 * The MIME type
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/com.telepathic.finder.provider.busCard";
		/**
		 * The MIME type
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/com.telepathic.finder.provider.busCard";
	}
	
	interface ConsumerRecordColumns extends BaseColumns {
		/**
		 * The card identification.
		 */
		String CARD_ID = "card_id";
		/**
		 * The line number.
		 */
		String LINE_NUMBER = "line_number";
		/**
		 * The bus number.
		 */
		String BUS_NUMBER = "bus_number";
		/**
		 * The consumption date.
		 */
		String DATE = "date";
		/**
		 * The consumption.
		 */
		String CONSUMPTION = "consumption";
		/**
		 * The residual. 
		 */
		String RESIDUAL = "residual";
		/**
		 * The consumption type.
		 */
		String TYPE = "type";
	}
	
	static class ConsumerRecord implements ConsumerRecordColumns {
		/**
		 * The content uri of this table
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://com.telepathic.finder.provider/consumerRecord");
		/**
		 * The MIME type
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/com.telepathic.finder.provider.consumerRecord";
		/**
		 * The MIME type
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/com.telepathic.finder.provider.consumerRecord";
		
		/**
		 * Get the consumer records uri according to the specified card numer.
		 * 
		 * @param cardNumber The card number
		 * @return
		 */
		public static final Uri getConsumerRecordUriByCardNumber(long cardNumber) {
            Uri.Builder builder = CONTENT_URI.buildUpon();
            ContentUris.appendId(builder, cardNumber);
            return builder.build();
        }
	}
	
	interface BusRouteColumns extends BaseColumns {
		/**
		 * Direction type (up, down, circle)
		 */
		String DIRECTION = "direction";
		/**
		 *  Bus line number
		 */
		String LINE_NUMBER = "line_number";
		/**
		 *  Departure time
		 */
		String DEPARTURE_TIME = "departure_time";
		/**
		 * Close off time
		 */
		String CLOSE_OFF_TIME = "close_off_time";
	}
	
	static class BusRoute implements BusRouteColumns {
		/**
		 * The content uri of this table
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://com.telepathic.finder.provider/busRoute");
		/**
		 * The MIME type
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/com.telepathic.finder.provider.busRoute";
		/**
		 * The MIME type
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/com.telepathic.finder.provider.busRoute";
	}
	
	interface BusStationColumns extends BaseColumns {
		/**
		 * Bus station name.
		 */
		String NAME = "name";
		/**
		 * Bus station GPS number.
		 */
		String GPS_NUMBER = "gps_number";
		/**
		 * The longitude of the bus station.
		 */
		String LONGITUDE = "longitude";
		/**
		 * The latitude of the bus station.
		 */
		String LATITUDE = "latitude";
	}
	
	static class BusStation implements BusStationColumns {
		/**
		 * The content uri of this table
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://com.telepathic.finder.provider/busStation");
		/**
		 * The MIME type
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/com.telepathic.finder.provider.busStation";
		/**
		 * The MIME type
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/com.telepathic.finder.provider.busStation";
	}
	
	interface BusRouteStationColumns {
		/**
		 * The bus route identification.
		 */
		String ROUTE_ID = "route_id";
		/**
		 * The bus station identification.
		 */
		String STATION_ID = "station_id";
		/**
		 * The station index in the route.
		 */
		String INDEX = "idx";
	}
	
	static class BusRouteStation implements BusRouteStationColumns {
		/**
		 * The content uri of this table
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://com.telepathic.finder.provider/busRouteStation");
		/**
		 * The MIME type
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/com.telepathic.finder.provider.busRouteStation";
		/**
		 * The MIME type
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/com.telepathic.finder.provider.busRouteStation";
	}

}
