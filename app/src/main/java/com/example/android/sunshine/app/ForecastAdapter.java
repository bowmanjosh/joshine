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
import android.media.Image;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
    // Use placeholder image. Later we will use weather ID to set an icon.
    ImageView icon = (ImageView) view.findViewById(R.id.list_item_icon);
    icon.setImageResource(R.drawable.ic_launcher);

    // This just displays the date in milliseconds. Later, we will format the date more nicely.
    TextView date = (TextView) view.findViewById(R.id.list_item_date_textview);
    date.setText(Utility.getFriendlyDayString(context,
        cursor.getLong(ForecastFragment.COL_WEATHER_DATE)));

    TextView forecast = (TextView) view.findViewById(R.id.list_item_forecast_textview);
    forecast.setText(cursor.getString(ForecastFragment.COL_WEATHER_SHORT_DESC));

    // Display temperatures as valueOf(Double) for now. Later: format them more nicely.
    boolean isCelsius = Utility.isCelsius(context);
    TextView high = (TextView) view.findViewById(R.id.list_item_high_textview);
    high.setText(Utility.formatTemperature(cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP),
        isCelsius));
    TextView low = (TextView) view.findViewById(R.id.list_item_low_textview);
    low.setText(Utility.formatTemperature(cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP),
        isCelsius));
  }
}