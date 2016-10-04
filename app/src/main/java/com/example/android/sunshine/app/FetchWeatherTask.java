package com.example.android.sunshine.app;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.format.Time;
import android.util.Log;

import com.example.android.sunshine.app.data.WeatherContract.LocationEntry;
import com.example.android.sunshine.app.data.WeatherContract.WeatherEntry;
import static com.example.android.sunshine.app.data.OwmStrings.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

class FetchWeatherTask extends AsyncTask<String, Void, Void> {

  private static final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
  private static final int NUM_DAYS = 14;

  private final Context mContext;

  FetchWeatherTask(Context context) {
    mContext = context;
  }

  long addLocation(String locSetting, String cityName,
      double latitude, double longitude) {

    Cursor cursor = mContext.getContentResolver().query(
        LocationEntry.CONTENT_URI,
        new String[]{LocationEntry._ID},
        LocationEntry.TABLE_NAME + "." + LocationEntry.COL_LOC_SETTING + " = ?",
        new String[]{locSetting},
        null);

    long rowId = -1;
    if (cursor == null) {
      return rowId;
    } else if (cursor.moveToFirst()) {
      // We already have this location
      rowId = cursor.getLong(cursor.getColumnIndex(LocationEntry._ID));
    } else {
      // We don't have this location; insert it.
      ContentValues values = new ContentValues();
      values.put(LocationEntry.COL_LOC_SETTING, locSetting);
      values.put(LocationEntry.COL_CITY_NAME, cityName);
      values.put(LocationEntry.COL_LATITUDE, latitude);
      values.put(LocationEntry.COL_LONGITUDE, longitude);
      Uri uri = mContext.getContentResolver().insert(LocationEntry.CONTENT_URI, values);
      rowId = ContentUris.parseId(uri);
    }

    cursor.close();
    return rowId;
  }

  @SuppressWarnings("deprecation")
  private void getWeatherDataFromJson(String forecastJsonStr,
      String locSetting) throws JSONException {

    JSONObject forecastJson = new JSONObject(forecastJsonStr);
    JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
    if (weatherArray.length() != NUM_DAYS) {
      Log.e(LOG_TAG, "Length of weatherArray != NUM_DAYS.");
    }

    // GOOG: do something weird with dates and times.
    Time dayTime = new Time();
    dayTime.setToNow();
    int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

    // now we work exclusively in UTC (hmm, yes, ok)
    // NOTE: In the Google code they use this to normalize datetimes. I think.
    dayTime = new Time();

    // Call addLocation() for the data.
    JSONObject locationJson = forecastJson.getJSONObject(OWM_CITY);
    long locationRowId = addLocation(locSetting,
        locationJson.getString(OWM_CITY_NAME),
        locationJson.getJSONObject(OWM_CITY_COORD).getDouble(OWM_COORD_LAT),
        locationJson.getJSONObject(OWM_CITY_COORD).getDouble(OWM_COORD_LON));
    if (locationRowId == -1) {
      Log.e(LOG_TAG, "Something went wrong in addLocation().");
    }

    /* The Udacity course uses a Vector<ContentValues> here. I'm not sure we
     * really need a Vector, but I'm going to use one for now anyway, just to
     * maintain consistency with the course. */
    Vector<ContentValues> valuesVector = new Vector<>(weatherArray.length());
    for (int i = 0; i < weatherArray.length(); i++) {
      ContentValues values = new ContentValues();
      JSONObject dayJson = weatherArray.getJSONObject(i);
      JSONObject weatherObject = dayJson.getJSONArray(OWM_WEATHER).getJSONObject(0);

      values.put(WeatherEntry.COL_LOC_KEY, locationRowId);
      values.put(WeatherEntry.COL_DATE, dayTime.setJulianDay(julianStartDay + i));
      values.put(WeatherEntry.COL_SHORT_DESC, weatherObject.getString(OWM_WEATHER_SHORT_DESC));
      values.put(WeatherEntry.COL_WEATHER_ID, weatherObject.getInt(OWM_WEATHER_ID));
      values.put(WeatherEntry.COL_MIN_TEMP,
          dayJson.getJSONObject(OWM_TEMPERATURE).getDouble(OWM_TEMPERATURE_MIN));
      values.put(WeatherEntry.COL_MAX_TEMP,
          dayJson.getJSONObject(OWM_TEMPERATURE).getDouble(OWM_TEMPERATURE_MAX));
      values.put(WeatherEntry.COL_HUMIDITY, dayJson.getDouble(OWM_HUMIDITY));
      values.put(WeatherEntry.COL_PRESSURE, dayJson.getDouble(OWM_PRESSURE));
      values.put(WeatherEntry.COL_WIND_SPEED, dayJson.getDouble(OWM_WIND_SPEED));
      values.put(WeatherEntry.COL_DEGREES, dayJson.getDouble(OWM_WIND_DEG));

      valuesVector.add(values);
    }
    ContentValues[] valuesArray = new ContentValues[valuesVector.size()];
    valuesVector.toArray(valuesArray);
    int rowsInserted = mContext.getContentResolver().bulkInsert(
        WeatherEntry.CONTENT_URI, valuesArray);
    Log.v(LOG_TAG, "Rows inserted: " + rowsInserted + ". valuesVector size: "
        + valuesVector.size() + ".");
  }

  @Override
  protected Void doInBackground(String... params) {
    if (params.length != 1) {
      Log.e(LOG_TAG, "Wrong parameter(s) passed to AsyncTask.");
      return null;
    }

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
          .appendQueryParameter("cnt", String.valueOf(NUM_DAYS))
          .appendQueryParameter("APPID", mContext.getString(R.string.private_owm_api));
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
      Log.e(LOG_TAG, "Error connecting to or retrieving API source data.", e);
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
      getWeatherDataFromJson(forecastJsonStr, params[0]);
    } catch (JSONException e) {
      Log.e(LOG_TAG, e.getMessage(), e);
      e.printStackTrace();
    }

    return null;
  }
}
