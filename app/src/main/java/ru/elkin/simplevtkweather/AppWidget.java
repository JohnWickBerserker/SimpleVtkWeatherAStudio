package ru.elkin.simplevtkweather;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;

import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Scanner;

/**
 * Implementation of App Widget functionality.
 */
public class AppWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, String temText) {

        //CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
        //views.setTextViewText(R.id., widgetText);

        views.setTextViewText(R.id.tem, temText);
        views.setTextColor(R.id.tem, ContextCompat.getColor(context, R.color.tem));

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    static void showTemUpdating(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId)
    {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
        views.setTextColor(R.id.tem, ContextCompat.getColor(context, R.color.temUpdate));
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        Log.d("widget", "onUpdate");

        /*// There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, "0");
        }*/

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
        registerClicks(context, appWidgetIds, views);
        for (int appWidgetId : appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

        new FetchWeatherData(context, appWidgetManager, appWidgetIds).execute();
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context, intent);
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT))
        {
            Log.d("widget", "ACTION_USER_PRESENT");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,  AppWidget.class));
            new FetchWeatherData(context, appWidgetManager, appWidgetIds).execute();
        }
    }

    private void registerClicks(Context context, int[] appWidgetIds, RemoteViews widgetView)
    {
        Intent intent = new Intent(context, AppWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

        // Register click event for the Background
        PendingIntent piBackground = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        widgetView.setOnClickPendingIntent(R.id.widgetBackground, piBackground);
    }

    private class FetchWeatherData extends AsyncTask<Void, Void, String> {

        private final String weatherUri = "http://sensor1-189909.000webhostapp.com/getTem.php";

        private Context _context;
        private AppWidgetManager _appWidgetManager;
        private int[] _appWidgetIds;

        private String _noDataString;

        public FetchWeatherData(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
        {
            _context = context;
            _appWidgetManager = appWidgetManager;
            _appWidgetIds = appWidgetIds;
            _noDataString = context.getString(R.string.no_data);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            for (int appWidgetId : _appWidgetIds) {
                showTemUpdating(_context, _appWidgetManager, appWidgetId);
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return new DecimalFormat("#.#").format(requestTem());
            }
            catch (Exception ex) {
                Log.e("widget", ex.toString());
                return _noDataString;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            for (int appWidgetId : _appWidgetIds) {
                updateAppWidget(_context, _appWidgetManager, appWidgetId, s);
            }
        }

        private float requestTem() throws Exception {
            InputStream response = new URL(weatherUri).openStream();
            Scanner s = new Scanner(response).useDelimiter("\\A");
            String result = s.hasNext() ? s.next() : "";
            return Float.parseFloat(result);
        }
    }
}

