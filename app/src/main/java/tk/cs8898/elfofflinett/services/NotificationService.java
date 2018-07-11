package tk.cs8898.elfofflinett.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.squareup.otto.Subscribe;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import tk.cs8898.elfofflinett.R;
import tk.cs8898.elfofflinett.activity.MainActivity;
import tk.cs8898.elfofflinett.model.bus.BusProvider;
import tk.cs8898.elfofflinett.model.bus.messages.MessageDatasetReady;
import tk.cs8898.elfofflinett.model.database.MarkedActsService;
import tk.cs8898.elfofflinett.model.entity.InternalActEntity;
import tk.cs8898.elfofflinett.receiver.AlarmReceiver;

public class NotificationService extends IntentService {

    public static final String ACTION_TRIGGER_NOTIFICATION = "tk.cs8898.elfofflinett.action.triggernotification";
    public static final String ACTION_INIT_NOTIFICATION = "tk.cs8898.elfofflinett.action.initnotification";

    private static final String PREFERENCES_NAME = "tk.cs8898.elfofflinett.preferences";
    private static final String PREF_NOTIFICATIONTRIGGER_TIME = "notificationtime";
    private static final String NOTIFICATION_CHAN_ID = "tk.cs8898.elfofflinett.notification.current";
    private static final String NOTIFICATION_CHAN_NAME = "Current Event";
    private static final int REQUEST_CODE = 889801;
    private static final int NOTIFICATION_ID = 889810;

    public static final String EXTRA_TIME = "tk.cs8898.elfofflinett.extra.time";

    private final Object waitTimeTableLock = new Object();

    public NotificationService() {
        super("NotificationService");
    }

    public static void startActionInitNotification(Context context) {
        Intent intent = new Intent(context, NotificationService.class);
        intent.setAction(ACTION_INIT_NOTIFICATION);
        context.startService(intent);
    }

    public static void startActionTriggerNotification(Context context, long time) {
        Intent intent = new Intent(context, NotificationService.class);
        intent.setAction(ACTION_TRIGGER_NOTIFICATION);
        intent.putExtra(EXTRA_TIME, time);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            BusProvider.getInstance().register(this);
            final String action = intent.getAction();
            if (ACTION_INIT_NOTIFICATION.equals(action)) {
                handleActionInitNotification();
            } else if (ACTION_TRIGGER_NOTIFICATION.equals(action)) {
                final long time = intent.getLongExtra(EXTRA_TIME, -1);
                handleActionTriggerNotification(time);
            }
            BusProvider.getInstance().unregister(this);
        }
        stopSelf();
    }

    /**
     * Removes an old timer and adds a new one
     */
    private void handleActionInitNotification() {
        if (MarkedActsService.getActs().size() == 0) {
            FetchTimeTableService.startActionFetchTimetable(getApplicationContext(), true, false);
            try {
                waitForTimeTable();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //FETCH LAST NOTIFICATION FOR DELETE
        SharedPreferences preferences = getApplicationContext().getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        long lastTriggerTime = preferences.getLong(PREF_NOTIFICATIONTRIGGER_TIME, -1);

        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ALARM_TRIGGER_NOTIFICATION);
        intent.putExtra(AlarmReceiver.EXTRA_TIME, lastTriggerTime);

        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        //REMOVE OLD TIMER
        if (lastTriggerTime != -1) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.cancel(pendingIntent);
        }

        long newTriggerTime = Long.MAX_VALUE;
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"), Locale.GERMANY);

        for (InternalActEntity act : MarkedActsService.getMarked()) {
            /*if(act.getTime().before(now) && act.getEnd().after(now)){
                newTriggerTime = now.getTimeInMillis();
                break;
            }*/
            if (act.getEnd().after(now) &&
                    newTriggerTime > act.getEnd().getTimeInMillis()) {
                newTriggerTime = act.getEnd().getTimeInMillis();
            }
            if (act.getTime().after(now) &&
                    newTriggerTime > act.getTime().getTimeInMillis()) {
                newTriggerTime = act.getTime().getTimeInMillis();
            }
        }
        Log.d("NotificationService", "New minTimestamp is " + (newTriggerTime == Long.MAX_VALUE ? "MAX" : newTriggerTime));
        if (newTriggerTime != Long.MAX_VALUE) {
            //START NEW TIMER
            intent.putExtra(EXTRA_TIME, newTriggerTime);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, newTriggerTime, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, newTriggerTime, pendingIntent);
            }
            preferences.edit().putLong(PREF_NOTIFICATIONTRIGGER_TIME, newTriggerTime).apply();
            Log.d("NotificationService", "Added new Timer for " + newTriggerTime);
        } else {
            preferences.edit().remove(PREF_NOTIFICATIONTRIGGER_TIME).apply();
            Log.d("NotificationService", "No New Timer to be set");
        }
    }

    /**
     * removes the old notification and adds the new one
     *
     * @param time the time when the notification should be displayed
     */
    private void handleActionTriggerNotification(long time) {
        Log.d(NotificationService.class.getSimpleName(), "Triggered Notification at Time" + time);
        if (time == -1)
            return;
        if (MarkedActsService.getActs().size() == 0) {
            FetchTimeTableService.startActionFetchTimetable(getApplicationContext(), true, false);
            try {
                waitForTimeTable();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Log.d(NotificationService.class.getSimpleName(), "Populated Acts");

        Calendar triggerTime = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"), Locale.GERMANY);
        triggerTime.setTimeInMillis(time);

        Set<InternalActEntity> currentActs = new HashSet<>();
        for (InternalActEntity act : MarkedActsService.getMarked()) {
            //NOW OR ALREADY PAST and the END in the FUTURE
            if (act.getTime().compareTo(triggerTime) <= 0 && act.getEnd().compareTo(triggerTime) > 0) {
                currentActs.add(act);
            }
        }

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        if (currentActs.size() > 0) {
            StringBuilder notificationBody = new StringBuilder();
            for (InternalActEntity act : currentActs) {
                notificationBody.append("[").append(act.getLocation()).append("] ")
                        .append(act.getName()).append("\n");
            }

            NotificationCompat.Builder notificationBuilder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (notificationManager.getNotificationChannel(NOTIFICATION_CHAN_ID) == null)
                    notificationManager.createNotificationChannel(new NotificationChannel(NOTIFICATION_CHAN_ID, NOTIFICATION_CHAN_NAME, NotificationManager.IMPORTANCE_DEFAULT));
            }
            notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHAN_ID);

            PendingIntent notificationIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                    new Intent(getApplicationContext(), MainActivity.class), 0);
            notificationBuilder = notificationBuilder.setContentTitle("Currently on Stage")
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentText(notificationBody.toString())
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationBody.toString()))
                    .setOngoing(true)
                    .setContentIntent(notificationIntent);
            //notificationManager.cancel(NOTIFICATION_ID); //Theoretical can be ignored
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
            Log.d(NotificationService.class.getSimpleName(), "Finished Sending the Notification");
        } else {
            notificationManager.cancel(NOTIFICATION_ID);
        }
        startActionInitNotification(getApplicationContext());
    }

    private void waitForTimeTable() throws InterruptedException {
        synchronized (waitTimeTableLock) {
            waitTimeTableLock.notify();
            waitTimeTableLock.wait();
        }
    }

    @Subscribe
    public void onDatasetReady(MessageDatasetReady message) {
        if (message.getOrigin().equals(FetchTimeTableService.class)) {
            synchronized (waitTimeTableLock) {
                waitTimeTableLock.notify();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static class ScheduledNotificationService extends JobService {
        public static final int INIT_JOB_ID = 889820;
        public static final int NOTIFY_JOB_ID = 889821;
        public static final String EXTRA_TIME = "tk.cs8898.elfofflinett.extra.time";

        @Override
        public boolean onStartJob(JobParameters params) {
            NotificationService service = new NotificationService();
            if (params.getJobId() == INIT_JOB_ID) {
                NotificationService.startActionInitNotification(this);
            }else if (params.getJobId() == NOTIFY_JOB_ID) {
                NotificationService.startActionTriggerNotification(this, params.getExtras().getLong(EXTRA_TIME, -1));
            }
            return true;
        }

        @Override
        public boolean onStopJob(JobParameters params) {
            return false;
        }
    }
}
