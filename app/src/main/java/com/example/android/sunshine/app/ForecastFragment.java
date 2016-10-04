package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.android.sunshine.app.data.WeatherContract.WeatherEntry;

/**
 * Fragment inside of MainActivity. Displays the weather forecast entries.
 *
 * @see MainActivity
 */
public class ForecastFragment extends Fragment {
  static final String LOG_TAG = ForecastFragment.class.getSimpleName();

  ForecastAdapter mForecastAdapter;

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
    String locationSetting = Utility.getPreferredLocation(getActivity());
    String sortOrder = WeatherEntry.COL_DATE + " ASC";
    Uri  weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(locationSetting,
        System.currentTimeMillis());
    Cursor cursor = getActivity().getContentResolver().query(weatherForLocationUri,
        null, null, null, sortOrder);
    mForecastAdapter = new ForecastAdapter(getActivity(), cursor, 0);

    View rootView = inflater.inflate(R.layout.fragment_main, container, false);

    ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
    listView.setAdapter(mForecastAdapter);

    return rootView;
  }

  @Override
  public void onStart() {
    super.onStart();
    updateWeather();
  }

}
