package tk.cs8898.elfofflinett.services.logic;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.PersistableBundle;
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
import tk.cs8898.elfofflinett.model.Common;
import tk.cs8898.elfofflinett.model.bus.BusProvider;
import tk.cs8898.elfofflinett.model.bus.messages.MessageDatasetReady;
import tk.cs8898.elfofflinett.model.database.MarkedActsService;
import tk.cs8898.elfofflinett.model.entity.InternalActEntity;
import tk.cs8898.elfofflinett.receiver.AlarmReceiver;
import tk.cs8898.elfofflinett.services.FetchTimeTableService;
import tk.cs8898.elfofflinett.services.NotificationService;

public class NotificationLogic {
    private static final String PREFERENCES_NAME = "tk.cs8898.elfofflinett.preferences";
    private static final String PREF_NOTIFICATIONTRIGGER_TIME = "notificationtime";
    private static final String NOTIFICATION_CHAN_ID = "tk.cs8898.elfofflinett.notification.current";
    private static final String NOTIFICATION_CHAN_NAME = "Current Event";
    private static final int REQUEST_CODE = 889801;
    private static final int NOTIFICATION_ID = 889810;

    public static final String EXTRA_TIME = "tk.cs8898.elfofflinett.extra.time";

    private final Object waitTimeTableLock = new Object();

    /**
     * Removes an old timer and adds a new one
     */
    public void handleActionInitNotification(Context context) {
        if (MarkedActsService.getActs().size() == 0) {
            BusProvider.getInstance().register(this);
            FetchTimeTableService.startActionFetchTimetable(context, true, false);
            try {
                waitForTimeTable();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            BusProvider.getInstance().unregister(this);
        }

        //FETCH LAST NOTIFICATION FOR DELETE
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        long lastTriggerTime = preferences.getLong(PREF_NOTIFICATIONTRIGGER_TIME, -1);

        //DELETE LAST TRIGGER
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            assert jobScheduler != null;
            jobScheduler.cancel(NotificationService.ScheduledNotificationService.NOTIFY_JOB_ID);
        } else {
            //REMOVE OLD TIMER
            if (lastTriggerTime != -1) {
                Intent intent = new Intent(context, AlarmReceiver.class);
                intent.setAction(AlarmReceiver.ALARM_TRIGGER_NOTIFICATION);
                intent.putExtra(AlarmReceiver.EXTRA_TIME, lastTriggerTime);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                assert alarmManager != null;
                alarmManager.cancel(pendingIntent);
            }
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
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                long delayTime = newTriggerTime-Calendar.getInstance(TimeZone.getTimeZone("Berlin/Germany"),Locale.GERMANY).getTimeInMillis();
                JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                assert jobScheduler != null;

                ComponentName serviceComponent = new ComponentName(context, NotificationService.ScheduledNotificationService.class);

                JobInfo.Builder builder = new JobInfo.Builder(NotificationService.ScheduledNotificationService.NOTIFY_JOB_ID,serviceComponent);
                PersistableBundle extras = new PersistableBundle();
                extras.putLong(NotificationService.ScheduledNotificationService.EXTRA_TIME,newTriggerTime);
                builder.setExtras(extras);
                builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                        .setMinimumLatency(delayTime)
                        .setOverrideDeadline(delayTime+Common.ONE_MIN)
                        .setRequiresCharging(false)
                        .setRequiresDeviceIdle(false);
                jobScheduler.schedule(builder.build());
            } else {
                //START NEW TIMER
                Intent intent = new Intent(context, AlarmReceiver.class);
                intent.setAction(AlarmReceiver.ALARM_TRIGGER_NOTIFICATION);
                intent.putExtra(AlarmReceiver.EXTRA_TIME, newTriggerTime);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                assert alarmManager != null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, newTriggerTime, pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, newTriggerTime, pendingIntent);
                }
                preferences.edit().putLong(PREF_NOTIFICATIONTRIGGER_TIME, newTriggerTime).apply();
                Log.d("NotificationService", "Added new Timer for " + newTriggerTime);
            }
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
    public void handleActionTriggerNotification(Context context, long time) {
        Log.d(NotificationService.class.getSimpleName(), "Triggered Notification at Time" + time);
        if (time == -1)
            return;
        if (MarkedActsService.getActs().size() == 0) {
            BusProvider.getInstance().register(this);
            //FetchTimeTableService.startActionFetchTimetable(context, true, false);
            FetchTimeTableLogic.getThreadActionFetchTimetable(context, context.getString(R.string.timetable_url), true, false).start();
            try {
                waitForTimeTable();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            BusProvider.getInstance().unregister(this);
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

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
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
            notificationBuilder = new NotificationCompat.Builder(context.getApplicationContext(), NOTIFICATION_CHAN_ID);

            PendingIntent notificationIntent = PendingIntent.getActivity(context.getApplicationContext(), 0,
                    new Intent(context.getApplicationContext(), MainActivity.class), 0);
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
        handleActionInitNotification(context.getApplicationContext());
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
}
