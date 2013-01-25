package com.telepathic.finder.sdk.traffic.store;

import android.provider.BaseColumns;

public interface ITrafficeStore {
	
	public interface ConsumerRecordsColumns extends BaseColumns {
		/**
		 * The card number.
		 */
		public static final String CARD_NUMBER = "card_number";
		/**
		 * The line number.
		 */
		public static final String LINE_NUMBER = "line_number";
		/**
		 * The bus number.
		 */
		public static final String BUS_NUMBER = "bus_number";
		/**
		 * The consumption date.
		 */
		public static final String DATE = "date";
		/**
		 * The consumption.
		 */
		public static final String CONSUMPTION = "consumption";
		/**
		 * The residual. 
		 */
		public static final String RESIDUAL = "residual";
		/**
		 * The consumption type.
		 */
		public static final String TYPE = "type";
	}
	
	public interface BusRouteColumns extends BaseColumns {
		/**
		 * Direction type (up, down, circle)
		 */
		public static final String DIRECTION = "direction";
		/**
		 *  Bus line number
		 */
		public static final String LINE_NUMBER = "line_number";
		/**
		 *  Departure time
		 */
		public static final String DEPARTURE_TIME = "departure_time";
		/**
		 * Close off time
		 */
		public static final String CLOSE_OFF_TIME = "close_off_time";
	}
	
	public interface BusStationColumns extends BaseColumns {
		/**
		 * Bus station name.
		 */
		public static final String NAME = "name";
		/**
		 * Bus station GPS number.
		 */
		public static final String GPS_NUMBER = "gps_number";
		/**
		 * The longitude of the bus station.
		 */
		public static final String LONGITUDE = "longitude";
		/**
		 * The latitude of the bus station.
		 */
		public static final String LATITUDE = "latitude";
	}
	
	public interface BusRouteStationColumns {
		/**
		 * The bus route identification.
		 */
		public static final String ROUTE_ID = "route_id";
		/**
		 * The bus station identification.
		 */
		public static final String STATION_ID = "station_id";
		/**
		 * The station index in the route.
		 */
		public static final String INDEX = "idx";
	}
	
}
