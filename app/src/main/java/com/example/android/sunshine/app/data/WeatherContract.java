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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Defines table and column names for the weather database.
 */
public class WeatherContract {

  // Boilerplate constants from Google at the beginning of the
  // ContentProvider section.
  static final String CONTENT_AUTHORITY =
      "com.example.android.sunshine.app";
  private static final Uri BASE_CONTENT_URI = Uri
      .parse("content://" + CONTENT_AUTHORITY);
  static final String PATH_WEATHER = "weather";
  static final String PATH_LOCATION = "location";

  // Normalize all date/times to some kind of UTC Julian day thing.
  @SuppressWarnings("deprecation")
  static long normalizeDate(long startDate) {
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

    // Boilerplate from Google re: ContentProviders.
    public static final Uri CONTENT_URI =
        BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATION).build();

    static final String CONTENT_TYPE = ContentResolver
        .CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;
    public static final String CONTENT_ITEM_TYPE = ContentResolver
        .CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LOCATION;

    static Uri buildLocationUri(long id) {
      return ContentUris.withAppendedId(CONTENT_URI, id);
    }
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

    // Degrees are meteorological degrees. Stored as floats.
    public static final String COL_DEGREES = "degrees";

    // Boilerplate from Google re: ContentProviders.
    public static final Uri CONTENT_URI =
        BASE_CONTENT_URI.buildUpon().appendPath(PATH_WEATHER).build();

    static final String CONTENT_TYPE = ContentResolver
        .CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;
    static final String CONTENT_ITEM_TYPE = ContentResolver
        .CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_WEATHER;


    static Uri buildWeatherUri(long id) {
      return ContentUris.withAppendedId(CONTENT_URI, id);
    }

    static Uri buildWeatherLocation(String locationSetting) {
      return CONTENT_URI.buildUpon().appendPath(locationSetting).build();
    }

    public static Uri buildWeatherLocationWithStartDate(
        String locationSetting, long startDate) {
      long normalizedDate = normalizeDate(startDate);
      return CONTENT_URI.buildUpon().appendPath(locationSetting)
          .appendQueryParameter(COL_DATE, Long.toString(normalizedDate))
          .build();
    }

    public static Uri buildWeatherLocationWithDate(String locationSetting,
        long date) {
      return CONTENT_URI.buildUpon().appendPath(locationSetting)
          .appendPath(Long.toString(normalizeDate(date))).build();
    }

    static String getLocationSettingFromUri(Uri uri) {
      return uri.getPathSegments().get(1);
    }

    static long getDateFromUri(Uri uri) {
      return Long.parseLong(uri.getPathSegments().get(2));
    }

    static long getStartDateFromUri(Uri uri) {
      String dateString = uri.getQueryParameter(COL_DATE);
      if (null != dateString && dateString.length() > 0)
        return Long.parseLong(dateString);
      else
        return 0;
    }
  }
}