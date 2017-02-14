package com.example.android.sunshine.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {

  private final String FORECASTFRAGMENT_TAG = "FFTAG";

  private String mLocation;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    mLocation = Utility.getPreferredLocation(this);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    if (savedInstanceState == null) {
      getSupportFragmentManager().beginTransaction()
          .add(R.id.container, new ForecastFragment(), FORECASTFRAGMENT_TAG)
          .commit();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    String location = Utility.getPreferredLocation(this);
    if (location != null && !location.equals(mLocation)) {
      mLocation = location;
      ForecastFragment fragment = (ForecastFragment)getSupportFragmentManager()
          .findFragmentByTag(FORECASTFRAGMENT_TAG);
      if (fragment != null) {
        fragment.onLocationChanged();
      }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here.
    int id = item.getItemId();

    if (id == R.id.action_settings) {
      startActivity(new Intent(this, SettingsActivity.class));
      return true;
    }

    return super.onOptionsItemSelected(item);
  }


}
