package de.cubeisland.cubeengine.conomy.config;

import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class CurrencyConfigurationConverter implements Converter<CurrencyConfiguration>
{
    @Override
    public Object toObject(CurrencyConfiguration object) throws ConversionException
    {
        LinkedHashMap<String, Object> currency = new LinkedHashMap<String, Object>();
        LinkedHashMap<String, Object> subCurrencies = new LinkedHashMap<String, Object>();
        for (Map.Entry<String, SubCurrencyConfig> entry : object.subcurrencies.entrySet())
        {
            subCurrencies.put(entry.getKey(), Convert.toObject(entry.getValue()));
        }
        currency.put("sub-currencies", subCurrencies);
        HashMap<String, Object> firstSub = (HashMap<String, Object>)subCurrencies.entrySet().iterator().next().getValue();
        firstSub.remove("value");
        LinkedHashMap<String, Object> format = new LinkedHashMap<String, Object>();
        currency.put("formatting", format);
        format.put("long", object.formatLong);
        format.put("short", object.formatShort);
        currency.put("default-balance", object.defaultBalance);
        return currency;
    }

    @Override
    public CurrencyConfiguration fromObject(Object object) throws ConversionException
    {
        try
        {
            LinkedHashMap<String, Object> currency = (LinkedHashMap<String, Object>)object;
            LinkedHashMap<String, Object> subCurrencies = (LinkedHashMap<String, Object>)currency.get("sub-currencies");
            LinkedHashMap<String, String> format = (LinkedHashMap<String, String>)currency.get("formatting");
            LinkedHashMap<String, SubCurrencyConfig> subConfigs = new LinkedHashMap<String, SubCurrencyConfig>();
            for (Map.Entry<String, Object> entry : subCurrencies.entrySet())
            {
                subConfigs.put(entry.getKey(), (SubCurrencyConfig)Convert.fromObject(SubCurrencyConfig.class, entry.getValue()));
            }
            Long defaultBalance = Long.parseLong(currency.get("default-balance").toString());
            CurrencyConfiguration currencyConfig = new CurrencyConfiguration(subConfigs, format.get("long"), format.get("short"), defaultBalance);
            return currencyConfig;
        }
        catch (Exception e)
        {
            throw new ConversionException("Could not convert Currency-Configuration!", e);
        }
    }
}
