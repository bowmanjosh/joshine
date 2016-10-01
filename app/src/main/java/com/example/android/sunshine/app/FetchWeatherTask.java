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
import java.text.SimpleDateFormat;
import java.util.Vector;

class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

  static final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
  static final int NUM_DAYS = 14;

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
      Uri uri = mContext.getContentResolver()
          .insert(LocationEntry.CONTENT_URI, values);
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

  @SuppressWarnings("deprecation")
  private String[] getWeatherDataFromJson(String forecastJsonStr,
      String locSetting) throws JSONException {

    JSONObject forecastJson = new JSONObject(forecastJsonStr);
    JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
    if (weatherArray.length() != NUM_DAYS) {
      Log.e(LOG_TAG, "Length of weatherArray != NUM_DAYS.");
    }

    // GOOG: do something weird with dates and times.
    Time dayTime = new Time();
    dayTime.setToNow();
    int julianStartDay = Time
        .getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

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
      JSONObject weatherObject = dayJson.getJSONArray(OWM_WEATHER)
          .getJSONObject(0);

      values.put(WeatherEntry.COL_LOC_KEY, locationRowId);
      values.put(WeatherEntry.COL_DATE, dayTime
          .setJulianDay(julianStartDay + i));
      values.put(WeatherEntry.COL_SHORT_DESC, weatherObject
          .getString(OWM_WEATHER_SHORT_DESC));
      values.put(WeatherEntry.COL_WEATHER_ID, weatherObject
          .getInt(OWM_WEATHER_ID));
      values.put(WeatherEntry.COL_MIN_TEMP, dayJson
          .getJSONObject(OWM_TEMPERATURE).getDouble(OWM_TEMPERATURE_MIN));
      values.put(WeatherEntry.COL_MAX_TEMP, dayJson
          .getJSONObject(OWM_TEMPERATURE).getDouble(OWM_TEMPERATURE_MAX));
      values.put(WeatherEntry.COL_HUMIDITY, dayJson.getDouble(OWM_HUMIDITY));
      values.put(WeatherEntry.COL_PRESSURE, dayJson.getDouble(OWM_PRESSURE));
      values.put(WeatherEntry.COL_WIND_SPEED, dayJson
          .getDouble(OWM_WIND_SPEED));
      values.put(WeatherEntry.COL_DEGREES, dayJson.getDouble(OWM_WIND_DEG));
      valuesVector.add(values);
    }
    ContentValues[] valuesArray = new ContentValues[valuesVector.size()];
    valuesVector.toArray(valuesArray);
    int rowsInserted = mContext.getContentResolver().bulkInsert(
        WeatherEntry.CONTENT_URI, valuesArray);
    Log.v(LOG_TAG, "Rows inserted: " + rowsInserted + ". valuesVector size: "
        + valuesVector.size() + ".");

    // FIXME: Once the Provider code is finished, refactor this.
    Cursor cursor = mContext.getContentResolver().query(
        WeatherEntry.buildWeatherLocationWithStartDate(locSetting,
            System.currentTimeMillis()),
        null, null, null, WeatherEntry.COL_DATE + " ASC");
    int colDate = cursor.getColumnIndex(WeatherEntry.COL_DATE);
    int colShortDesc = cursor.getColumnIndex(WeatherEntry.COL_SHORT_DESC);
    int colMaxTemperature = cursor.getColumnIndex(WeatherEntry.COL_MAX_TEMP);
    int colMinTemperature = cursor.getColumnIndex(WeatherEntry.COL_MIN_TEMP);
    String[] resultStrings = new String[cursor.getCount()];
    cursor.moveToFirst();
    for (int i = 0; i < cursor.getCount(); i++) {
      String day = getReadableDateString(cursor.getLong(colDate));
      String description = cursor.getString(colShortDesc);
      double maxTemperature = cursor.getDouble(colMaxTemperature);
      double minTemperature = cursor.getDouble(colMinTemperature);

      resultStrings[i] = day + " || " + description + " || "
          + formatHighLows(maxTemperature, minTemperature);
      cursor.moveToNext();
    }
    Log.v(LOG_TAG, "Cursor size: " + cursor.getCount()
        + ". Last result string: " + resultStrings[resultStrings.length - 1]);
    cursor.close();

    return resultStrings;
  }

  @Override
  protected String[] doInBackground(String... params) {
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
      return getWeatherDataFromJson(forecastJsonStr, params[0]);
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
