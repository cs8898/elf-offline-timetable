package tk.cs8898.elfofflinett.receiver;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;

import org.junit.Assert;
import org.junit.Test;

import tk.cs8898.elfofflinett.model.Common;

import static org.junit.Assert.*;

public class AutostartReceiverTest {

    @Test
    public void onReceive() {

        Context appContext = InstrumentationRegistry.getTargetContext();

        // prepare data for onReceive and call it
        Intent intent = new Intent(Intent.ACTION_BOOT_COMPLETED);
        AutostartReceiver mReceiver = new AutostartReceiver();
        mReceiver.onReceive(appContext, intent);

        SharedPreferences prefs = appContext.getSharedPreferences(Common.PREFERENCES_NAME,Context.MODE_PRIVATE);
        int loops = 0;
        while(!prefs.getBoolean(Common.PREF_AUTOSTARTED_NAME, false)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(++loops > 5){
                break;
            }
        }
        Assert.assertTrue(prefs.getBoolean(Common.PREF_AUTOSTARTED_NAME, false));
    }
}