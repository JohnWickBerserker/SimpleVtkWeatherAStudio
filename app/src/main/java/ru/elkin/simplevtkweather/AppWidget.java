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

public class AppWidget extends AppWidgetProvider {

    private static final String TAG = "VtkTemWidget";

    private static void setWidgetsIsUpdating(Context context) {
        updateAppWidgets(context, createViewsToSetIsUpdating(context));
    }

    private static void setWidgetsTemperature(Context context, String temperature) {
        updateAppWidgets(context, createViewsToSetTemperature(context, temperature));
    }

    private static void updateAppWidgets(Context context, RemoteViews views) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,  AppWidget.class));
        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }

    private static RemoteViews createViewsToSetTemperature(Context context, String temperature) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
        views.setTextViewText(R.id.tem, temperature);
        views.setTextColor(R.id.tem, ContextCompat.getColor(context, R.color.tem));
        return views;
    }

    private static RemoteViews createViewsToSetIsUpdating(Context context) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
        views.setTextColor(R.id.tem, ContextCompat.getColor(context, R.color.temUpdate));
        return views;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate");
        setOnClickEvent(context, appWidgetManager, appWidgetIds);
        new FetchWeatherData(context).execute();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            Log.d(TAG, "ACTION_USER_PRESENT");
            new FetchWeatherData(context).execute();
        }
    }

    private void setOnClickEvent(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Intent intent = createAppWidgetUpdateIntent(context, appWidgetIds);
        PendingIntent broadcastIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
        views.setOnClickPendingIntent(R.id.widgetBackground, broadcastIntent);

        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }

    private Intent createAppWidgetUpdateIntent(Context context, int[] appWidgetIds) {
        Intent intent = new Intent(context, AppWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        return intent;
    }

    private class FetchWeatherData extends AsyncTask<Void, Void, String> {

        private Context context;
        private String noDataString;

        public FetchWeatherData(Context context) {
            this.context = context;
            this.noDataString = context.getString(R.string.no_data);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setWidgetsIsUpdating(context);
        }

        @Override
        protected String doInBackground(Void... params) {
            return new TemperatureRequester(noDataString).requestTemperature();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            setWidgetsTemperature(context, s);
        }
    }
}

