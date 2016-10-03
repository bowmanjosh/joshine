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

import com.example.android.sunshine.app.data.WeatherContract;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {
  public ForecastAdapter(Context context, Cursor c, int flags) {
    super(context, c, flags);
  }

  /**
   * Prepare the weather high/lows for presentation.
   */
  private String formatHighLows(double high, double low) {
    boolean isMetric = Utility.isMetric(mContext);
    String highLowStr = Utility.formatTemperature(high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
    return highLowStr;
  }

  /*
      This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
      string.
   */
  private String convertCursorRowToUXFormat(Cursor cursor) {
    // get row indices for our cursor
    int idx_max_temp = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP);
    int idx_min_temp = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP);
    int idx_date = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
    int idx_short_desc = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC);

    String highAndLow = formatHighLows(
        cursor.getDouble(idx_max_temp),
        cursor.getDouble(idx_min_temp));

    return Utility.formatDate(cursor.getLong(idx_date)) +
        " - " + cursor.getString(idx_short_desc) +
        " - " + highAndLow;
  }

  /*
      Remember that these views are reused as needed.
   */
  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent) {
    View view = LayoutInflater.from(context).inflate(R.layout.list_item_forecast, parent, false);

    return view;
  }

  /*
      This is where we fill-in the views with the contents of the cursor.
   */
  @Override
  public void bindView(View view, Context context, Cursor cursor) {
    // our view is pretty simple here --- just a text view
    // we'll keep the UI functional with a simple (and slow!) binding.

    TextView tv = (TextView)view;
    tv.setText(convertCursorRowToUXFormat(cursor));
  }
}