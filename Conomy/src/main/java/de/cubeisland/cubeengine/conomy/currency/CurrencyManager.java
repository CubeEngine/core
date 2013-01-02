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
    private Currency mainCurrency;

    public CurrencyManager(Conomy module, ConomyConfiguration config)
    {
        this.module = module;
        this.config = config;
    }

    public void load()
    {
        for (Map.Entry<String, CurrencyConfiguration> entry : config.currencies.entrySet())
        {
            this.currencies.put(entry.getKey(), new Currency(this, entry.getKey(), entry.getValue()));
        }
        this.mainCurrency = this.currencies.get(config.currencies.keySet().iterator().next());
    }

    public Collection<Currency> getAllCurrencies()
    {
        return this.currencies.values();
    }

    public Currency getMainCurrency()
    {
        return mainCurrency;
    }

    public Currency getCurrencyByName(String currencyName)
    {
        return this.currencies.get(currencyName);
    }

    public boolean canConvert(Currency currency1, Currency currency2)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
