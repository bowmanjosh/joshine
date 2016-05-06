/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine.app.data;

import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Defines table and column names for the weather database.
 */
public class WeatherContract {

  // Normalize all date/times to some kind of UTC Julian day thing.
  @SuppressWarnings("deprecation")
  public static long normalizeDate(long startDate) {
    Time time = new Time();
    time.set(startDate);
    int julianDay = Time.getJulianDay(startDate, time.gmtoff);
    return time.setJulianDay(julianDay);
  }

  public static final class LocationEntry implements BaseColumns {
    public static final String TABLE_NAME = "location";

    // Location string that will be sent in the OWM query
    public static final String COL_LOC_SETTING = "location_setting";

    // Human-readable location string, as returned by the API.
    public static final String COL_CITY_NAME = "city_name";

    // Latitude and longitude. (stored as ???)
    public static final String COL_LATITUDE = "coord_latitude";
    public static final String COL_LONGITUDE = "coord_longitude";
  }

  public static final class WeatherEntry implements BaseColumns {
    public static final String TABLE_NAME = "weather";

    // Column with the foreign key into the location table.
    public static final String COL_LOC_KEY = "location_id";
    // Date, stored as long in milliseconds since the epoch
    public static final String COL_DATE = "date";
    // Weather id as returned by API, to identify the icon to be used
    public static final String COL_WEATHER_ID = "weather_id";

    // Short description of weather, as returned by the API.
    public static final String COL_SHORT_DESC = "short_desc";

    // Min and max temperatures for the day (stored as floats)
    public static final String COL_MIN_TEMP = "min";
    public static final String COL_MAX_TEMP = "max";

    // Humidity is stored as a float representing percentage
    public static final String COL_HUMIDITY = "humidity";

    // Humidity is stored as a float representing percentage
    public static final String COL_PRESSURE = "pressure";

    // Windspeed is stored as a float representing windspeed  mph
    public static final String COL_WIND_SPEED = "wind";

    // Degrees are meteorological degrees (e.g, 0 is north, 180 is south).  Stored as floats.
    public static final String COL_DEGREES = "degrees";
  }
}