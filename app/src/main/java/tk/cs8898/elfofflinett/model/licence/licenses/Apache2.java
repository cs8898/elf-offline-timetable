package tk.cs8898.elfofflinett.model.licence.licenses;

public class Apache2 implements ILicense {

    private final String name, url;
    private final String copyright;

    public Apache2(String copyright, String name, String url) {
        this.name = name;
        this.url = url;
        this.copyright = copyright;
    }
    @Override
    public String getPackageName() {
        return this.name;
    }

    @Override
    public String getPackageUrl() {
        return this.url;
    }

    @Override
    public String getLicenseText() {
        return "Copyright "+this.copyright+"\n" +
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
                "limitations under the License.";
    }

    @Override
    public String getName() {
        return String.valueOf(getLicenseText().hashCode());
    }
}
