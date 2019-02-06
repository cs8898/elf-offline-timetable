package tk.cs8898.elfofflinett.services;

import android.app.IntentService;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Context;
import android.os.Build;
import androidx.annotation.RequiresApi;

import tk.cs8898.elfofflinett.services.logic.NotificationLogic;

public class NotificationService extends IntentService {

    public static final String ACTION_TRIGGER_NOTIFICATION = "tk.cs8898.elfofflinett.action.triggernotification";
    public static final String ACTION_INIT_NOTIFICATION = "tk.cs8898.elfofflinett.action.initnotification";

    public static final String EXTRA_TIME = "tk.cs8898.elfofflinett.extra.time";

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

    public static void scheduleInitNotification(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            assert jobScheduler != null;
            ComponentName serviceComponent = new ComponentName(context, ScheduledNotificationService.class);

            JobInfo.Builder builder = new JobInfo.Builder(ScheduledNotificationService.INIT_JOB_ID, serviceComponent);
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                    .setOverrideDeadline(0)
                    .setRequiresCharging(false)
                    .setRequiresDeviceIdle(false);
            jobScheduler.schedule(builder.build());
        } else {
            startActionInitNotification(context);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_INIT_NOTIFICATION.equals(action)) {
                new NotificationLogic().handleActionInitNotification(this.getApplicationContext());
            } else if (ACTION_TRIGGER_NOTIFICATION.equals(action)) {
                //final long time = intent.getLongExtra(EXTRA_TIME, -1);
                //new NotificationLogic().handleActionTriggerNotification(this.getApplicationContext(),time);
                new NotificationLogic().handleActionTriggerNotification(this.getApplicationContext());
            }
        }
        stopSelf();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static class ScheduledNotificationService extends JobService {
        public static final int INIT_JOB_ID = 889820;
        public static final int NOTIFY_JOB_ID = 889821;
        public static final String EXTRA_TIME = "tk.cs8898.elfofflinett.extra.time";

        @Override
        public boolean onStartJob(JobParameters params) {
            if (params.getJobId() == INIT_JOB_ID) {
                new NotificationLogic().handleActionInitNotification(this);
            }else if (params.getJobId() == NOTIFY_JOB_ID) {
                //new NotificationLogic().handleActionTriggerNotification(this, params.getExtras().getLong(EXTRA_TIME, -1));
                new NotificationLogic().handleActionTriggerNotification(this);
            }
            return false;
        }

        @Override
        public boolean onStopJob(JobParameters params) {
            return false;
        }
    }
}
