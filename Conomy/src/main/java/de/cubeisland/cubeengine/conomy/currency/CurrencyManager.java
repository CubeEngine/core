package de.cubeisland.cubeengine.conomy.currency;

import de.cubeisland.cubeengine.conomy.Conomy;
import de.cubeisland.cubeengine.conomy.config.ConomyConfiguration;
import de.cubeisland.cubeengine.conomy.config.CurrencyConfiguration;
import gnu.trove.map.hash.THashMap;
import java.util.Collection;
import java.util.Map;

public class CurrencyManager
{
    private THashMap<String, Currency> currencies = new THashMap<String, Currency>();
    private Conomy module;
    private ConomyConfiguration config;

    public CurrencyManager(Conomy module, ConomyConfiguration config)
    {
        this.module = module;
        this.config = config;
    }

    public void load()
    {
        for (Map.Entry<String, CurrencyConfiguration> entry : config.currencies.entrySet())
        {
            this.currencies.put(entry.getKey(), new Currency(entry.getKey(), entry.getValue()));
        }
    }

    public Collection<Currency> getAllCurrencies()
    {
        return this.currencies.values();
    }

    public Currency getMainCurrency()
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
