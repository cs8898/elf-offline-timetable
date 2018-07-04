package tk.cs8898.elfofflinett.receiver;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import tk.cs8898.elfofflinett.R;
import tk.cs8898.elfofflinett.model.database.MarkedActsService;
import tk.cs8898.elfofflinett.model.entity.InternalActEntity;
import tk.cs8898.elfofflinett.services.FetchTimeTableService;

public class AlarmReceiver extends BroadcastReceiver {
    public static String ACTION_TRIGGER_NOTIFICATION = "tk.cs8898.elfofflinett.action.triggernotification";
    public static String ACTION_INIT_NOTIFICATION = "tk.cs8898.elfofflinett.action.initnotification";
    private static final String PREFERENCES_NAME = "tk.cs8898.elfofflinett.preferences";
    private static final String EXTRA_ACT = "tk.cs8898.elfofflinett.extra.act";
    private static final String PREF_NOTIFICATIONACT_NAME = "notificationact";
    private static final String NOTIFICATION_CHAN_ID = "tk.cs8898.elfofflinett.notification.current";
    private static final String NOTIFICATION_CHAN_NAME = "Current Event";
    private static final int REQUEST_CODE = 889801;
    private static final int NOTIFICATION_ID = 889810;

    public static void start(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ACTION_INIT_NOTIFICATION);

        context.sendBroadcast(intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_TRIGGER_NOTIFICATION.equals(intent.getAction())) {
            String actString = intent.getStringExtra(EXTRA_ACT);
            if (actString == null || actString.length() == 0)
                return;
            triggerNotification(context.getApplicationContext(), actString);
        } else if (ACTION_INIT_NOTIFICATION.equals(intent.getAction())) {
            addNotificationDelays(context.getApplicationContext());
        }
    }

    private void triggerNotification(Context context, String actString) {
        if (MarkedActsService.getActs().size() == 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            FetchTimeTableService.startActionFetchTimetable(context, "", true, false);
            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (MarkedActsService.getActs().size() == 0);
        }

        InternalActEntity intentAct = MarkedActsService.findAct(actString);
        if (intentAct == null)
            return;

        Set<InternalActEntity> currentActs = new HashSet<>();
        for (InternalActEntity act : MarkedActsService.getMarked()) {
            if (act.getTime().compareTo(intentAct.getTime()) == 0) {
                currentActs.add(act);
            }
        }
        StringBuilder notificationBody = new StringBuilder();
        for (InternalActEntity act : currentActs) {
            notificationBody.append("[").append(act.getLocation()).append("] ")
                    .append(act.getName()).append("\n");
        }

        Notification.Builder notificationBuilder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            assert mNotificationManager != null;
            mNotificationManager.createNotificationChannel(new NotificationChannel(NOTIFICATION_CHAN_ID, NOTIFICATION_CHAN_NAME, NotificationManager.IMPORTANCE_DEFAULT));
            notificationBuilder = new Notification.Builder(context, NOTIFICATION_CHAN_ID);

        } else {
            notificationBuilder = new Notification.Builder(context);
        }
        PendingIntent notificationIntent = PendingIntent.getActivity(context, 0,
                new Intent(), 0);
        notificationBuilder = notificationBuilder.setContentTitle("Currently on Stage")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentText(notificationBody.toString())
                .setContentIntent(notificationIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.cancel(NOTIFICATION_ID);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        addNotificationDelays(context);
    }

    private void addNotificationDelays(Context context) {
        if (MarkedActsService.getActs().size() == 0) {
            FetchTimeTableService.startActionFetchTimetable(context, "", true, false);
            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (MarkedActsService.getActs().size() == 0);
        }


        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        String actString = preferences.getString(PREF_NOTIFICATIONACT_NAME, "");

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ACTION_TRIGGER_NOTIFICATION);
        intent.putExtra(EXTRA_ACT, actString);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        if (actString.length() != 0) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            //REMOVE TIMER
            alarmManager.cancel(pendingIntent);
        }

        InternalActEntity minStart = null;
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"), Locale.GERMANY);

        for (InternalActEntity act : MarkedActsService.getMarked()) {
            if (act.getTime().after(now) &&
                    (minStart == null || act.getTime().before(minStart.getTime()))) {
                minStart = act;
            }
        }
        if (minStart != null) {
            //START NEW TIMER
            intent.putExtra(EXTRA_ACT, minStart.toString());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, minStart.getTime().getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, minStart.getTime().getTimeInMillis(), pendingIntent);
            }
            preferences.edit().putString(PREF_NOTIFICATIONACT_NAME, minStart.toString()).apply();
        }
    }
}
