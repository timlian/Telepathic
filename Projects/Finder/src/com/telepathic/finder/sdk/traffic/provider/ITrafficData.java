package com.telepathic.finder.sdk.traffic.provider;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public interface ITrafficData {
	
	String AUTHORITY = "com.telepathic.finder.provider";
	/**
	 * 
	 * The kuai xin data
	 */
	interface KuaiXinData {
		
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
			String LAST_UPDATE_TIME = "last_update_time";
		}
		
		class BusCard implements BusCardColumns {
			/**
			 * The content uri of this table
			 */
			public static final Uri CONTENT_URI = Uri.parse("content://com.telepathic.finder.provider/kuaiXinBusCard");
			/**
			 * The MIME type
			 */
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/com.telepathic.finder.provider.kuaiXinBusCard";
			/**
			 * The MIME type
			 */
			public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/com.telepathic.finder.provider.kuaiXinBusCard";
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
		
		class ConsumerRecord implements ConsumerRecordColumns {
			/**
			 * The content uri of this table
			 */
			public static final Uri CONTENT_URI = Uri.parse("content://com.telepathic.finder.provider/kuaiXinConsumerRecord");
			/**
			 * The MIME type
			 */
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/com.telepathic.finder.provider.kuaiXinConsumerRecord";
			/**
			 * The MIME type
			 */
			public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/com.telepathic.finder.provider.kuaiXinConsumerRecord";
			
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
			 *  Bus line number
			 */
			String LINE_NUMBER = "line_number";
			/**
			 * Direction type (up, down, circle)
			 */
			String DIRECTION = "direction";
			/**
			 *  Start time
			 */
			String START_TIME = "start_time";
			/**
			 * End time
			 */
			String END_TIME = "end_time";
			/**
			 * The station names
			 */
			String STATIONS = "stations";
			
			/**
			 * The last update time
			 */
			//String LAST_UPDATE_TIME = "last_update_time";
		}
		
		
		
		class BusRoute implements BusRouteColumns {
			/**
			 * The content uri of this table
			 */
			public static final Uri CONTENT_URI = Uri.parse("content://com.telepathic.finder.provider/kuaiXinBusRoute");
			/**
			 * The MIME type
			 */
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/com.telepathic.finder.provider.kuaiXinBusRoute";
			/**
			 * The MIME type
			 */
			public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/com.telepathic.finder.provider.kuaiXinBusRoute";
		}
		
		interface BusStationColumns extends BaseColumns {
			/**
			 * Bus station name.
			 */
			String NAME = "name";
			/**
			 * The gps number of the bus station.
			 */
			String GPS_NUMBER = "gps_number";
			/**
			 * The last update time
			 */
			String LAST_UPDATE_TIME = "last_update_time";
		}
		
		class BusStation implements BusStationColumns {
			/**
			 * The content uri of this table
			 */
			public static final Uri CONTENT_URI = Uri.parse("content://com.telepathic.finder.provider/kuaiXinBusStation");
			/**
			 * The MIME type
			 */
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/com.telepathic.finder.provider.kuaiXinBusStation";
			/**
			 * The MIME type
			 */
			public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/com.telepathic.finder.provider.kuaiXinBusStation";
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
		
		class BusRouteStation implements BusRouteStationColumns {
			/**
			 * The content uri of this table
			 */
			public static final Uri CONTENT_URI = Uri.parse("content://com.telepathic.finder.provider/kuaiXinBusRouteStation");
			/**
			 * The MIME type
			 */
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/com.telepathic.finder.provider.kuaiXinBusRouteStation";
			/**
			 * The MIME type
			 */
			public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/com.telepathic.finder.provider.kuaiXinBusRouteStation";
		}
		
		class BusStationLines {
			/**
			 * The content uri of this table
			 */
			public static final Uri CONTENT_URI = Uri.parse("content://com.telepathic.finder.provider/kuaiXinBusStationLines");
			/**
			 * The MIME type
			 */
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/com.telepathic.finder.provider.kuaiXinBusStationLines";
			/**
			 * The MIME type
			 */
			public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/com.telepathic.finder.provider.kuaiXinBusStationLines";
		}
		
		interface Performance extends BaseColumns {
			/**
			 * The request name
			 */
			String REQUEST_NAME = "request_name";
			/**
			 * The request id
			 */
			String REQUEST_ID = "request_id";
			/**
			 * The request status
			 */
			String STATUS = "status";
			/**
			 * The request time
			 */
			String TIME = "time";
			/**
			 * The error description
			 */
			String ERROR = "error";
			/**
			 * The retry count
			 */
			String RETRY = "retry";
		}
		
		class NetworkPerformance implements Performance {
			/**
			 * The content uri of this table
			 */
			public static final Uri CONTENT_URI = Uri.parse("content://com.telepathic.finder.provider/kuaiXinPerformance");
			/**
			 * The MIME type
			 */
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/com.telepathic.finder.provider.kuaiXinPerformance";
			/**
			 * The MIME type
			 */
			public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/com.telepathic.finder.provider.kuaiXinPerformance";
		}
		
	}
	
	/**
	 * 
	 * The bai du data
	 */
	interface BaiDuData {
		
		interface BusRouteColumns extends BaseColumns {
			/**
			 *  Bus line number
			 */
			String LINE_NUMBER = "line_number";
			/**
			 *  Bus route uid
			 */
			String UID = "uid";
			/**
			 *  Bus route name
			 */
			String NAME = "name";
			/**
			 * The last update time
			 */
			String LAST_UPDATE_TIME = "last_update_time";
		}
		
		class BusRoute implements BusRouteColumns {
			/**
			 * The content uri of this table
			 */
			public static final Uri CONTENT_URI = Uri.parse("content://com.telepathic.finder.provider/baiDuBusRoute");
			/**
			 * The MIME type
			 */
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/com.telepathic.finder.provider.baiDuBusRoute";
			/**
			 * The MIME type
			 */
			public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/com.telepathic.finder.provider.baiDuBusRoute";
		}
		
		interface BusStationColumns extends BaseColumns {
			/**
			 * Bus station name.
			 */
			String NAME = "name";
			/**
			 * The longitude of the bus station.
			 */
			String LONGITUDE = "longitude";
			/**
			 * The latitude of the bus station.
			 */
			String LATITUDE = "latitude";
		}
		
		class BusStation implements BusStationColumns {
			/**
			 * The content uri of this table
			 */
			public static final Uri CONTENT_URI = Uri.parse("content://com.telepathic.finder.provider/baiDuBusStation");
			/**
			 * The MIME type
			 */
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/com.telepathic.finder.provider.baiDuBusStation";
			/**
			 * The MIME type
			 */
			public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/com.telepathic.finder.provider.baiDuBusStation";
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
		
		class BusRouteStation implements BusRouteStationColumns {
			/**
			 * The content uri of this table
			 */
			public static final Uri CONTENT_URI = Uri.parse("content://com.telepathic.finder.provider/baiDuBusRouteStation");
			/**
			 * The MIME type
			 */
			public static final String CONTENT_TYPE = "vnd.android.cursor.dir/com.telepathic.finder.provider.baiDuBusRouteStation";
			/**
			 * The MIME type
			 */
			public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/com.telepathic.finder.provider.baiDuBusRouteStation";
		}
	}
}
