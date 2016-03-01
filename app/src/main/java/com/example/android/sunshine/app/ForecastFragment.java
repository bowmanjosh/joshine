package com.example.android.sunshine.app;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Fragment inside of MainActivity. Displays the weather forecast entries.
 *
 * @see MainActivity
 */
public class ForecastFragment extends Fragment {

    private ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Fake weather data, for prototyping
        ArrayList<String> fakeWeather = new ArrayList<>();
        fakeWeather.add("Monday COLD");
        fakeWeather.add("Tuesday COLDER");
        fakeWeather.add("Wednesday WARM");
        fakeWeather.add("Thursday 650");
        fakeWeather.add("Fri partly farty");
        fakeWeather.add("Sat raining squirrels");
        fakeWeather.add("Sun global warming");
        mForecastAdapter = new ArrayAdapter<>(
                getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                fakeWeather);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);

        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            return null;
        }
    }
}
