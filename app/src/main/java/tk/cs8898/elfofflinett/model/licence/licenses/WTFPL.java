package tk.cs8898.elfofflinett.model.licence.licenses;

@SuppressWarnings("SpellCheckingInspection")
public class WTFPL implements ILicense {
    private static final String NAME = "WTFPL";

    private final String name, url;
    private final String copyright;

    public WTFPL(String name, String url, String copyright) {
        this.name = name;
        this.url = url;
        this.copyright = copyright;
    }

    @Override
    public String getName() {
        return NAME;
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
        return "            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE\n" +
                "                    Version 2, December 2004\n" +
                "\n" +
                " Copyright (C) "+this.copyright+"\n" +
                "\n" +
                " Everyone is permitted to copy and distribute verbatim or modified\n" +
                " copies of this license document, and changing it is allowed as long\n" +
                " as the name is changed.\n" +
                "\n" +
                "            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE\n" +
                "   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION\n" +
                "\n" +
                "  0. You just DO WHAT THE FUCK YOU WANT TO.\n" +
                "\n";
    }
}
