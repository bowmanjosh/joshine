/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.example.android.sunshine.app.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

public class WeatherProvider extends ContentProvider {

  // static final String LOG_TAG = WeatherProvider.class.getSimpleName();

  // The URI Matcher used by this content provider.
  private static final UriMatcher sUriMatcher = buildUriMatcher();
  private WeatherDbHelper mOpenHelper;

  // Some codes for the content provider (??)
  static final int WEATHER = 100;
  static final int WEATHER_WITH_LOCATION = 101;
  static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
  static final int LOCATION = 300;

  private static final SQLiteQueryBuilder sWeatherByLocationSettingQueryBuilder;

  static {
    sWeatherByLocationSettingQueryBuilder = new SQLiteQueryBuilder();

    sWeatherByLocationSettingQueryBuilder.setTables(
        WeatherContract.WeatherEntry.TABLE_NAME + " INNER JOIN "
            + WeatherContract.LocationEntry.TABLE_NAME + " ON "
            + WeatherContract.WeatherEntry.TABLE_NAME + "."
            + WeatherContract.WeatherEntry.COL_LOC_KEY + " = "
            + WeatherContract.LocationEntry.TABLE_NAME + "."
            + WeatherContract.LocationEntry._ID);
  }

  private static final String sLocationSettingSelection =
      WeatherContract.LocationEntry.TABLE_NAME + "."
          + WeatherContract.LocationEntry.COL_LOC_SETTING + " = ? ";

  private static final String sLocationSettingWithStartDateSelection =
      WeatherContract.LocationEntry.TABLE_NAME + "."
          + WeatherContract.LocationEntry.COL_LOC_SETTING + " = ? AND "
          + WeatherContract.WeatherEntry.COL_DATE + " >= ? ";

  private static final String sLocationSettingAndDaySelection =
      WeatherContract.LocationEntry.TABLE_NAME + "."
          + WeatherContract.LocationEntry.COL_LOC_SETTING + " = ? AND "
          + WeatherContract.WeatherEntry.COL_DATE + " = ? ";

  private Cursor getWeatherByLocationSetting(Uri uri, String[] projection,
      String sortOrder) {

    String locationSetting = WeatherContract.WeatherEntry
        .getLocationSettingFromUri(uri);
    long startDate = WeatherContract.WeatherEntry.getStartDateFromUri(uri);
    String[] selectionArgs;
    String selection;
    if (startDate == 0) {
      selection = sLocationSettingSelection;
      selectionArgs = new String[]{locationSetting};
    } else {
      selectionArgs = new String[]{locationSetting, Long.toString(startDate)};
      selection = sLocationSettingWithStartDateSelection;
    }

    return sWeatherByLocationSettingQueryBuilder.query(
        mOpenHelper.getReadableDatabase(),
        projection,
        selection,
        selectionArgs,
        null,
        null,
        sortOrder
    );
  }

  private Cursor getWeatherByLocationSettingAndDate(Uri uri,
      String[] projection, String sortOrder) {

    String locationSetting = WeatherContract.WeatherEntry
        .getLocationSettingFromUri(uri);
    long date = WeatherContract.WeatherEntry.getDateFromUri(uri);

    return sWeatherByLocationSettingQueryBuilder.query(
        mOpenHelper.getReadableDatabase(),
        projection,
        sLocationSettingAndDaySelection,
        new String[]{locationSetting, Long.toString(date)},
        null,
        null,
        sortOrder
    );
  }

  /**
   * Students: Here is where you need to create the UriMatcher. This UriMatcher
   * will match each URI to the WEATHER, WEATHER_WITH_LOCATION,
   * WEATHER_WITH_LOCATION_AND_DATE, and LOCATION integer constants defined
   * above.  You can test this by uncommenting the testUriMatcher test within
   * TestUriMatcher.
   */
  static UriMatcher buildUriMatcher() {
    UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY,
        WeatherContract.PATH_WEATHER,
        WEATHER);
    uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY,
        WeatherContract.PATH_WEATHER + "/*",
        WEATHER_WITH_LOCATION);
    uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY,
        WeatherContract.PATH_WEATHER + "/*/#",
        WEATHER_WITH_LOCATION_AND_DATE);
    uriMatcher.addURI(WeatherContract.CONTENT_AUTHORITY,
        WeatherContract.PATH_LOCATION,
        LOCATION);

    return uriMatcher;
  }

  @Override
  public boolean onCreate() {
    mOpenHelper = new WeatherDbHelper(getContext());
    return true;
  }

  /**
   * Students: Here's where you'll code the getType function that uses the
   * UriMatcher. You can test this by uncommenting testGetType in TestProvider.
   */
  @Override
  public String getType(@NonNull Uri uri) {

    // Use the Uri Matcher to determine what kind of URI this is.
    final int match = sUriMatcher.match(uri);

    switch (match) {
      // Student: Uncomment and fill out these two cases
      case WEATHER_WITH_LOCATION_AND_DATE:
        return WeatherContract.WeatherEntry.CONTENT_ITEM_TYPE;
      case WEATHER_WITH_LOCATION:
        return WeatherContract.WeatherEntry.CONTENT_TYPE;
      case WEATHER:
        return WeatherContract.WeatherEntry.CONTENT_TYPE;
      case LOCATION:
        return WeatherContract.LocationEntry.CONTENT_TYPE;
      default:
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }
  }

  @Override
  public Cursor query(@NonNull Uri uri, String[] projection, String selection,
      String[] selectionArgs, String sortOrder) {

    Cursor retCursor;
    switch (sUriMatcher.match(uri)) {
      // "weather/*/*"
      case WEATHER_WITH_LOCATION_AND_DATE: {
        retCursor = getWeatherByLocationSettingAndDate(uri, projection,
            sortOrder);
        break;
      }
      // "weather/*"
      case WEATHER_WITH_LOCATION: {
        retCursor = getWeatherByLocationSetting(uri, projection, sortOrder);
        break;
      }
      // "weather"
      case WEATHER: {
        retCursor = mOpenHelper.getReadableDatabase().query(
            WeatherContract.WeatherEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder
        );
        break;
      }
      // "location"
      case LOCATION: {
        retCursor = mOpenHelper.getReadableDatabase().query(
            WeatherContract.LocationEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder
        );
        break;
      }
      default: {
        throw new UnsupportedOperationException("Unknown uri: " + uri);
      }
    }
    Context context = getContext();
    if (context != null) {
      retCursor.setNotificationUri(context.getContentResolver(), uri);
    }
    return retCursor;
  }

  /**
   * Student: Add the ability to insert Locations to the implementation of this
   * function.
   */
  @Override
  public Uri insert(@NonNull Uri uri, ContentValues values) {
    final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    final int match = sUriMatcher.match(uri);
    Uri returnUri;

    switch (match) {
      case WEATHER: {
        normalizeDate(values);
        long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null,
            values);
        if (_id >= 0) {
          returnUri = WeatherContract.WeatherEntry.buildWeatherUri(_id);
        } else {
          throw new android.database.SQLException("Failed to insert row into "
              + uri);
        }
        break;
      }
      case LOCATION: {
        long _id = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null,
            values);
        if (_id >= 0) {
          returnUri = WeatherContract.LocationEntry.buildLocationUri(_id);
        } else {
          throw new android.database.SQLException("Failed to insert row into "
              + uri);
        }
        break;
      }
      default: {
        throw new UnsupportedOperationException("Unknown uri: " + uri);
      }
    }

    /*
      Reminder: notifyChange() needs to be passed the Uri that was passed
      *into* this function, not the one we have constructed to return. If we
      use the one we plan to return, listeners will not be notified.
     */
    Context context = getContext();
    if (context != null) {
      context.getContentResolver().notifyChange(uri, null);
    }
    return returnUri;
  }

  @Override
  public int delete(@NonNull Uri uri, String selection, String[]
      selectionArgs) {

    final String tableName;
    switch (sUriMatcher.match(uri)) {
      case WEATHER: {
        tableName = WeatherContract.WeatherEntry.TABLE_NAME;
        break;
      }
      case LOCATION: {
        tableName = WeatherContract.LocationEntry.TABLE_NAME;
        break;
      }
      default: {
        throw new UnsupportedOperationException("Unsupported Uri: " + uri);
      }
    }

    // Student: A null value deletes all rows.  In my implementation of this,
    // I only notified the uri listeners (using the content resolver) if the
    // rowsDeleted != 0 or the selection is null. Oh, and you should notify the
    // listeners here.
    final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    if (selection == null) {
      selection = "1";
    }
    final int rowsDeleted = db.delete(tableName, selection, selectionArgs);

    if (rowsDeleted > 0) {
      Context context = getContext();
      if (context != null) {
        context.getContentResolver().notifyChange(uri, null);
      }
    }

    return rowsDeleted;
  }

  private void normalizeDate(ContentValues values) {
    // normalize the date value
    if (values.containsKey(WeatherContract.WeatherEntry.COL_DATE)) {
      long dateValue = values.getAsLong(WeatherContract.WeatherEntry
          .COL_DATE);
      values.put(WeatherContract.WeatherEntry.COL_DATE, WeatherContract
          .normalizeDate(dateValue));
    }
  }

  @Override
  public int update(@NonNull Uri uri, ContentValues values, String selection,
      String[] selectionArgs) {
    // Student: This is a lot like the delete function.  We return the number
    // of rows impacted
    // by the update.

    final String tableName;
    switch (sUriMatcher.match(uri)) {
      case WEATHER: {
        tableName = WeatherContract.WeatherEntry.TABLE_NAME;
        break;
      }
      case LOCATION: {
        tableName = WeatherContract.LocationEntry.TABLE_NAME;
        break;
      }
      default: {
        throw new UnsupportedOperationException("Unsupported Uri: " + uri);
      }
    }

    // This chain goes all the way to the SQLite update() call, which returns
    // an int, which is the number of rows we affected with the call.
    int rowsUpdated = mOpenHelper.getWritableDatabase().update(tableName,
        values,
        selection, selectionArgs);
    if (rowsUpdated > 0) {
      Context context = getContext();
      if (context != null) {
        context.getContentResolver().notifyChange(uri, null);
      }

    }
    return rowsUpdated;
  }

  @Override
  public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
    final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    final int match = sUriMatcher.match(uri);
    switch (match) {
      case WEATHER:
        db.beginTransaction();
        int returnCount = 0;
        try {
          for (ContentValues value : values) {
            normalizeDate(value);
            long _id = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null,
                value);
            if (_id != -1) {
              returnCount++;
            }
          }
          db.setTransactionSuccessful();
        } finally {
          db.endTransaction();
        }
        Context context = getContext();
        if (context != null) {
          context.getContentResolver().notifyChange(uri, null);
        }
        return returnCount;

      default:
        return super.bulkInsert(uri, values);
    }
  }

  // You do not need to call this method. This is a method specifically to
  // assist the testing framework in running smoothly.
  @Override
  public void shutdown() {
    mOpenHelper.close();
    super.shutdown();
  }
}