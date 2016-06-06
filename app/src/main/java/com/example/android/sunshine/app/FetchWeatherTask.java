package com.example.android.sunshine.app;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.example.android.sunshine.app.data.WeatherContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

  static final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
  private ArrayAdapter<String> mForecastAdapter;
  private final Context mContext;

  public FetchWeatherTask(Context context,
      ArrayAdapter<String> forecastAdapter) {
    mContext = context;
    mForecastAdapter = forecastAdapter;
  }

  long addLocation(String locSetting, String cityName,
      double latitude, double longitude) {

    Cursor cursor = mContext.getContentResolver().query(
        WeatherContract.LocationEntry.CONTENT_URI,
        new String[]{WeatherContract.LocationEntry._ID},
        WeatherContract.LocationEntry.TABLE_NAME
            + "." + WeatherContract.LocationEntry.COL_LOC_SETTING + " = ?",
        new String[]{locSetting},
        null);

    long rowId = -1;
    if (cursor == null) {
      return rowId;
    } else if (cursor.moveToFirst()) {
      // We already have this location
      rowId = cursor.getLong(cursor.getColumnIndex(
          WeatherContract.LocationEntry._ID));
    } else {
      // We don't have this location; insert it.
      ContentValues values = new ContentValues();
      values.put(WeatherContract.LocationEntry.COL_LOC_SETTING, locSetting);
      values.put(WeatherContract.LocationEntry.COL_CITY_NAME, cityName);
      values.put(WeatherContract.LocationEntry.COL_LATITUDE, latitude);
      values.put(WeatherContract.LocationEntry.COL_LONGITUDE, longitude);
      Uri uri = mContext.getContentResolver()
          .insert(WeatherContract.LocationEntry.CONTENT_URI, values);
      rowId = ContentUris.parseId(uri);
    }

    cursor.close();
    return rowId;
  }

  // GOOG helper: convert the API's Unix time to something human-readable.
  @SuppressLint("SimpleDateFormat")
  private String getReadableDateString(long time) {
    SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
    return shortenedDateFormat.format(time);
  }

  /* GOOG helper: Prepare the weather high/lows for presentation.
   * With addition of user preference, this method now ruined by Josh. */
  private String formatHighLows(double high, double low) {
    // Find what units the user wants, convert data if necessary.
    String units = PreferenceManager.getDefaultSharedPreferences(mContext)
        .getString(mContext.getString(R.string.pref_units_key),
            mContext.getString(R.string.str_celsius_key));
    if (units.equals(mContext.getString(R.string.str_fahrenheit_key))) {
      high = high * 1.8 + 32;
      low = low * 1.8 + 32;
    }

    // For presentation, assume the user doesn't care about tenths of a degree.
    return Math.round(high) + " / " + Math.round(low);
  }

  // GOOG helper: Parses JSON from the API call.
  @SuppressWarnings("deprecation")
  private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
      throws JSONException {

    final String OWM_LIST = "list";
    final String OWM_WEATHER = "weather";
    final String OWM_TEMPERATURE = "temp";
    final String OWM_MAX = "max";
    final String OWM_MIN = "min";
    final String OWM_DESCRIPTION = "main";

    JSONObject forecastJson = new JSONObject(forecastJsonStr);
    JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

    // GOOG: do something weird with dates and times.
    Time dayTime = new Time();
    dayTime.setToNow();
    int julianStartDay = Time
        .getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

    // now we work exclusively in UTC (hmm, yes, ok)
    dayTime = new Time();

    String[] resultStrs = new String[numDays];
    for (int i = 0; i < weatherArray.length(); i++) {
      String day;
      String description;
      String highAndLow;

      // Get the JSON object representing the day
      JSONObject dayForecast = weatherArray.getJSONObject(i);

      // The date/time comes from OWM as a long. Make it human-readable.
      long dateTime;
      dateTime = dayTime.setJulianDay(julianStartDay + i);
      day = getReadableDateString(dateTime);

      JSONObject weatherObject = dayForecast
          .getJSONArray(OWM_WEATHER).getJSONObject(0);
      description = weatherObject.getString(OWM_DESCRIPTION);

      // OWM temperatures come in a object named "temp". Bad naming strategy.
      JSONObject temperatureObject = dayForecast
          .getJSONObject(OWM_TEMPERATURE);
      double high = temperatureObject.getDouble(OWM_MAX);
      double low = temperatureObject.getDouble(OWM_MIN);

      highAndLow = formatHighLows(high, low);
      resultStrs[i] = day + " || " + description + " || " + highAndLow;
    }

    return resultStrs;
  }

  @Override
  protected String[] doInBackground(String... params) {
    if (params.length != 1) {
      Log.e(LOG_TAG, "Wrong parameter(s) passed to AsyncTask.");
      return null;
    }

    int numDays = 7;
    HttpURLConnection urlConnection = null;
    BufferedReader reader = null;
    String forecastJsonStr = null;
    try {
      // Construct the URL for the OpenWeatherMap query
      Uri.Builder uriBuilder = new Uri.Builder();
      uriBuilder.scheme("http")
          .authority("api.openweathermap.org")
          .appendPath("data")
          .appendPath("2.5")
          .appendPath("forecast")
          .appendPath("daily")
          .appendQueryParameter("q", params[0])
          .appendQueryParameter("mode", "json")
          .appendQueryParameter("units", "metric")
          .appendQueryParameter("cnt", String.valueOf(numDays))
          .appendQueryParameter("APPID",
              mContext.getString(R.string.private_owm_api));
      URL url = new URL(uriBuilder.build().toString());

      // Create the request to OpenWeatherMap, and open the connection
      urlConnection = (HttpURLConnection) url.openConnection();
      urlConnection.setRequestMethod("GET");
      urlConnection.connect();

      // Read the input stream into a String
      InputStream inputStream = urlConnection.getInputStream();
      StringBuilder buffer = new StringBuilder();
      if (inputStream == null) {
        // Nothing to do.
        return null;
      }
      reader = new BufferedReader(new InputStreamReader(inputStream));

      String line;
      while ((line = reader.readLine()) != null) {
        // Add newlines to aid debugging.
        buffer.append(line).append("\n");
      }
      if (buffer.length() == 0) {
        return null;
      }
      forecastJsonStr = buffer.toString();

    } catch (IOException e) {
      Log.e(LOG_TAG, "Error", e);
      return null;

    } finally {
      if (urlConnection != null) {
        urlConnection.disconnect();
      }

      if (reader != null) {
        try {
          reader.close();
        } catch (final IOException e) {
          Log.e(LOG_TAG, "Error closing stream", e);
        }
      }
    }

    try {
      return getWeatherDataFromJson(forecastJsonStr, numDays);
    } catch (JSONException e) {
      Log.e(LOG_TAG, e.getMessage(), e);
      e.printStackTrace();
    }

    return null;
  }

  @Override
  protected void onPostExecute(String[] result) {
    if (result != null) {
      mForecastAdapter.clear();
      for (String str : result) {
        mForecastAdapter.add(str);
      }
    }
  }
}
