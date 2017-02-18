/*
 * Copyright (C) 20XX The Android Open Source Project
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
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
class ForecastAdapter extends CursorAdapter {
  // private static final String LOG_TAG = ForecastAdapter.class.getSimpleName();

  ForecastAdapter(Context context, Cursor c, int flags) {
    super(context, c, flags);
  }

  /*
      This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
      string.
   */
  private String convertCursorRowToUXFormat(Cursor cursor) {
    // WARNING: We are using the hacky "index" values from ForecastFragment in this method.

    boolean celsius = Utility.isCelsius(mContext);

    return Utility.formatDate(cursor.getLong(ForecastFragment.COL_WEATHER_DATE))
        + " - " + cursor.getString(ForecastFragment.COL_WEATHER_SHORT_DESC) + " - "
        + Utility.formatTemperature(
            cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP), celsius)
        + "/" + Utility.formatTemperature(
            cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP), celsius);
  }

  /*
      Remember that these views are reused as needed.
   */
  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent) {
    return LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);
  }

  /*
      This is where we fill-in the views with the contents of the cursor.
   */
  @Override
  public void bindView(View view, Context context, Cursor cursor) {
    // our view is pretty simple here --- just a text view
    // we'll keep the UI functional with a simple (and slow!) binding.

    //TextView tv = (TextView) view;
    //tv.setText(convertCursorRowToUXFormat(cursor));
  }
}