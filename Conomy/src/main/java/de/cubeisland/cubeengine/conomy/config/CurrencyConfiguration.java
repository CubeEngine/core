package de.cubeisland.cubeengine.conomy.config;

import java.util.LinkedHashMap;

public class CurrencyConfiguration
{//TODO singular / plural names
    public LinkedHashMap<String, SubCurrencyConfig> subcurrencies;
    public String formatLong;
    public String formatShort;

    public CurrencyConfiguration(LinkedHashMap<String, SubCurrencyConfig> subcurrencies, String formatLong, String formatShort)
    {
        this.subcurrencies = subcurrencies;
        this.formatLong = formatLong;
        this.formatShort = formatShort;
    }
}
