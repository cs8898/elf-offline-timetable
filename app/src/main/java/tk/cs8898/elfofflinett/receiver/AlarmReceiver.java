package tk.cs8898.elfofflinett.receiver;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;

import tk.cs8898.elfofflinett.services.NotificationService;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String ALARM_TRIGGER_NOTIFICATION = "tk.cs8898.elfofflinett.alarm.triggernotification";
    public static final String EXTRA_TIME = "tk.cs8898.elfofflinett.extra.time";

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (ALARM_TRIGGER_NOTIFICATION.equals(intent.getAction())) {
            final long time = intent.getLongExtra(EXTRA_TIME, -1);
            if (time != -1) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                    assert jobScheduler != null;
                    ComponentName serviceComponent = new ComponentName(context, NotificationService.ScheduledNotificationService.class);

                    JobInfo.Builder builder = new JobInfo.Builder(NotificationService.ScheduledNotificationService.NOTIFY_JOB_ID,serviceComponent);
                    PersistableBundle extras = new PersistableBundle();
                    extras.putLong(NotificationService.ScheduledNotificationService.EXTRA_TIME,time);
                    builder.setExtras(extras);
                    builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                            .setRequiresBatteryNotLow(false)
                            .setRequiresCharging(false)
                            .setRequiresDeviceIdle(false);
                    jobScheduler.schedule(builder.build());
                }else {
                    NotificationService.startActionTriggerNotification(context, time);
                }
            }
        }
    }
}
