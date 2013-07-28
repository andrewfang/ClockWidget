package com.andrewbfang.clockwidget;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class clockWidgetProvider extends AppWidgetProvider {

    private DateFormat t = new SimpleDateFormat("h:mm");
    private DateFormat d = new SimpleDateFormat("EEE, MMM d");
    private final String clocks[][] = {
            {"HTC", "com.htc.android.worldclock", "com.htc.android.worldclock.WorldClockTabControl" },
            {"Standard", "com.android.deskclock", "com.android.deskclock.AlarmClock"},
            {"Nexus", "com.google.android.deskclock", "com.android.deskclock.DeskClock"},
            {"Motorola", "com.motorola.blur.alarmclock",  "com.motorola.blur.alarmclock.AlarmClock"},
            {"Samsung", "com.sec.android.app.clockpackage","com.sec.android.app.clockpackage.ClockPackage"}
    };

    public void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        PackageManager packageManager = context.getPackageManager();

        // To launch Clock
        boolean foundClock = false;
        Intent clockIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        for (String[] testClock : clocks) {
            if (!foundClock) {
                try {
                    ComponentName cn = new ComponentName(testClock[1], testClock[2]);
                    ActivityInfo aInfo = packageManager.getActivityInfo(cn, PackageManager.GET_META_DATA);
                    clockIntent.setComponent(cn);
                    foundClock = true;
                } catch (Exception e) {
                    //Clock is not the right one. Try next.
                }
            } else {
                break;
            }
        }
        PendingIntent pendingClockIntent = PendingIntent.getActivity(context, 0, clockIntent, 0);


        // To Launch Calendar
        Intent calIntent = new Intent(Intent.ACTION_MAIN);
        ComponentName calCN;
        try {
            calCN = new ComponentName("com.android.calendar", "com.android.calendar.LaunchActivity");
            ActivityInfo aInfo2 = packageManager.getActivityInfo(calCN, PackageManager.GET_META_DATA);
        } catch (Exception e) {
            calCN = new ComponentName("com.google.android.calendar", "com.android.calendar.LaunchActivity");
        }
        calIntent.setComponent(calCN);
        PendingIntent pendingCalIntent = PendingIntent.getActivity(context, 1, calIntent, 0);

        // Update the clock views
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        views.setOnClickPendingIntent(R.id.time, pendingClockIntent);
        views.setOnClickPendingIntent(R.id.date, pendingCalIntent);
        views.setTextViewText(R.id.time, t.format(new Date()));
        views.setTextViewText(R.id.date, d.format(new Date()));
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (CLOCK_WIDGET_UPDATE.equals(intent.getAction())) {
            ComponentName thisAppWidget = new ComponentName(context.getPackageName(), getClass().getName());
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int ids[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
            for (int appWidgetID: ids) {
                updateAppWidget(context, appWidgetManager, appWidgetID);
            }
        }
    }

    private PendingIntent createClockTickIntent(Context context) {
        Intent intent = new Intent(CLOCK_WIDGET_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 1);
        alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), 10000, createClockTickIntent(context));
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(createClockTickIntent(context));
    }

    public static String CLOCK_WIDGET_UPDATE = "com.andrewbfang.watchWidget.WATCH_WIDGET_UPDATE";
}
