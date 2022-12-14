package com.example.android.sunshine.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.example.android.sunshine.app.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

/* Students: These are functions and some test data to make it easier to test
 * your database and Content Provider. */
public class TestUtilities extends AndroidTestCase {
  static final String TEST_LOCATION = "99705";
  static final long TEST_DATE = 1419033600L;  // December 20th, 2014

  static void validateCursor(String error, Cursor valueCursor,
      ContentValues expectedValues) {
    assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
    validateCurrentRecord(error, valueCursor, expectedValues);
    valueCursor.close();
  }

  static void validateCurrentRecord(String error, Cursor valueCursor,
      ContentValues expectedValues) {
    Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
    for (Map.Entry<String, Object> entry : valueSet) {
      String columnName = entry.getKey();
      int idx = valueCursor.getColumnIndex(columnName);
      assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
      String expectedValue = entry.getValue().toString();
      assertEquals("Value '" + entry.getValue().toString() +
          "' did not match the expected value '" + expectedValue
          + "'. " + error, expectedValue, valueCursor.getString(idx));
    }
  }

  /**
   * Creates dummy weather data for testing.
   */
  static ContentValues createWeatherValues(long locationRowId) {
    ContentValues weatherValues = new ContentValues();
    weatherValues.put(WeatherContract.WeatherEntry.COL_LOC_KEY, locationRowId);
    weatherValues.put(WeatherContract.WeatherEntry.COL_DATE, TEST_DATE);
    weatherValues.put(WeatherContract.WeatherEntry.COL_DEGREES, 1.1);
    weatherValues.put(WeatherContract.WeatherEntry.COL_HUMIDITY, 1.2);
    weatherValues.put(WeatherContract.WeatherEntry.COL_PRESSURE, 1.3);
    weatherValues.put(WeatherContract.WeatherEntry.COL_MAX_TEMP, 75);
    weatherValues.put(WeatherContract.WeatherEntry.COL_MIN_TEMP, 65);
    weatherValues.put(WeatherContract.WeatherEntry.COL_SHORT_DESC, "Asteroids");
    weatherValues.put(WeatherContract.WeatherEntry.COL_WIND_SPEED, 5.5);
    weatherValues.put(WeatherContract.WeatherEntry.COL_WEATHER_ID, 321);

    return weatherValues;
  }

  /**
   * Creates dummy location data. The data created is for North Pole, Alaska.
   */
  static ContentValues createNorthPoleLocationValues() {
    ContentValues values = new ContentValues();
    values.put(WeatherContract.LocationEntry.COL_LOC_SETTING, TEST_LOCATION);
    values.put(WeatherContract.LocationEntry.COL_CITY_NAME, "North Pole");
    values.put(WeatherContract.LocationEntry.COL_LATITUDE, 64.7488);
    values.put(WeatherContract.LocationEntry.COL_LONGITUDE, -147.353);

    return values;
  }

  static long insertNorthPoleLocationValues(Context context) {
    // insert our test records into the database
    WeatherDbHelper dbHelper = new WeatherDbHelper(context);
    SQLiteDatabase db = dbHelper.getWritableDatabase();
    ContentValues testValues = TestUtilities.createNorthPoleLocationValues();

    long locationRowId;
    locationRowId = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null,
        testValues);

    // Verify we got a row back.
    assertTrue("Error: Failure to insert North Pole Location Values",
        locationRowId != -1);

    return locationRowId;
  }

  /* Students: The functions we provide inside of TestProvider use this utility
   * class to test the ContentObserver callbacks using the PollingCheck class
   * that we grabbed from the Android CTS tests. Note that this only tests that
   * the onChange function is called; it does not test that the correct Uri is
   * returned. */
  static class TestContentObserver extends ContentObserver {
    final HandlerThread mHT;
    boolean mContentChanged;

    static TestContentObserver getTestContentObserver() {
      HandlerThread ht = new HandlerThread("ContentObserverThread");
      ht.start();
      return new TestContentObserver(ht);
    }

    private TestContentObserver(HandlerThread ht) {
      super(new Handler(ht.getLooper()));
      mHT = ht;
    }

    // On earlier versions of Android, this onChange method is called
    @Override
    public void onChange(boolean selfChange) {
      onChange(selfChange, null);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
      mContentChanged = true;
    }

    public void waitForNotificationOrFail() {
      new PollingCheck(5000) {
        @Override
        protected boolean check() {
          return mContentChanged;
        }
      }.run();
      mHT.quit();
    }
  }

  static TestContentObserver getTestContentObserver() {
    return TestContentObserver.getTestContentObserver();
  }
}