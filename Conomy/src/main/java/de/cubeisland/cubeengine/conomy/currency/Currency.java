package de.cubeisland.cubeengine.conomy.currency;

import de.cubeisland.cubeengine.conomy.config.CurrencyConfiguration;
import de.cubeisland.cubeengine.conomy.config.SubCurrencyConfig;
import java.util.LinkedHashSet;
import java.util.Map;

public class Currency
{
    private LinkedHashSet<SubCurrency> sub = new LinkedHashSet<SubCurrency>();
    private String formatlong;
    private String formatshort;
    private String name;
    private CurrencyManager manager;

    public Currency(CurrencyManager manager, String name, CurrencyConfiguration config)
    {
        this.manager = manager;
        this.name = name;
        this.formatlong = config.formatLong;
        this.formatshort = config.formatShort;
        SubCurrency parent = null;
        for (Map.Entry<String, SubCurrencyConfig> entry : config.subcurrencies.entrySet())
        {
            parent = new SubCurrency(entry.getKey(), entry.getValue(), parent);
            this.sub.add(parent);
        }
    }

    public String getName()
    {
        return name;
    }

    public boolean canConvert(Currency currency)
    {
        return this.manager.canConvert(this, currency);
    }

    public Long getDefaultValue()
    {//Default value in config
        return 42L; //TODO
    }
}
