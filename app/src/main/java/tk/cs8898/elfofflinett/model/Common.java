package tk.cs8898.elfofflinett.model;

import android.content.Context;
import android.net.ConnectivityManager;

public class Common {
    public static final int ONE_MIN = 60*1000;

    //Main Activity
    public static final String ACTIVITY_VIEW_MARKED = "tk.cs8898.elfofflinett.view.marked";
    public static final String ACTIVITY_VIEW_ALL = "tk.cs8898.elfofflinett.view.all";

    //PREFERENCES
    public static final String PREFERENCES_NAME = "tk.cs8898.elfofflinett.preferences";
    /**
     * The Time Value of the now staring Event
     */
    public static final String PREF_NOTIFICATIONTRIGGER_TIME_NAME = "notificationtime";

    /**
     * The Time when the Last trigger has bin executed
     */
    public static final String PREF_ALREADY_TRIGGERED_TIME = "alreadytrigeredtime";
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
