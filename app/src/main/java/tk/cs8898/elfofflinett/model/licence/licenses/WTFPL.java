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
        return " Copyright (C) " + this.copyright + "\n" +
                " This program is free software. It comes without any warranty, to\n" +
                " the extent permitted by applicable law.You can redistribute it\n" +
                " and/or modify it under the terms of the Do What The Fuck You Want\n" +
                " To Public License, Version 2, as published by Sam Hocevar. See\n" +
                " http://www.wtfpl.net/ for more details.";
    }
}
