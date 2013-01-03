package de.cubeisland.cubeengine.conomy.currency;

import com.google.common.collect.Lists;
import de.cubeisland.cubeengine.conomy.config.CurrencyConfiguration;
import de.cubeisland.cubeengine.conomy.config.SubCurrencyConfig;
import java.util.LinkedList;
import java.util.Map;

public class Currency
{

    private LinkedList<SubCurrency> sub = new LinkedList<SubCurrency>();
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

    public String formatLong(Long balance)
    {
        String format = this.formatlong;
        for (SubCurrency subcur : Lists.reverse(this.sub))
        {
            Long subBalance = balance % subcur.getValueForParent();
            if (subcur.equals(this.sub.getFirst()))
            {
                format = format.replace("%" + subcur.getSymbol(), balance.toString());
            }
            else
            {
                format = format.replace("%" + subcur.getSymbol(), subBalance.toString());
            }
            balance -= balance % subcur.getValueForParent();
            balance /= subcur.getValueForParent();
        }
        return format;
    }
}
