package com.byagowi.persiancalendar.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.byagowi.persiancalendar.BuildConfig;
import com.byagowi.persiancalendar.Constants;
import com.byagowi.persiancalendar.R;
import com.byagowi.persiancalendar.Widget1x1;
import com.byagowi.persiancalendar.Widget2x2;
import com.byagowi.persiancalendar.Widget4x1;
import com.byagowi.persiancalendar.entity.AbstractEvent;
import com.byagowi.persiancalendar.enums.CalendarTypeEnum;
import com.byagowi.persiancalendar.view.activity.MainActivity;
import com.github.praytimes.Clock;
import com.google.android.apps.dashclock.api.ExtensionData;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.core.app.NotificationCompat;
import calendar.AbstractDate;

public class UpdateUtils {
    private static final int NOTIFICATION_ID = 1001;
    private static AbstractDate pastDate;
    private static ExtensionData mExtensionData;

    public static void update(Context context, boolean updateDate) {
        Log.d("UpdateUtils", "update");
        Calendar calendar = Utils.makeCalendarFromDate(new Date());
        CalendarTypeEnum mainCalendar = Utils.getMainCalendar();
        AbstractDate date = Utils.getTodayOfCalendar(mainCalendar);
        long jdn = Utils.getJdnDate(date);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent launchAppPendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        //
        // Widgets
        //
        //
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        String colorInt = Utils.getSelectedWidgetTextColor();
        int color = Color.parseColor(colorInt);

        // Widget 1x1
        ComponentName widget1x1 = new ComponentName(context, Widget1x1.class),
                widget4x1 = new ComponentName(context, Widget4x1.class),
                widget2x2 = new ComponentName(context, Widget2x2.class);

        if (manager.getAppWidgetIds(widget1x1).length != 0) {
            RemoteViews remoteViews1 = new RemoteViews(context.getPackageName(), R.layout.widget1x1);
            remoteViews1.setTextColor(R.id.textPlaceholder1_1x1, color);
            remoteViews1.setTextColor(R.id.textPlaceholder2_1x1, color);
            remoteViews1.setTextViewText(R.id.textPlaceholder1_1x1,
                    Utils.formatNumber(date.getDayOfMonth()));
            remoteViews1.setTextViewText(R.id.textPlaceholder2_1x1,
                    Utils.getMonthName(date));
            remoteViews1.setOnClickPendingIntent(R.id.widget_layout1x1, launchAppPendingIntent);
            manager.updateAppWidget(widget1x1, remoteViews1);
        }

        if (pastDate == null || !pastDate.equals(date) || updateDate) {
            Log.d("UpdateUtils", "date has changed");
            pastDate = date;

            Utils.initUtils(context);
            updateDate = true;
        }

        String weekDayName = Utils.getWeekDayName(date);
        String status = Utils.getMonthName(date);
        String title = Utils.dayTitleSummary(date);
        String subtitle = Utils.dateStringOfOtherCalendar(mainCalendar, jdn);

        Clock currentClock =
                new Clock(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        String owghat = Utils.getNextOwghatTime(context, currentClock, updateDate);
        if (!TextUtils.isEmpty(owghat)) {
            String cityName = Utils.getCityName(context, false);
            if (!TextUtils.isEmpty(cityName)) {
                owghat = owghat + " (" + cityName + ")";
            }
        }

        if (manager.getAppWidgetIds(widget4x1).length != 0 ||
                manager.getAppWidgetIds(widget2x2).length != 0) {
            RemoteViews remoteViews4, remoteViews2;
            boolean enableClock = Utils.isWidgetClock() && Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1;
            if (enableClock) {
                if (!Utils.isIranTime()) {
                    remoteViews4 = new RemoteViews(context.getPackageName(), R.layout.widget4x1_clock);
                    remoteViews2 = new RemoteViews(context.getPackageName(), R.layout.widget2x2_clock);
                } else {
                    remoteViews4 = new RemoteViews(context.getPackageName(), R.layout.widget4x1_clock_iran);
                    remoteViews2 = new RemoteViews(context.getPackageName(), R.layout.widget2x2_clock_iran);
                }
            } else {
                remoteViews4 = new RemoteViews(context.getPackageName(), R.layout.widget4x1);
                remoteViews2 = new RemoteViews(context.getPackageName(), R.layout.widget2x2);
            }

            String mainDateString = Utils.dateToString(date);

            {
                // Widget 4x1
                remoteViews4.setTextColor(R.id.textPlaceholder1_4x1, color);
                remoteViews4.setTextColor(R.id.textPlaceholder2_4x1, color);
                remoteViews4.setTextColor(R.id.textPlaceholder3_4x1, color);

                String text2;
                String text3 = "";

                if (enableClock) {
                    text2 = title;
                    if (Utils.isIranTime()) {
                        text3 = "(" + context.getString(R.string.iran_time) + ")";
                    }
                } else {
                    remoteViews4.setTextViewText(R.id.textPlaceholder1_4x1, weekDayName);
                    text2 = mainDateString;
                }
                text2 += Utils.getComma() + " " + subtitle;

                remoteViews4.setTextViewText(R.id.textPlaceholder2_4x1, text2);
                remoteViews4.setTextViewText(R.id.textPlaceholder3_4x1, text3);
                remoteViews4.setOnClickPendingIntent(R.id.widget_layout4x1, launchAppPendingIntent);
                manager.updateAppWidget(widget4x1, remoteViews4);
            }

            {
                String text2;
                // Widget 2x2
                remoteViews2.setTextColor(R.id.time_2x2, color);
                remoteViews2.setTextColor(R.id.date_2x2, color);
                remoteViews2.setTextColor(R.id.event_2x2, color);
                remoteViews2.setTextColor(R.id.owghat_2x2, color);

                if (enableClock) {
                    text2 = title;
                } else {
                    remoteViews2.setTextViewText(R.id.time_2x2, weekDayName);
                    text2 = mainDateString;
                }

                if (updateDate) {
                    List<AbstractEvent> events = Utils.getEvents(jdn);
                    String holidays = Utils.getEventsTitle(events, true);

                    if (!TextUtils.isEmpty(holidays)) {
                        remoteViews2.setTextViewText(R.id.holiday_2x2, holidays);
                        remoteViews2.setViewVisibility(R.id.holiday_2x2, View.VISIBLE);
                    } else {
                        remoteViews2.setViewVisibility(R.id.holiday_2x2, View.GONE);
                    }

                    String nonHolidays = Utils.getEventsTitle(events, false);

                    if (!TextUtils.isEmpty(nonHolidays)) {
                        remoteViews2.setTextViewText(R.id.event_2x2, nonHolidays);
                        remoteViews2.setViewVisibility(R.id.event_2x2, View.VISIBLE);
                    } else {
                        remoteViews2.setViewVisibility(R.id.event_2x2, View.GONE);
                    }
                }

                if (!TextUtils.isEmpty(owghat)) {
                    remoteViews2.setTextViewText(R.id.owghat_2x2, owghat);
                    remoteViews2.setViewVisibility(R.id.owghat_2x2, View.VISIBLE);
                } else {
                    remoteViews2.setViewVisibility(R.id.owghat_2x2, View.GONE);
                }

                remoteViews2.setTextViewText(R.id.date_2x2, text2);

                remoteViews2.setOnClickPendingIntent(R.id.widget_layout2x2, launchAppPendingIntent);
                manager.updateAppWidget(widget2x2, remoteViews2);
            }
        }

        //
        // Permanent Notification Bar and DashClock Data Extension Update
        //
        //

        // Prepend a right-to-left mark character to Android with sane text rendering stack
        // to resolve a bug seems some Samsung devices have with characters with weak direction,
        // digits being at the first of string on
        if ((Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)) {
            title = Constants.RLM + title;
            subtitle = Constants.RLM + subtitle;
        }

        int icon = Utils.getDayIconResource(date.getDayOfMonth());

        if (Utils.isNotifyDate()) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                int importance = NotificationManager.IMPORTANCE_LOW;
                NotificationChannel channel = new NotificationChannel(String.valueOf(NOTIFICATION_ID), context.getString(R.string.app_name), importance);
                channel.setShowBadge(false);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, String.valueOf(NOTIFICATION_ID))
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setSmallIcon(icon)
                    .setOngoing(true)
                    .setWhen(0)
                    .setContentIntent(launchAppPendingIntent)
                    .setVisibility(Utils.isNotifyDateOnLockScreen()
                            ? NotificationCompat.VISIBILITY_PUBLIC
                            : NotificationCompat.VISIBILITY_SECRET)
                    .setColor(0xFF607D8B)
                    .setContentTitle(title)
                    .setContentText(subtitle);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N || BuildConfig.DEBUG) {
                RemoteViews cv = new RemoteViews(context.getPackageName(), R.layout.custom_notification);
                cv.setTextViewText(R.id.title, title);
                cv.setTextViewText(R.id.body, subtitle);

                RemoteViews bcv = new RemoteViews(context.getPackageName(), R.layout.custom_notification_big);
                bcv.setTextViewText(R.id.title, title);
                bcv.setTextViewText(R.id.body, subtitle);

                List<AbstractEvent> events = Utils.getEvents(jdn);
                String holidays = Utils.getEventsTitle(events, true, true);
                if (!TextUtils.isEmpty(holidays))
                    bcv.setTextViewText(R.id.holidays, holidays);
                else
                    bcv.setViewVisibility(R.id.holidays, View.GONE);
                String nonHolidays = Utils.getEventsTitle(events, false, true);
                if (!TextUtils.isEmpty(nonHolidays))
                    bcv.setTextViewText(R.id.nonholidays, nonHolidays);
                else
                    bcv.setViewVisibility(R.id.nonholidays, View.GONE);

                if (!TextUtils.isEmpty(owghat))
                    bcv.setTextViewText(R.id.owghat, owghat);
                else
                    bcv.setViewVisibility(R.id.owghat, View.GONE);

                builder = builder
                        .setCustomContentView(cv)
                        .setCustomBigContentView(bcv)
                        .setStyle(new NotificationCompat.DecoratedCustomViewStyle());
            }

            if (BuildConfig.DEBUG) {
                builder = builder.setWhen(Calendar.getInstance().getTimeInMillis());
            }

            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }

        mExtensionData = new ExtensionData().visible(true).icon(icon)
                .status(status)
                .expandedTitle(title)
                .expandedBody(subtitle).clickIntent(intent);
    }

    public static ExtensionData getExtensionData() {
        return mExtensionData;
    }

}
