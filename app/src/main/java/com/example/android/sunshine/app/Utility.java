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
package com.example.android.sunshine.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.Time;

import java.text.SimpleDateFormat;
import java.util.Locale;

class Utility {

  static String convertWindDirection(double degrees) {
    if (degrees >= 337.5 || degrees < 22.5) {
      return "N";
    } else if (degrees >= 22.5 && degrees < 67.5) {
      return "NE";
    } else if (degrees >= 67.5 && degrees < 112.5) {
      return "E";
    } else if (degrees >= 112.5 && degrees < 157.5) {
      return "SE";
    } else if (degrees >= 157.5 && degrees < 202.5) {
      return "S";
    } else if (degrees >= 202.5 && degrees < 247.5) {
      return "SW";
    } else if (degrees >= 247.5 && degrees < 292.5) {
      return "W";
    } else if (degrees >= 292.5 && degrees < 337.5) {
      return "NW";
    } else {
      return "";
    }
  }

  // OWM gives wind speed in meters per second. Convert to kilometers per hour or miles per hour.
  // Units reference: https://www.weather.gov/media/epz/wxcalc/windConversion.pdf
  static double convertWindSpeed(double speed, boolean isCelsius) {
    if (isCelsius) {
      return speed * 3.6;
    } else {
      return speed * 2.23694;
    }
  }

  static String getPreferredLocation(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    return prefs.getString(context.getString(R.string.pref_location_key),
        context.getString(R.string.pref_location_default));
  }

  static boolean isCelsius(Context context) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    return prefs.getString(context.getString(R.string.pref_units_key),
        context.getString(R.string.str_celsius_key))
        .equals(context.getString(R.string.str_celsius_key));
  }

  static String formatTemperature(Context context, double temperature, boolean isCelsius) {
    if ( !isCelsius ) {
      temperature = temperature * 9 / 5 + 32;
    }

    return context.getString(R.string.format_temperature, temperature);
  }


  /**
   * Given a day, returns just the name to use for that day.
   * E.g "today", "tomorrow", "wednesday".
   *
   * @param context Context to use for resource localization
   * @param dateInMillis The date in milliseconds
   */
  @SuppressWarnings("deprecation")
  static String getDayName(Context context, long dateInMillis) {
    // If the date is today, return the localized version of "Today" instead of the actual
    // day name.

    Time t = new Time();
    t.setToNow();
    int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
    int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
    if (julianDay == currentJulianDay) {
      return context.getString(R.string.today);
    } else if ( julianDay == currentJulianDay +1 ) {
      return context.getString(R.string.tomorrow);
    } else {
      Time time = new Time();
      time.setToNow();
      // Otherwise, the format is just the day of the week (e.g "Wednesday".
      // Note: should probably be using Android's locale methods but I don't understand them yet.
      SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
      return dayFormat.format(dateInMillis);
    }
  }

  /**
   * Converts db date format to the format "Month day", e.g "June 24".
   * @param dateInMillis The db formatted date string, expected to be of the form specified
   *                in Utility.DATE_FORMAT
   * @return The day in the form of a string formatted "December 6"
   */
  static String getFormattedMonthDay(long dateInMillis) {
    SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd", Locale.getDefault());
    return monthDayFormat.format(dateInMillis);
  }

  /**
   * Helper method to convert the database representation of the date into something to display
   * to users.  As classy and polished a user experience as "20140102" is, we can do better.
   *
   * @param context Context to use for resource localization
   * @param dateInMillis The date in milliseconds
   * @return a user-friendly representation of the date.
   */
  @SuppressWarnings("deprecation")
  static String getFriendlyDayString(Context context, long dateInMillis) {
    // The day string for forecast uses the following logic:
    // For today: "Today, June 8"
    // For tomorrow:  "Tomorrow"
    // For the next 5 days: "Wednesday" (just the day name)
    // For all days after that: "Mon Jun 8"

    Time time = new Time();
    time.setToNow();
    long currentTime = System.currentTimeMillis();
    int julianDay = Time.getJulianDay(dateInMillis, time.gmtoff);
    int currentJulianDay = Time.getJulianDay(currentTime, time.gmtoff);

    // If the date we're building the String for is today's date, the format
    // is "Today, June 24"
    if (julianDay == currentJulianDay) {
      String today = context.getString(R.string.today);
      int formatId = R.string.format_full_friendly_date;
      return context.getString(
          formatId,
          today,
          getFormattedMonthDay(dateInMillis));
    } else if ( julianDay < currentJulianDay + 7 ) {
      // If the input date is less than a week in the future, just return the day name.
      return getDayName(context, dateInMillis);
    } else {
      // Otherwise, use the form "Mon Jun 3"
      SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd",
          Locale.getDefault());
      return shortenedDateFormat.format(dateInMillis);
    }
  }

  /**
   * Helper method to provide the icon resource id according to the weather condition id returned
   * by the OpenWeatherMap call.
   * @param weatherId from OpenWeatherMap API response
   * @return resource id for the corresponding icon. -1 if no relation is found.
   */
  static int getIconResourceForWeatherCondition(int weatherId) {
    // Based on weather code data found at:
    // https://openweathermap.org/weather-conditions

    // First, let's check for the unique codes for which we have pictures.
    switch (weatherId) {
      case 800:
        return R.drawable.ic_clear;
      case 801:
        return R.drawable.ic_light_clouds;
      default:
        break;
    }

    // If we didn't match in the above block, then we only have a generic category picture.
    // The categories go by hundreds (200-299, 300-399, etc.) so we can divide weatherId by 100.
    weatherId = weatherId / 100;
    switch (weatherId) {
      case 2:
        return R.drawable.ic_storm;
      case 3:
        return R.drawable.ic_light_rain;
      case 4:
        return R.drawable.ic_rain;
      case 5:
        return R.drawable.ic_rain;
      case 6:
        return R.drawable.ic_rain;
      case 7:
        return R.drawable.ic_fog;
      case 8:
        return R.drawable.ic_cloudy;
      default:
        break;
    }

    // If we get here, we didn't find a matching code.
    return -1;
  }

  /**
   * Helper method to provide the art resource id according to the weather condition id returned
   * by the OpenWeatherMap call.
   * @param weatherId from OpenWeatherMap API response
   * @return resource id for the corresponding image. -1 if no relation is found.
   */
  static int getArtResourceForWeatherCondition(int weatherId) {
    // Based on weather code data found at:
    // https://openweathermap.org/weather-conditions

    // First, let's check for the unique codes for which we have pictures.
    switch (weatherId) {
      case 800:
        return R.drawable.art_clear;
      case 801:
        return R.drawable.art_light_clouds;
      default:
        break;
    }

    // If we didn't match in the above block, then we only have a generic category picture.
    // The categories go by hundreds (200-299, 300-399, etc.) so we can divide weatherId by 100.
    weatherId = weatherId / 100;
    switch (weatherId) {
      case 2:
        return R.drawable.art_storm;
      case 3:
        return R.drawable.art_light_rain;
      case 4:
        return R.drawable.art_rain;
      case 5:
        return R.drawable.art_rain;
      case 6:
        return R.drawable.art_rain;
      case 7:
        return R.drawable.art_fog;
      case 8:
        return R.drawable.art_clouds;
      default:
        break;
    }

    // If we get here, we didn't find a matching code.
    return -1;
  }
}