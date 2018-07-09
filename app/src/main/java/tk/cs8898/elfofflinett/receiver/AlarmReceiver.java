package tk.cs8898.elfofflinett.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import tk.cs8898.elfofflinett.services.NotificationService;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String ALARM_TRIGGER_NOTIFICATION = "tk.cs8898.elfofflinett.alarm.triggernotification";
    public static final String ALARM_INIT_NOTIFICATION = "tk.cs8898.elfofflinett.alarm.initnotification";
    private static final String EXTRA_ACT = "tk.cs8898.elfofflinett.extra.act";

    public static void start(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ALARM_INIT_NOTIFICATION);

        context.sendBroadcast(intent);
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (ALARM_TRIGGER_NOTIFICATION.equals(intent.getAction())) {
            final String actString = intent.getStringExtra(EXTRA_ACT);
            if (actString == null || actString.length() == 0)
                return;
            NotificationService.startActionTriggerNotification(context, actString);

        } else if (ALARM_INIT_NOTIFICATION.equals(intent.getAction())) {
            NotificationService.startActionInitNotification(context);

        }
    }
}
