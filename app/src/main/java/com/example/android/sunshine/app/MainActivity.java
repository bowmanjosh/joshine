package com.example.android.sunshine.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity {

  private static final String DETAILFRAGMENT_TAG = "DFTAG";

  private String mLocation;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mLocation = Utility.getPreferredLocation(this);
    setContentView(R.layout.activity_main);

    // This 'if' checks if we are in tablet mode.
    if (findViewById(R.id.weather_detail_container) != null) {
      if (savedInstanceState == null) {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.weather_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
            .commit();
      }
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    String location = Utility.getPreferredLocation(this);
    if (location != null && !location.equals(mLocation)) {
      mLocation = location;
      ForecastFragment fragment = (ForecastFragment)getSupportFragmentManager()
          .findFragmentById(R.id.fragment_forecast);
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
