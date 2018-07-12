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
import tk.cs8898.elfofflinett.services.logic.NotificationLogic;

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
            final String action = intent.getAction();
            if (ACTION_INIT_NOTIFICATION.equals(action)) {
                new NotificationLogic().handleActionInitNotification(this.getApplicationContext());
            } else if (ACTION_TRIGGER_NOTIFICATION.equals(action)) {
                final long time = intent.getLongExtra(EXTRA_TIME, -1);
                new NotificationLogic().handleActionTriggerNotification(this.getApplicationContext(),time);
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
                new NotificationLogic().handleActionTriggerNotification(this, params.getExtras().getLong(EXTRA_TIME, -1));
            }
            return false;
        }

        @Override
        public boolean onStopJob(JobParameters params) {
            return false;
        }
    }
}
