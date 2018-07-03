package tk.cs8898.elfofflinett.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import tk.cs8898.elfofflinett.R;
import tk.cs8898.elfofflinett.model.licence.LicenseListEntry;
import tk.cs8898.elfofflinett.model.licence.licenses.ILicense;

import java.util.List;

public class MyLicenseListAdapter extends ArrayAdapter<LicenseListEntry> {
    private final List<LicenseListEntry> objects;
    private final Context context;

    public MyLicenseListAdapter(Context context, List<LicenseListEntry> objects) {
        super(context, -1, objects);
        this.objects = objects;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int i, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        if(convertView==null) {
            convertView = inflater.inflate(R.layout.row_license_entry, parent, false);
        }
        assert convertView != null;
        LinearLayout projectList = convertView.findViewById(R.id.project_list);
        projectList.removeAllViews();

        for(ILicense license:objects.get(i).getLicenses()){
            View projectElem = inflater.inflate(R.layout.row_license_project_entry,projectList);
            TextView projectName = projectElem.findViewById(R.id.project_name);
            TextView projectUrl = projectElem.findViewById(R.id.project_url);

            projectName.setText(license.getPackageName());
            projectUrl.setText(license.getPackageUrl());
        }

        TextView licenseText = convertView.findViewById(R.id.license_text);
        licenseText.setText(objects.get(i).getLicenseText());
        return convertView;
    }

    /*public void setItem(int index, LicenseListEntry o) {
        this.objects.set(index, o);
        this.notifyDataSetChanged();
    }

    public void addItem(LicenseListEntry o) {
        this.objects.add(o);
        this.notifyDataSetChanged();
    }

    @Override
    public void remove(LicenseListEntry o) {
        super.remove(o);
        this.notifyDataSetChanged();
    }*/
}
