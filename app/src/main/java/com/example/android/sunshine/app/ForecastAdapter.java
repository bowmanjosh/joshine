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
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
class ForecastAdapter extends CursorAdapter {
  // private static final String LOG_TAG = ForecastAdapter.class.getSimpleName();

  private final int VIEW_TYPE_COUNT = 2;
  private final int VIEW_TYPE_TODAY = 0;
  private final int VIEW_TYPE_FUTURE_DAY = 1;

  private static class ListItemViewHolder {
    final ImageView icon;
    final TextView date;
    final TextView forecast;
    final TextView high;
    final TextView low;

    ListItemViewHolder(View view) {
      icon = view.findViewById(R.id.list_item_icon);
      date = view.findViewById(R.id.list_item_date_textview);
      forecast = view.findViewById(R.id.list_item_forecast_textview);
      high = view.findViewById(R.id.list_item_high_textview);
      low = view.findViewById(R.id.list_item_low_textview);
    }
  }

  ForecastAdapter(Context context, Cursor c, int flags) {
    super(context, c, flags);
  }

  @Override
  public int getItemViewType(int position) {
    if (position == 0) {
      return VIEW_TYPE_TODAY;
    } else {
      return VIEW_TYPE_FUTURE_DAY;
    }
  }

  @Override
  public int getViewTypeCount() {
    return VIEW_TYPE_COUNT;
  }

  /*
      Remember that these views are reused as needed.
   */
  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent) {
    final int layoutId;
    if (getItemViewType(cursor.getPosition()) == VIEW_TYPE_TODAY) {
      layoutId = R.layout.list_item_forecast_today;
    } else {
      layoutId = R.layout.list_item_forecast;
    }

    View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
    ListItemViewHolder viewHolder = new ListItemViewHolder(view);
    view.setTag(viewHolder);

    return view;
  }

  /*
      This is where we fill-in the views with the contents of the cursor.
   */
  @Override
  public void bindView(View view, Context context, Cursor cursor) {
    ListItemViewHolder viewHolder = (ListItemViewHolder) view.getTag();

    // Use placeholder image. Later we will use weather ID to set an icon.
    viewHolder.icon.setImageResource(R.mipmap.ic_launcher);

    viewHolder.date.setText(Utility.getFriendlyDayString(context,
        cursor.getLong(ForecastFragment.COL_WEATHER_DATE)));
    viewHolder.forecast.setText(cursor.getString(ForecastFragment.COL_WEATHER_SHORT_DESC));

    // Display temperatures as valueOf(Double) for now. Later: format them more nicely.
    boolean isCelsius = Utility.isCelsius(context);
    viewHolder.high.setText(Utility.formatTemperature(context,
        cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP), isCelsius));
    viewHolder.low.setText(Utility.formatTemperature(context,
        cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP), isCelsius));
  }
}