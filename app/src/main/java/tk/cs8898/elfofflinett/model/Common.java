package tk.cs8898.elfofflinett.model;

import android.content.Context;
import android.net.ConnectivityManager;

public class Common {
    public static final int ONE_MIN = 60_000;

    //Main Activity
    public static final String ACTIVITY_VIEW_MARKED = "tk.cs8898.elfofflinett.view.marked";
    public static final String ACTIVITY_VIEW_ALL = "tk.cs8898.elfofflinett.view.all";

    //PREFERENCES
    public static final String PREFERENCES_NAME = "tk.cs8898.elfofflinett.preferences";
    /**
     * Auto Started When already started is set to True
     */
    public static final String PREF_AUTOSTARTED_NAME = "autostarted";
    /**
     * The Time Value of the now staring Event
     */
    public static final String PREF_NOTIFICATION_TRIGGER_TIME_NAME = "notificationtime";

    /**
     * The Time when the Last CurrentlyOnStafe trigger has been executed
     */
    public static final String PREF_NOTIFICATION_LAST_SHOWN_ONSTAGE_TIME_NAME = "notificationshownonstagetime";
    /**
     * The Time when the Last Upcomeing trigger has been executed
     */
    public static final String PREF_NOTIFICATION_LAST_SHOWN_UPCOMING_TIME_NAME = "notificationshownupcomingtime";
    /**
     * How many minutes does the notification come earlier
     */
    public static final String PREF_NOTIFICATION_EARLIER_TIME_NAME = "notificationearlytime";
    /**
     * All Marked Events
     */
    public static final String PREF_MARKED_NAME = "marked";

    public static boolean isOnline(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null)
                return cm.getActiveNetworkInfo().isConnectedOrConnecting();
        } catch (Exception ignored) {
        }
        return false;
    }
}
