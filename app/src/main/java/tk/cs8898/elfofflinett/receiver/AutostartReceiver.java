package tk.cs8898.elfofflinett.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import tk.cs8898.elfofflinett.services.NotificationService;

public class AutostartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            NotificationService.scheduleInitNotification(context);
        }
    }
}
