package ru.elkin.simplevtkweather;

import android.util.Log;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.Scanner;

class TemperatureRequester {
    private static final String TAG = "TemRequester";
    private static final String WEATHER_URL = "http://weatherstation.ezyro.com/getTem.php";

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
        String firstGet = readFromUrl(WEATHER_URL);
        String _test = firstGet.substring(firstGet.indexOf("_test=") + 6, firstGet.indexOf("_test=") + 6 + 32);
        _test = "909c5aee2437fd65faa5f7913e7f100a";

        URLConnection con = new URL(WEATHER_URL).openConnection();
        con.setRequestProperty("Cookie", "_test" + _test);
        InputStream response = con.getInputStream();
        return Float.parseFloat(convertStreamToString(response));
    }

    private String readFromUrl(String url) throws Exception {
        InputStream response = new URL(url).openStream();
        return convertStreamToString(response);
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
