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

import androidx.core.app.NotificationCompat;

import android.util.Log;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Collectors;

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

import static tk.cs8898.elfofflinett.model.Common.ONE_MIN;
import static tk.cs8898.elfofflinett.model.Common.PREFERENCES_NAME;
import static tk.cs8898.elfofflinett.model.Common.PREF_NOTIFICATION_LAST_SHOWN_ONSTAGE_TIME_NAME;
import static tk.cs8898.elfofflinett.model.Common.PREF_NOTIFICATION_LAST_SHOWN_UPCOMING_TIME_NAME;
import static tk.cs8898.elfofflinett.model.Common.PREF_NOTIFICATION_TRIGGER_TIME_NAME;
import static tk.cs8898.elfofflinett.model.Common.PREF_NOTIFICATION_EARLIER_TIME_NAME;

public class NotificationLogic {
    private static final String NOTIFICATION_CURRENT_CHAN_ID = "tk.cs8898.elfofflinett.notification.current";
    private static final String NOTIFICATION_CURRENT_CHAN_NAME = "Current Event";
    private static final int NOTIFICATION_CURRENT_ID = 889810;

    private static final String NOTIFICATION_UPCOMING_CHAN_ID = "tk.cs8898.elfofflinett.notification.upcomeing";
    private static final String NOTIFICATION_UPCOMING_CHAN_NAME = "Upcomeing Event";
    private static final int NOTIFICATION_UPCOMING_ID = 889811;

    private static final int REQUEST_CODE = 889801;

    private final Object waitForTimeTableLock = new Object();

    /**
     * Removes an old timer and adds a new one
     */
    public void handleActionInitNotification(Context context) {
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"), Locale.GERMANY);
        if (MarkedActsService.getActs().size() == 0) {
            try {
                //waitForTimeTable(context,false);
                waitForTimeTable(context, true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //FETCH LAST NOTIFICATION FOR DELETE
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        long lastTriggerTime = preferences.getLong(PREF_NOTIFICATION_TRIGGER_TIME_NAME, -1);
        int upcomingOffsetValue = preferences.getInt(PREF_NOTIFICATION_EARLIER_TIME_NAME, 0) * ONE_MIN;
        long lastShownUpcomingTime = preferences.getLong(PREF_NOTIFICATION_LAST_SHOWN_UPCOMING_TIME_NAME, now.getTimeInMillis() - upcomingOffsetValue);
        Calendar lastUpcomingCalendar = Calendar.getInstance(TimeZone.getTimeZone("Berlin/Germany"), Locale.GERMANY);
        lastUpcomingCalendar.setTimeInMillis(lastShownUpcomingTime);
        long lastShownOnstageTime = preferences.getLong(PREF_NOTIFICATION_LAST_SHOWN_ONSTAGE_TIME_NAME, now.getTimeInMillis());
        Calendar lastOnstageCalendar = Calendar.getInstance(TimeZone.getTimeZone("Berlin/Germany"), Locale.GERMANY);
        lastOnstageCalendar.setTimeInMillis(lastShownOnstageTime);

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
                //intent.putExtra(AlarmReceiver.EXTRA_TIME, lastTriggerTime);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                assert alarmManager != null;
                alarmManager.cancel(pendingIntent);
            }
        }

        long newTriggerTime = findNextTriggerTime(now, lastOnstageCalendar, lastUpcomingCalendar, upcomingOffsetValue);

        Log.d(getClass().getSimpleName(), "New lastUpcoming is " + lastUpcomingCalendar.getTimeInMillis());
        Log.d(getClass().getSimpleName(), "New minTimestamp is " + (newTriggerTime == Long.MAX_VALUE ? "MAX" : newTriggerTime));
        if (newTriggerTime != Long.MAX_VALUE) {
            long alarmTime = newTriggerTime;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                alarmTime -= Calendar.getInstance(TimeZone.getTimeZone("Berlin/Germany"), Locale.GERMANY).getTimeInMillis();
                JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                assert jobScheduler != null;

                ComponentName serviceComponent = new ComponentName(context, NotificationService.ScheduledNotificationService.class);

                JobInfo.Builder builder = new JobInfo.Builder(NotificationService.ScheduledNotificationService.NOTIFY_JOB_ID, serviceComponent);
                PersistableBundle extras = new PersistableBundle();
                extras.putLong(NotificationService.ScheduledNotificationService.EXTRA_TIME, newTriggerTime);
                builder.setExtras(extras);
                builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                        .setMinimumLatency(alarmTime)
                        .setOverrideDeadline(alarmTime + Common.ONE_MIN)
                        .setRequiresCharging(false)
                        .setRequiresDeviceIdle(false);
                jobScheduler.schedule(builder.build());
            } else {
                //START NEW TIMER
                Intent intent = new Intent(context, AlarmReceiver.class);
                intent.setAction(AlarmReceiver.ALARM_TRIGGER_NOTIFICATION);
                //intent.putExtra(AlarmReceiver.EXTRA_TIME, newTriggerTime);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                assert alarmManager != null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
                }
            }
            preferences.edit().putLong(PREF_NOTIFICATION_TRIGGER_TIME_NAME, newTriggerTime).apply();
            Log.d(getClass().getSimpleName(), "Added new Timer for " + newTriggerTime + " with the time/delay " +
                    alarmTime);
        } else {
            preferences.edit().remove(PREF_NOTIFICATION_TRIGGER_TIME_NAME).apply();
            Log.d(getClass().getSimpleName(), "No New Timer to be set");
        }
    }

    /**
     * removes the old notification and adds the new one
     * <p>
     * //* @param time the time at which the events begin
     */
    public void handleActionTriggerNotification(Context context) {//, long time) {
        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        long triggerTime = preferences.getLong(PREF_NOTIFICATION_TRIGGER_TIME_NAME, -1);
        int upcomingOffsetValue = preferences.getInt(PREF_NOTIFICATION_EARLIER_TIME_NAME, 0) * ONE_MIN;

        Log.d(NotificationService.class.getSimpleName(), "Triggered Notification for Time" + triggerTime);
        if (triggerTime == -1)
            return;
        if (MarkedActsService.getActs().size() == 0) {
            try {
                waitForTimeTable(context, true);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Log.d(getClass().getSimpleName(), "Populated Acts");

        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"), Locale.GERMANY);

        //THE TIME FOR WITCH THE TRIGGER WAS SET // BUT SHOULD BE NOW
        //Calendar triggerTimeCal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"), Locale.GERMANY);
        //triggerTimeCal.setTimeInMillis(triggerTime);

        Collection<InternalActEntity> currentActs = getCurrentActs(now);
        Collection<InternalActEntity> upcomingActs = getUpcomingActs(now, upcomingOffsetValue);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        showCurrentNotification(context, notificationManager, currentActs, triggerTime);
        showUpcomingNotification(context, notificationManager, upcomingActs, triggerTime);

        //INIT THE NEXT NOTIFICATIONS
        handleActionInitNotification(context.getApplicationContext());
    }

    private void showCurrentNotification(Context context, NotificationManager notificationManager, Collection<InternalActEntity> currentActs, long triggerTime) {
        if (currentActs.size() > 0) {
            StringBuilder notificationBody = new StringBuilder();
            for (InternalActEntity act : currentActs) {
                notificationBody.append("[").
                        append(act.getLocation()).append("] ")
                        .append(act.getName())
                        .append(" ")
                        .append(String.format(Locale.GERMANY, "%02d", act.getTime().get(Calendar.HOUR_OF_DAY)))
                        .append(":")
                        .append(String.format(Locale.GERMANY, "%02d", act.getTime().get(Calendar.MINUTE)))
                        .append(" bis ")
                        .append(String.format(Locale.GERMANY, "%02d", act.getEnd().get(Calendar.HOUR_OF_DAY)))
                        .append(":")
                        .append(String.format(Locale.GERMANY, "%02d", act.getEnd().get(Calendar.MINUTE)))
                        .append("\n");
            }

            NotificationCompat.Builder notificationBuilder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (notificationManager.getNotificationChannel(NOTIFICATION_CURRENT_CHAN_ID) == null)
                    notificationManager.createNotificationChannel(new NotificationChannel(NOTIFICATION_CURRENT_CHAN_ID, NOTIFICATION_CURRENT_CHAN_NAME, NotificationManager.IMPORTANCE_DEFAULT));
            }
            notificationBuilder = new NotificationCompat.Builder(context.getApplicationContext(), NOTIFICATION_CURRENT_CHAN_ID);

            PendingIntent notificationIntent = PendingIntent.getActivity(context.getApplicationContext(), 0,
                    new Intent(context.getApplicationContext(), MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
            notificationBuilder = notificationBuilder.setContentTitle("On Stage")
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentText(notificationBody.toString())
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationBody.toString()))
                    .setOngoing(true)
                    .setContentIntent(notificationIntent);
            //notificationManager.cancel(NOTIFICATION_ID); //Theoretical can be ignored
            notificationManager.notify(NOTIFICATION_CURRENT_ID, notificationBuilder.build());
            Log.d(getClass().getSimpleName(), "Finished Sending the Notification");
        } else {
            notificationManager.cancel(NOTIFICATION_CURRENT_ID);
        }
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
                .putLong(PREF_NOTIFICATION_LAST_SHOWN_ONSTAGE_TIME_NAME, triggerTime).apply();

    }

    private void showUpcomingNotification(Context context, NotificationManager notificationManager, Collection<InternalActEntity> upcomingActs, long triggerTime) {
        if (upcomingActs.size() > 0) {
            StringBuilder notificationBody = new StringBuilder();
            for (InternalActEntity act : upcomingActs) {
                notificationBody.append("[").
                        append(act.getLocation()).append("] ")
                        .append(act.getName())
                        .append(" ")
                        .append(String.format(Locale.GERMANY, "%02d", act.getTime().get(Calendar.HOUR_OF_DAY)))
                        .append(":")
                        .append(String.format(Locale.GERMANY, "%02d", act.getTime().get(Calendar.MINUTE)))
                        .append("\n");
            }

            NotificationCompat.Builder notificationBuilder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (notificationManager.getNotificationChannel(NOTIFICATION_UPCOMING_CHAN_ID) == null)
                    notificationManager.createNotificationChannel(new NotificationChannel(NOTIFICATION_UPCOMING_CHAN_ID, NOTIFICATION_UPCOMING_CHAN_NAME, NotificationManager.IMPORTANCE_DEFAULT));
            }
            notificationBuilder = new NotificationCompat.Builder(context.getApplicationContext(), NOTIFICATION_UPCOMING_CHAN_ID);

            PendingIntent notificationIntent = PendingIntent.getActivity(context.getApplicationContext(), 0,
                    new Intent(context.getApplicationContext(), MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
            notificationBuilder = notificationBuilder.setContentTitle("Upcomeing")
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentText(notificationBody.toString())
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationBody.toString()))
                    .setOngoing(true)
                    .setContentIntent(notificationIntent);
            //notificationManager.cancel(NOTIFICATION_ID); //Theoretical can be ignored
            notificationManager.notify(NOTIFICATION_UPCOMING_ID, notificationBuilder.build());
            Log.d(getClass().getSimpleName(), "Finished Sending the Notification");
        } else {
            notificationManager.cancel(NOTIFICATION_UPCOMING_ID);
        }
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
                .putLong(PREF_NOTIFICATION_LAST_SHOWN_UPCOMING_TIME_NAME, triggerTime).apply();
    }

    private void waitForTimeTable(Context context, boolean thread) throws InterruptedException {
        BusProvider.getInstance().register(this);
        Log.d(getClass().getSimpleName(), "Started Waiting for TT");
        if (thread) {
            FetchTimeTableLogic.getThreadActionFetchTimetable(context, context.getString(R.string.timetable_url), true, false).start();
        } else {
            FetchTimeTableService.startActionFetchTimetable(context, true, false);
        }
        synchronized (waitForTimeTableLock) {
            waitForTimeTableLock.notify();
            waitForTimeTableLock.wait();
        }

        Log.d(getClass().getSimpleName(), "Finished Waiting for TT");
        BusProvider.getInstance().unregister(this);
    }

    /**
     * Finds the next change in the TimeTable
     *
     * @param now            the current time
     * @param lastOnstage    the last shown on stage
     * @param lastUpcoming   the last shown upcoming
     * @param upcomingOffset how many MS should an upcoming be triggered before the actual start
     * @return the Timestamp for the next Notification Trigger
     */
    public static long findNextTriggerTime(Calendar now, Calendar lastOnstage, Calendar lastUpcoming, long upcomingOffset) {
        long newTriggerTime = Long.MAX_VALUE;
        //FIND MINIMUM CHANGE TIME FOR NOTIFICATION AFTER LAST SHOWN
        synchronized (MarkedActsService.actsLock) {
            for (InternalActEntity act : MarkedActsService.getMarked()) {
                /*if(!act.getTime().after(now))
                    continue;*/
                if (act.getTime().before(now) &&
                        act.getEnd().getTimeInMillis() > lastOnstage.getTimeInMillis() &&
                        newTriggerTime > act.getEnd().getTimeInMillis()) {
                    //END TIME
                    newTriggerTime = act.getEnd().getTimeInMillis();
                }
                if (act.getTime().after(now)) {
                    if (act.getTime().getTimeInMillis() > lastOnstage.getTimeInMillis() &&
                            newTriggerTime > act.getTime().getTimeInMillis()) {
                        //START TIME
                        newTriggerTime = act.getTime().getTimeInMillis();
                    }
                    if (lastUpcoming.getTimeInMillis() < act.getTime().getTimeInMillis() - upcomingOffset &&
                            newTriggerTime > act.getTime().getTimeInMillis() - upcomingOffset) {
                        //UPCOMING TIME
                        newTriggerTime = act.getTime().getTimeInMillis() - upcomingOffset;
                    }
                }
            }
        }
        return newTriggerTime;
    }

    public static List<InternalActEntity> getCurrentActs(Calendar now) {
        synchronized (MarkedActsService.actsLock) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return MarkedActsService.getMarked().parallelStream()
                        .filter(e -> e.getTime().compareTo(now) <= 0 &&
                                e.getEnd().compareTo(now) > 0)
                        .sorted(Comparator.comparingLong(e -> e.getTime().getTimeInMillis()))
                        .collect(Collectors.toList());
            } else {
                List<InternalActEntity> currentActs = new ArrayList<>();
                for (InternalActEntity act : MarkedActsService.getMarked()) {
                    //NOW OR ALREADY PAST and the END in the FUTURE
                    if (act.getTime().compareTo(now) <= 0 &&
                            act.getEnd().compareTo(now) > 0) {
                        currentActs.add(act);
                    }
                }
                Collections.sort(currentActs, (o1, o2) -> Long.compare(o1.getTime().getTimeInMillis(), o2.getTime().getTimeInMillis()));
                return currentActs;
            }
        }
    }

    public static List<InternalActEntity> getUpcomingActs(Calendar now, long upcomingOffset) {
        synchronized (MarkedActsService.actsLock) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return MarkedActsService.getMarked().parallelStream()
                        .filter(e -> e.getTime().after(now) &&
                                e.getTime().getTimeInMillis() - now.getTimeInMillis() <= upcomingOffset)
                        .sorted(Comparator.comparingLong(e -> e.getTime().getTimeInMillis()))
                        .collect(Collectors.toList());
            } else {
                List<InternalActEntity> upcomingActs = new ArrayList<>();
                for (InternalActEntity act : MarkedActsService.getMarked()) {
                    if (act.getTime().after(now) &&
                            act.getTime().getTimeInMillis() - now.getTimeInMillis() <= upcomingOffset) {
                        upcomingActs.add(act);
                    }
                }
                Collections.sort(upcomingActs, (o1, o2) -> Long.compare(o1.getTime().getTimeInMillis(), o2.getTime().getTimeInMillis()));
                return upcomingActs;
            }
        }
    }

    @Subscribe
    public void onDatasetReady(MessageDatasetReady message) {
        if (message.getOrigin().equals(FetchTimeTableService.class)) {
            synchronized (waitForTimeTableLock) {
                waitForTimeTableLock.notify();
            }
        }
    }
}
