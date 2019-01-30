package tk.cs8898.elfofflinett.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import static tk.cs8898.elfofflinett.model.Common.*;

import tk.cs8898.elfofflinett.services.NotificationService;

public class AutostartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AutoStartRec","I Got some Broadcast "+intent.getAction());
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("AutoStartRec", "Hey I Got Triggered");
            SharedPreferences prefs = context.getSharedPreferences(PREFERENCES_NAME,Context.MODE_PRIVATE);
            prefs.edit()
                    .remove(PREF_NOTIFICATION_LAST_SHOWN_ONSTAGE_TIME_NAME)
                    .remove(PREF_NOTIFICATION_LAST_SHOWN_UPCOMING_TIME_NAME)
                    .putBoolean(PREF_AUTOSTARTED_NAME,true)
                    .apply();
            try {
                NotificationService.startActionInitNotification(context);
            } catch (IllegalStateException e) {
                Log.d("autostartRecv", "The App is in Background Using Scheduler Now");
                NotificationService.scheduleInitNotification(context);
            }
        }
    }
}
