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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

  public static final String LOG_TAG = TestDb.class.getSimpleName();

  void deleteTheDatabase() {
    mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
  }

  // Deleting the database ensures we have a clean test.
  public void setUp() {
    deleteTheDatabase();
  }

  /* Students: Uncomment this test once you've written the code to create the
   * Location table. */
  public void testCreateDb() throws Throwable {
    // build a HashSet of all of the table names we wish to look for
    // Note that there will be another table in the DB that stores the
    // Android metadata (db version information)
    final HashSet<String> tableNameHashSet = new HashSet<>();
    tableNameHashSet.add(WeatherContract.LocationEntry.TABLE_NAME);
    tableNameHashSet.add(WeatherContract.WeatherEntry.TABLE_NAME);

    mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    SQLiteDatabase db = new WeatherDbHelper(
        this.mContext).getWritableDatabase();
    assertEquals(true, db.isOpen());

    // have we created the tables we want?
    Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE " +
        "type='table'", null);

    assertTrue("Error: This means that the database has not been created " +
            "correctly",
        cursor.moveToFirst());

    // verify that the tables have been created
    do {
      tableNameHashSet.remove(cursor.getString(0));
    } while (cursor.moveToNext());

    // if this fails, it means that your database doesn't contain both the
    // location entry and weather entry tables
    assertTrue("Error: Your database was created without both the location " +
            "entry and weather entry tables",
        tableNameHashSet.isEmpty());

    // now, do our tables contain the correct columns?
    cursor = db.rawQuery("PRAGMA table_info(" +
            WeatherContract.LocationEntry.TABLE_NAME + ")",
        null);

    assertTrue("Error: This means that we were unable to query the database" +
            " for table information.",
        cursor.moveToFirst());

    // Build a HashSet of all of the column names we want to look for
    final HashSet<String> locationColumnHashSet = new HashSet<>();
    locationColumnHashSet.add(WeatherContract.LocationEntry._ID);
    locationColumnHashSet.add(WeatherContract.LocationEntry.COL_CITY_NAME);
    locationColumnHashSet.add(WeatherContract.LocationEntry.COL_LATITUDE);
    locationColumnHashSet.add(WeatherContract.LocationEntry.COL_LONGITUDE);
    locationColumnHashSet.add(WeatherContract.LocationEntry.COL_LOC_SETTING);

    int columnNameIndex = cursor.getColumnIndex("name");
    do {
      String columnName = cursor.getString(columnNameIndex);
      locationColumnHashSet.remove(columnName);
    } while (cursor.moveToNext());

    // if this fails, it means that your database doesn't contain all of
    // the required location entry columns
    assertTrue("Error: The database doesn't contain all of the required " +
            "location entry columns",
        locationColumnHashSet.isEmpty());

    cursor.close();
    db.close();
  }

  /* Students:  Here is where you will build code to test that we can insert and
   * query the location database.  We've done a lot of work for you. You'll want
   * to look in TestUtilities where you can uncomment out the
   * "createNorthPoleLocationValues" function.  You can also make use of the
   * ValidateCurrentRecord function from within TestUtilities.*/
  public void testLocationTable() {
    // First step: Get reference to writable database
    mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    SQLiteDatabase db = new WeatherDbHelper(this.mContext)
        .getWritableDatabase();
    assertEquals(true, db.isOpen());

    // Create ContentValues for a location, and insert it into the DB.
    ContentValues values = TestUtilities.createNorthPoleLocationValues();
    long rowId = db
        .insert(WeatherContract.LocationEntry.TABLE_NAME, null, values);
    assertTrue(rowId != -1);

    // Query the database and receive a Cursor back
    Cursor cursor;
    cursor = db.query(WeatherContract.LocationEntry.TABLE_NAME,
        null, null, null, null, null, null);

    // Move the cursor to a valid database row
    assertEquals(true, cursor.moveToFirst());

    // Validate data in resulting Cursor with the original ContentValues
    TestUtilities.validateCurrentRecord(null, cursor, values);

    // Finally, close the cursor and database
    cursor.close();
    db.close();
  }

  /* Students:  Here is where you will build code to test that we can insert
   * and query the database.
   */
  public void testWeatherTable() {
    // First insert the location, and then use the locationRowId to insert
    // the weather. Make sure to cover as many failure cases as you can.
    mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    SQLiteDatabase db = new WeatherDbHelper(this.mContext)
        .getWritableDatabase();
    assertEquals(true, db.isOpen());
    ContentValues locationValues = TestUtilities
        .createNorthPoleLocationValues();
    long locationRow = db.insert(WeatherContract.LocationEntry.TABLE_NAME,
        null, locationValues);
    assertTrue(locationRow != -1);

    // Create ContentValues for the test weather, and insert it into the DB.
    ContentValues weatherValues = TestUtilities
        .createWeatherValues(locationRow);
    long weatherRow = db.insert(WeatherContract.WeatherEntry.TABLE_NAME,
        null, weatherValues);
    assertTrue(weatherRow != -1);

    // Query the database and receive a Cursor back
    Cursor cursor = db.query(WeatherContract.WeatherEntry.TABLE_NAME,
        null, null, null, null, null, null);

    // Move the cursor to a valid database row
    assertEquals(true, cursor.moveToFirst());

    // Test that the values are correct
    TestUtilities.validateCurrentRecord(null, cursor, weatherValues);

    // Finally, close the cursor and database
    cursor.close();
    db.close();
  }
}