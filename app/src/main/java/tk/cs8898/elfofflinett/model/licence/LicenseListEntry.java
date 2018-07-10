package tk.cs8898.elfofflinett.model.licence;

import tk.cs8898.elfofflinett.model.licence.licenses.Apache2;
import tk.cs8898.elfofflinett.model.licence.licenses.GenericLicense;
import tk.cs8898.elfofflinett.model.licence.licenses.ILicense;
import tk.cs8898.elfofflinett.model.licence.licenses.WTFPL;

import java.util.ArrayList;
import java.util.List;

public class LicenseListEntry {
    private List<ILicense> licenses;

    public LicenseListEntry() {

    }

    public LicenseListEntry(List<ILicense> list) {
        this.licenses = list;
    }

    @SuppressWarnings("SpellCheckingInspection")
    public static List<LicenseListEntry> getUsedLicenses() {
        ArrayList<ILicense> licenses = new ArrayList<>();
        licenses.add(new WTFPL("2018 Christian Schmied","ELF Offline TimeTable", "https://github.com/cs8898/elf-offline-timetable"));
        licenses.add(new Apache2("2008 Google Inc.","google-gson", "https://github.com/google/gson"));
        licenses.add(new Apache2("2016 Square, Inc.","OkHttp", "https://github.com/square/okhttp"));
        licenses.add(new Apache2("2013 Square, Inc.","Otto", "https://github.com/square/otto"));
        licenses.add(new Apache2("2014 Raquib-ul-Alam","Android Week View","https://github.com/Quivr/Android-Week-View"));
        return getLicenseListEntries(licenses);
    }

    private static LicenseListEntry getOfSameLicense(List<ILicense> list, int i) {
        ArrayList<ILicense> tmp = new ArrayList<>();
        for (ILicense aList : list) {
            if (aList.getName().equals(list.get(i).getName())) {
                tmp.add(aList);
            }
        }
        return new LicenseListEntry(tmp);
    }

    public static List<LicenseListEntry> getLicenseListEntries(List<ILicense> list) {
        ArrayList<ILicense> tmp = new ArrayList<>();
        ArrayList<LicenseListEntry> tmp2 = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            boolean contains = false;
            for (ILicense aTmp : tmp) {
                if (aTmp.getName().equals(list.get(i).getName())) {
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                tmp.add(list.get(i));
                tmp2.add(getOfSameLicense(list, i));
            }
        }
        return tmp2;
    }

    public String getLicenseText() {
        return this.licenses.get(0).getLicenseText();
    }

    public List<ILicense> getLicenses() {
        return licenses;
    }
}
