package de.cubeisland.cubeengine.conomy.config;

public class SubCurrencyConfig
{
    public String longNamePlural;
    public String shortName;
    public String shortNamePlural;
    public int value;

    public SubCurrencyConfig(String longNamePlural, String shortName, String shortNamePlural, int value)
    {
        this.longNamePlural = longNamePlural;
        this.shortName = shortName;
        this.shortNamePlural = shortNamePlural;
        this.value = value;
    }
}
