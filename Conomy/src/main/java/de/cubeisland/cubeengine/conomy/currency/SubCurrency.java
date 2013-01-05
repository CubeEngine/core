package de.cubeisland.cubeengine.conomy.currency;

import de.cubeisland.cubeengine.conomy.config.SubCurrencyConfig;

public class SubCurrency
{
    private String name;
    private String symbol;
    private SubCurrency parent = null;
    private long valueForParent;
    private long valueInLowest;

    public SubCurrency(String name, SubCurrencyConfig config, SubCurrency parent)
    {
        this.name = name;
        this.symbol = config.shortName;
        this.valueForParent = config.value;
        this.parent = parent;
    }

    public void setvalueInLowest(long valueInLowest)
    {
        this.valueInLowest = valueInLowest;
    }

    public String getSymbol()
    {
        return this.symbol;
    }

    public long getValueForParent()
    {
        return this.valueForParent;
    }

    public long getValueInLowest()
    {
        return valueInLowest;
    }

    public SubCurrency getParent()
    {
        return parent;
    }

    public String getName()
    {
        return name;
    }
}
