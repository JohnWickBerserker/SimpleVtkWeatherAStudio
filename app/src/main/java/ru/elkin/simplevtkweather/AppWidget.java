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

    static void updateWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, String temperature) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);

        views.setTextViewText(R.id.tem, temperature);
        views.setTextColor(R.id.tem, ContextCompat.getColor(context, R.color.tem));

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    static void setWidgetIsUpdating(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId)
    {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
        views.setTextColor(R.id.tem, ContextCompat.getColor(context, R.color.temUpdate));
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    static void updateWidgets(Context context, String temperature)
    {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,  AppWidget.class));
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId, temperature);
        }
    }

    static void setWidgetsIsUpdating(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,  AppWidget.class));
        for (int appWidgetId : appWidgetIds) {
            setWidgetIsUpdating(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate");
        setOnClickEvent(context, appWidgetManager, appWidgetIds);
        new FetchWeatherData(context).execute();
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context, intent);
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT))
        {
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

        public FetchWeatherData(Context context)
        {
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
            updateWidgets(context, s);
        }
    }
}

