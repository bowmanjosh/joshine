package com.example.android.sunshine.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
import java.util.ArrayList;

/**
 * Fragment inside of MainActivity. Displays the weather forecast entries.
 *
 * @see MainActivity
 */
public class ForecastFragment extends Fragment {
  static final String LOG_TAG = ForecastFragment.class.getSimpleName();

  ArrayAdapter<String> mForecastAdapter;

  public ForecastFragment() {
  }

  private void updateWeather() {
    Log.v(LOG_TAG, "Now inside updateWeather()");
    FetchWeatherTask fetchWeather = new FetchWeatherTask();
    SharedPreferences sharedPref = PreferenceManager
        .getDefaultSharedPreferences(getActivity());
    fetchWeather.execute(sharedPref.getString(
        getString(R.string.pref_location_key),
        getString(R.string.pref_location_default)
    ));
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.forecastfragment, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.action_refresh) {
      updateWeather();
      return true;
    } else if (id == R.id.action_view_map_location) {
      Intent viewMapIntent = new Intent()
          .setAction(Intent.ACTION_VIEW)
          .setData(Uri.parse("geo:0,0?")
              .buildUpon()
              .appendQueryParameter("q",
                  PreferenceManager.getDefaultSharedPreferences(getActivity())
                      .getString(getString(R.string.pref_location_key),
                          getString(R.string.pref_location_default)))
              .build());
      if (viewMapIntent.resolveActivity(getActivity().getPackageManager())
          != null) {
        startActivity(viewMapIntent);
      } else {
        Log.e(LOG_TAG, "No apps available to view map location.");
      }
      return true;
    } else {
      return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_main, container, false);

    mForecastAdapter = new ArrayAdapter<>(
        getActivity(),
        R.layout.list_item_forecast,
        R.id.list_item_forecast_textview,
        new ArrayList<String>());

    ListView listView =
        (ListView) rootView.findViewById(R.id.listview_forecast);
    listView.setAdapter(mForecastAdapter);
    // pass the list item's forecast string to DetailActivity
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(
          AdapterView<?> adapterView, View view, int i, long l) {
        startActivity(new Intent(getActivity(), DetailActivity.class)
            .putExtra(Intent.EXTRA_TEXT, mForecastAdapter.getItem(i)));
      }
    });

    return rootView;
  }

  @Override
  public void onStart() {
    super.onStart();
    updateWeather();
  }

  public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

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
      String wowrude = "wowrude";
      SharedPreferences prefs = PreferenceManager
          .getDefaultSharedPreferences(getActivity());
      String units = prefs
          .getString(getString(R.string.pref_units_key), wowrude);
      if (units.equals(wowrude)) {
        Log.e(LOG_TAG, "do you even prefs, bro?");
      } else if (units.equals(getString(R.string.str_fahrenheit_key))) {
        high = high * 1.8 + 32;
        low = low * 1.8 + 32;
      }

      // For presentation, assume the user doesn't care about tenths of a degree.
      long roundedHigh = Math.round(high);
      long roundedLow = Math.round(low);

      return roundedHigh + " / " + roundedLow;
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
            .appendQueryParameter("APPID", getString(R.string.private_owm_api));
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
}
