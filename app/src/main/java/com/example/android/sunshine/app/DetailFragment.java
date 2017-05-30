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
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

  private static final String LOG_TAG = DetailFragment.class.getSimpleName();

  private ShareActionProvider mShareActionProvider;
  private String mForecastShare = null;
  private Uri mUri;
  private static final String SHARE_HASHTAG = "#SunshineApp";
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
      WeatherContract.WeatherEntry.COL_HUMIDITY,
      WeatherContract.WeatherEntry.COL_WIND_SPEED,
      WeatherContract.WeatherEntry.COL_DEGREES,
      WeatherContract.WeatherEntry.COL_PRESSURE
  };
  // private static final int COL_WEATHER_TABLE_ID = 0;
  private static final int COL_WEATHER_DATE = 1;
  private static final int COL_WEATHER_SHORT_DESC = 2;
  private static final int COL_WEATHER_MAX_TEMP = 3;
  private static final int COL_WEATHER_MIN_TEMP = 4;
  private static final int COL_WEATHER_HUMIDITY = 5;
  private static final int COL_WEATHER_WIND_SPEED = 6;
  private static final int COL_WEATHER_DEGREES = 7;
  private static final int COL_WEATHER_PRESSURE = 8;

  // Member variables to hold references to Views.
  private ImageView mWeatherIcon;
  private TextView mDayName;
  private TextView mMonthDate;
  private TextView mWeather;
  private TextView mHigh;
  private TextView mLow;
  private TextView mHumidity;
  private TextView mWind;
  private TextView mPressure;

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
      Context context = getContext();

      mWeatherIcon.setImageResource(R.drawable.ic_launcher);
      long dateLong = cursor.getLong(COL_WEATHER_DATE);
      mDayName.setText(Utility.getDayName(context, dateLong));
      String dateString = Utility.getFormattedMonthDay(context, dateLong);
      mMonthDate.setText(dateString);
      String weather = cursor.getString(COL_WEATHER_SHORT_DESC);
      mWeather.setText(weather);
      boolean isCelsius = Utility.isCelsius(context);
      String high = Utility.formatTemperature(context, cursor.getDouble(COL_WEATHER_MAX_TEMP),
          isCelsius);
      mHigh.setText(high);
      String low = Utility.formatTemperature(context, cursor.getDouble(COL_WEATHER_MIN_TEMP),
          isCelsius);
      mLow.setText(low);
      mHumidity.setText(context
          .getString(R.string.format_humidity, cursor.getFloat(COL_WEATHER_HUMIDITY)));
      double windSpeed = Utility
          .convertWindSpeed(cursor.getDouble(COL_WEATHER_WIND_SPEED), isCelsius);
      String windUnits;
      if (isCelsius) {
        windUnits = getString(R.string.wind_units_metric);
      } else {
        windUnits = getString(R.string.wind_units_imperial);
      }
      String windDirection = Utility.convertWindDirection(cursor.getDouble(COL_WEATHER_DEGREES));
      mWind.setText(context.getString(R.string.format_wind, windSpeed, windUnits, windDirection));
      mPressure.setText(context
          .getString(R.string.format_pressure, cursor.getDouble(COL_WEATHER_PRESSURE)));


      // social web 2.0 cloud engagement
      mForecastShare = String.format("%s - %s - %s/%s", dateString, weather, high, low);
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
        .putExtra(Intent.EXTRA_TEXT, mForecastShare + " " + SHARE_HASHTAG);
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
    if (mForecastShare != null) {
      mShareActionProvider.setShareIntent(createShareForecastIntent());
    }
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
    mWeatherIcon = (ImageView) rootView.findViewById(R.id.fragment_detail_weather_icon);
    mDayName = (TextView) rootView.findViewById(R.id.fragment_detail_day_name_textview);
    mMonthDate = (TextView) rootView.findViewById(R.id.fragment_detail_month_date_textview);
    mWeather = (TextView) rootView.findViewById(R.id.fragment_detail_weather_textview);
    mHigh = (TextView) rootView.findViewById(R.id.fragment_detail_high_textview);
    mLow = (TextView) rootView.findViewById(R.id.fragment_detail_low_textview);
    mHumidity = (TextView) rootView.findViewById(R.id.fragment_detail_humidity_textview);
    mWind = (TextView) rootView.findViewById(R.id.fragment_detail_wind_textview);
    mPressure = (TextView) rootView.findViewById(R.id.fragment_detail_pressure_textview);

    return rootView;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    getLoaderManager().initLoader(DETAIL_LOADER_ID, null, this);
    super.onActivityCreated(savedInstanceState);
  }
}
