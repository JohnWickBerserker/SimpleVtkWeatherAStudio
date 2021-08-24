package ru.elkin.simplevtkweather;

import android.util.Log;

import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Scanner;

class TemperatureRequester {
    private static final String TAG = "TemRequester";
    private static final String WEATHER_URL = "http://sensor1-189909.000webhostapp.com/getTem.php";

    private String noDataString;

    public TemperatureRequester(String noDataString) {
        this.noDataString = noDataString;
    }

    public String requestTemperature() {
        try {
            return formatTemperature(requestTemperatureAsFloat());
        }
        catch (Exception ex) {
            Log.e(TAG, "requestTemperature error", ex);
            return noDataString;
        }
    }

    private float requestTemperatureAsFloat() throws Exception {
        InputStream response = new URL(WEATHER_URL).openStream();
        return Float.parseFloat(convertStreamToString(response));
    }

    private String convertStreamToString(InputStream stream)
    {
        Scanner s = new Scanner(stream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private String formatTemperature(float temperature)
    {
        return new DecimalFormat("#.#").format(temperature);
    }
}
