package tk.cs8898.elfofflinett.model.licence.licenses;

public class GenericLicense implements ILicense {
    private final String name, url, text;

    public GenericLicense(String name, String url, String text) {
        this.name = name;
        this.url = url;
        this.text = text;
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
        return this.text;
    }

    @Override
    public String getName() {
        return String.valueOf(this.text.hashCode());
    }
}
