package tk.cs8898.elfofflinett.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ListView;

import tk.cs8898.elfofflinett.R;
import tk.cs8898.elfofflinett.adapters.MyLicenseListAdapter;
import tk.cs8898.elfofflinett.model.licence.LicenseListEntry;

import java.util.List;

public class LicenseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);
        initLicenses();
    }

    private void initLicenses() {
        final ListView licenseListView = findViewById(R.id.licenseListView);
        final List<LicenseListEntry> licenseList = LicenseListEntry.getUsedLicenses();
        licenseListView.setAdapter(new MyLicenseListAdapter(getBaseContext(), licenseList));
    }
}
