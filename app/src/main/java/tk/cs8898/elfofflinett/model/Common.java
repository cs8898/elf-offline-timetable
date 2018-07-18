package tk.cs8898.elfofflinett.model;

import android.content.Context;
import android.net.ConnectivityManager;

public class Common {
    public static final int ONE_MIN = 60*1000;
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
