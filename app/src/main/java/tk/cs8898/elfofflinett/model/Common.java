package tk.cs8898.elfofflinett.model;

import android.content.Context;
import android.net.ConnectivityManager;

public class Common {
    public static boolean isOnline(Context context)
    {
        try
        {
            ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo().isConnectedOrConnecting();
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
