package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.sunshine.app.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine.app.data.WeatherContract.LocationEntry;

/**
 * Fragment inside of MainActivity. Displays the weather forecast entries.
 *
 * @see MainActivity
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
  static final String LOG_TAG = ForecastFragment.class.getSimpleName();
  private static final int FORECAST_LOADER_ID = 1;

  ForecastAdapter mForecastAdapter;

  /**
   * The database projection to use for this fragment's list of forecasts. This and the following
   * int "indices" are a really bad hacky way to do this, but it's how the instructors are doing it
   * and I don't know enough Java and Android to have a good solution. So just BEWARE that this is
   * not a good solution and make sure not to mess it up.
   */
  private static final String[] FORECAST_PROJECTION = {
      WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
      WeatherEntry.COL_DATE,
      WeatherEntry.COL_SHORT_DESC,
      WeatherEntry.COL_MAX_TEMP,
      WeatherEntry.COL_MIN_TEMP,
      LocationEntry.COL_LOC_SETTING,
      WeatherEntry.COL_WEATHER_ID,
      LocationEntry.COL_LATITUDE,
      LocationEntry.COL_LONGITUDE
  };
  // public static final int COL_WEATHER_TABLE_ID = 0;
  public static final int COL_WEATHER_DATE = 1;
  public static final int COL_WEATHER_SHORT_DESC = 2;
  public static final int COL_WEATHER_MAX_TEMP = 3;
  public static final int COL_WEATHER_MIN_TEMP = 4;
  // public static final int COL_LOC_SETTING = 5;
  // public static final int COL_WEATHER_ID = 6;
  // public static final int COL_LATITUDE = 7;
  // public static final int COL_LONGITUDE = 8;

  public ForecastFragment() {
  }

  private void updateWeather() {
    Log.v(LOG_TAG, "Now inside updateWeather()");
    FetchWeatherTask fetchWeather = new FetchWeatherTask(getActivity());
    fetchWeather.execute(Utility.getPreferredLocation(getActivity()));
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
              .appendQueryParameter("q", Utility.getPreferredLocation(getActivity()))
              .build());
      if (viewMapIntent.resolveActivity(getActivity().getPackageManager()) != null) {
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
    mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);

    View rootView = inflater.inflate(R.layout.fragment_main, container, false);

    ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
    listView.setAdapter(mForecastAdapter);
    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
          Cursor cursor = (Cursor) adapterView.getItemAtPosition(i);
          if (cursor != null) {
            String locationSetting = Utility.getPreferredLocation(getActivity());
            Intent intent = new Intent(getActivity(), DetailActivity.class)
                .setData(WeatherEntry.buildWeatherLocationWithDate(locationSetting,
                    cursor.getLong(COL_WEATHER_DATE)));
            startActivity(intent);
          } else {
            Log.d(LOG_TAG, "Call to adapterView.getItemAtPosition(i) failed.");
          }
        }
    });

    return rootView;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    getLoaderManager().initLoader(FORECAST_LOADER_ID, null, this);
    super.onActivityCreated(savedInstanceState);
  }

  @Override
  public void onStart() {
    super.onStart();
    updateWeather();
  }

  @Override
  public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
    return new CursorLoader(getActivity(),
        WeatherEntry.buildWeatherLocationWithStartDate(
            Utility.getPreferredLocation(getActivity()), System.currentTimeMillis()),
        FORECAST_PROJECTION,
        null,
        null,
        WeatherEntry.COL_DATE + " ASC");
  }

  @Override
  public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
    mForecastAdapter.swapCursor(cursor);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> cursorLoader) {
    mForecastAdapter.swapCursor(null);
  }

}
