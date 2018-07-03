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
        licenses.add(new WTFPL("ELF Offline TimeTable", "https://github.com/cs8898/elf-offline-timetable"));
        licenses.add(new GenericLicense("google-gson", "https://github.com/google/gson","Copyright 2008 Google Inc.\n" +
                "\n" +
                "Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                "you may not use this file except in compliance with the License.\n" +
                "You may obtain a copy of the License at\n" +
                "\n" +
                "    http://www.apache.org/licenses/LICENSE-2.0\n" +
                "\n" +
                "Unless required by applicable law or agreed to in writing, software\n" +
                "distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                "See the License for the specific language governing permissions and\n" +
                "limitations under the License."));
        licenses.add(new GenericLicense("okhttp", "https://github.com/square/okhttp","Copyright 2016 Square, Inc.\n" +
                "\n" +
                "Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                "you may not use this file except in compliance with the License.\n" +
                "You may obtain a copy of the License at\n" +
                "\n" +
                "   http://www.apache.org/licenses/LICENSE-2.0\n" +
                "\n" +
                "Unless required by applicable law or agreed to in writing, software\n" +
                "distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                "See the License for the specific language governing permissions and\n" +
                "limitations under the License."));
        licenses.add(new GenericLicense("Android Week View","https://github.com/alamkanak/Android-Week-View","Copyright 2014 Raquib-ul-Alam\n" +
                "\n" +
                "Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                "you may not use this file except in compliance with the License.\n" +
                "You may obtain a copy of the License at\n" +
                "\n" +
                "   http://www.apache.org/licenses/LICENSE-2.0\n" +
                "\n" +
                "Unless required by applicable law or agreed to in writing, software\n" +
                "distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                "See the License for the specific language governing permissions and\n" +
                "limitations under the License."));
        return getLicenseListEntrys(licenses);
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

    public static List<LicenseListEntry> getLicenseListEntrys(List<ILicense> list) {
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
