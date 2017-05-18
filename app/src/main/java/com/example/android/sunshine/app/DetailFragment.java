package com.example.android.sunshine.app;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

  private static final String LOG_TAG = DetailFragment.class.getSimpleName();

  private ShareActionProvider mShareActionProvider;
  private String mForecastStr;
  private Uri mUri;
  private final String SHARE_HASHTAG = "#SunshineApp";
  private static final int DETAIL_LOADER_ID = 0;

  /**
   * Copied from ForecastFragment. I still hate this but it's what the instructor code does. The
   * int constants have been modified: in ForecastFragment they are public, here they're private.
   */
  private static final String[] DETAIL_PROJECTION = {
      WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
      WeatherContract.WeatherEntry.COL_DATE,
      WeatherContract.WeatherEntry.COL_SHORT_DESC,
      WeatherContract.WeatherEntry.COL_MAX_TEMP,
      WeatherContract.WeatherEntry.COL_MIN_TEMP,
      WeatherContract.LocationEntry.COL_LOC_SETTING,
      WeatherContract.WeatherEntry.COL_WEATHER_ID,
      WeatherContract.LocationEntry.COL_LATITUDE,
      WeatherContract.LocationEntry.COL_LONGITUDE
  };
  // private static final int COL_WEATHER_TABLE_ID = 0;
  private static final int COL_WEATHER_DATE = 1;
  private static final int COL_WEATHER_SHORT_DESC = 2;
  private static final int COL_WEATHER_MAX_TEMP = 3;
  private static final int COL_WEATHER_MIN_TEMP = 4;
  // private static final int COL_LOC_SETTING = 5;
  // private static final int COL_WEATHER_ID = 6;
  // private static final int COL_LATITUDE = 7;
  // private static final int COL_LONGITUDE = 8;

  public DetailFragment() {
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    return new CursorLoader(getActivity(),
        mUri,
        DETAIL_PROJECTION,
        null,
        null,
        null);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
    if (!cursor.moveToFirst()) {
      return;
    }

    Log.v(LOG_TAG, "Cursor has " + cursor.getCount() + " rows.");

    View rootView = getView();
    if (rootView != null) {
      TextView tv = (TextView) getView().findViewById(R.id.detail_text);
      if (tv != null) {
        boolean celsius = Utility.isCelsius(getContext());
        Log.v(LOG_TAG, "Celsius status: " + celsius);
        Context context = getActivity();
        mForecastStr = Utility.formatDate(cursor.getLong(COL_WEATHER_DATE))
            + " - " + cursor.getString(COL_WEATHER_SHORT_DESC)
            + " - " + Utility.formatTemperature(context,
            cursor.getDouble(COL_WEATHER_MAX_TEMP), celsius)
            + "/" + Utility.formatTemperature(context,
            cursor.getDouble(COL_WEATHER_MIN_TEMP), celsius);
        tv.setText(mForecastStr);
      }

      if (mShareActionProvider != null) {
        mShareActionProvider.setShareIntent(createShareForecastIntent());
      }
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
  }

  @SuppressWarnings("deprecation")
  private Intent createShareForecastIntent() {
    return new Intent()
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        .setAction(Intent.ACTION_SEND)
        .setType("text/plain")
        .putExtra(Intent.EXTRA_TEXT, mForecastStr + " " + SHARE_HASHTAG);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
    Intent intent = getActivity().getIntent();
    if (intent != null) {
      mUri = intent.getData();
    }
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.detail_fragment, menu);
    mShareActionProvider = (ShareActionProvider) MenuItemCompat
        .getActionProvider(menu.findItem(R.id.action_menu_share));
    if (mForecastStr != null) {
      mShareActionProvider.setShareIntent(createShareForecastIntent());
    }
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_detail, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    getLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);
    super.onActivityCreated(savedInstanceState);
  }
}
