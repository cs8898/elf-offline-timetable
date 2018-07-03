package tk.cs8898.elfofflinett.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import tk.cs8898.elfofflinett.R;
import tk.cs8898.elfofflinett.adapters.MyLicenseListAdapter;
import tk.cs8898.elfofflinett.model.licence.LicenseListEntry;

import java.util.List;

public class LicenseActivity extends AppCompatActivity {
    private static LicenseActivity obj = null;

    public static LicenseActivity get() {
        return obj;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);
        obj = this;
        initLicenses();
    }

    private void initLicenses() {
        final ListView licenseListView = (ListView) LicenseActivity.get().findViewById(R.id.licenseListView);
        final List<LicenseListEntry> licenseList = LicenseListEntry.getUsedLicenses();
        /*runOnUiThread(new Runnable() {
            @Override
            public void run() {*/
        licenseListView.setAdapter(new MyLicenseListAdapter(LicenseActivity.get().getBaseContext(), licenseList));
            /*}
        });*/
    }

    @Override
    public void onDestroy(){
        obj = null;
        super.onDestroy();
    }
}
