package de.cubeisland.cubeengine.conomy.config;

import java.util.LinkedHashMap;

public class CurrencyConfiguration
{
    public LinkedHashMap<String, SubCurrencyConfig> subcurrencies;
    public String formatLong;
    public String formatShort;
    public long defaultBalance;
    public long minimumBalance;
    public String decimalSeparator;
    public String thousandSeparator;

    public CurrencyConfiguration(LinkedHashMap<String, SubCurrencyConfig> subcurrencies, String formatLong, String formatShort,
                                 long defaultBalance, long minimumBalance, String decimalSeparator, String thousandSeparator) {
        this.subcurrencies = subcurrencies;
        this.formatLong = formatLong;
        this.formatShort = formatShort;
        this.defaultBalance = defaultBalance;
        this.minimumBalance = minimumBalance;
        this.decimalSeparator = decimalSeparator;
        this.thousandSeparator = thousandSeparator;
    }
}
