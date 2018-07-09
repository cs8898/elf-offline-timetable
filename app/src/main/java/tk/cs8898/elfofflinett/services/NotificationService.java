package tk.cs8898.elfofflinett.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import tk.cs8898.elfofflinett.R;
import tk.cs8898.elfofflinett.activity.MainActivity;
import tk.cs8898.elfofflinett.model.database.MarkedActsService;
import tk.cs8898.elfofflinett.model.entity.InternalActEntity;
import tk.cs8898.elfofflinett.receiver.AlarmReceiver;

import static tk.cs8898.elfofflinett.receiver.AlarmReceiver.ALARM_TRIGGER_NOTIFICATION;

public class NotificationService extends IntentService {

    private static final String ACTION_TRIGGER_NOTIFICATION = "tk.cs8898.elfofflinett.action.triggernotification";
    private static final String ACTION_INIT_NOTIFICATION = "tk.cs8898.elfofflinett.action.initnotification";

    private static final String PREFERENCES_NAME = "tk.cs8898.elfofflinett.preferences";
    private static final String PREF_NOTIFICATIONACT_NAME = "notificationact";
    private static final String NOTIFICATION_CHAN_ID = "tk.cs8898.elfofflinett.notification.current";
    private static final String NOTIFICATION_CHAN_NAME = "Current Event";
    private static final int REQUEST_CODE = 889801;
    private static final int NOTIFICATION_ID = 889810;

    private static final String EXTRA_ACT = "tk.cs8898.elfofflinett.extra.act";

    public NotificationService() {
        super("NotificationService");
    }

    public static void startActionInitNotification(Context context) {
        Intent intent = new Intent(context, NotificationService.class);
        intent.setAction(ACTION_INIT_NOTIFICATION);
        context.startService(intent);
    }

    public static void startActionTriggerNotification(Context context, String actString) {
        Intent intent = new Intent(context, NotificationService.class);
        intent.setAction(ACTION_TRIGGER_NOTIFICATION);
        intent.putExtra(EXTRA_ACT, actString);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_INIT_NOTIFICATION.equals(action)) {
                handleActionInitNotification();
            } else if (ACTION_TRIGGER_NOTIFICATION.equals(action)) {
                final String actString = intent.getStringExtra(EXTRA_ACT);
                handleActionTriggerNotification(actString);
            }
        }
    }

    /**
     * Removes an old timer and adds a new one
     */
    private void handleActionInitNotification() {
        if (MarkedActsService.getActs().size() == 0) {
            FetchTimeTableService.startActionFetchTimetable(getApplicationContext(), true, false);
            //IMPLEMENT Observer
            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (MarkedActsService.getActs().size() == 0);
        }

        //FETCH LAST NOTIFICATION FOR DELETE
        SharedPreferences preferences = getApplicationContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        String actString = preferences.getString(PREF_NOTIFICATIONACT_NAME, "");

        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        intent.setAction(ALARM_TRIGGER_NOTIFICATION);
        intent.putExtra(EXTRA_ACT, actString);

        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        if (actString.length() != 0) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            //REMOVE OLD TIMER
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
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, minStart.getTime().getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, minStart.getTime().getTimeInMillis(), pendingIntent);
            }
            preferences.edit().putString(PREF_NOTIFICATIONACT_NAME, minStart.toString()).apply();
        }
    }

    /**
     * removes the old notification and adds the new one
     *
     * @param actString the act for the notification
     */
    private void handleActionTriggerNotification(String actString) {
        if (MarkedActsService.getActs().size() == 0) {
            FetchTimeTableService.startActionFetchTimetable(getApplicationContext(), true, false);
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

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;

        Notification.Builder notificationBuilder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(NOTIFICATION_CHAN_ID) == null)
                notificationManager.createNotificationChannel(new NotificationChannel(NOTIFICATION_CHAN_ID, NOTIFICATION_CHAN_NAME, NotificationManager.IMPORTANCE_DEFAULT));
            notificationBuilder = new Notification.Builder(getApplicationContext(), NOTIFICATION_CHAN_ID);
        } else {
            notificationBuilder = new Notification.Builder(getApplicationContext());
        }
        PendingIntent notificationIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                new Intent(getApplicationContext(),MainActivity.class), 0);
        notificationBuilder = notificationBuilder.setContentTitle("Currently on Stage")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentText(notificationBody.toString())
                .setContentIntent(notificationIntent);
        //notificationManager.cancel(NOTIFICATION_ID); //Theoretical can be ignored
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        startActionInitNotification(getApplicationContext());
    }
}
