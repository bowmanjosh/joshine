package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
    FetchWeatherTask fetchWeather = new FetchWeatherTask(getActivity(),
        mForecastAdapter);
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

    getString(R.string.str_fahrenheit_key);
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

}
