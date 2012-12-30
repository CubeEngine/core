package de.cubeisland.cubeengine.conomy.config;

import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import java.util.HashMap;

public class SubCurrencyConverter implements Converter<SubCurrencyConfig>
{
    @Override
    public Object toObject(SubCurrencyConfig object) throws ConversionException
    {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("value", object.value);
        map.put("shortname", object.shortName);
        return map;
    }

    @Override
    public SubCurrencyConfig fromObject(Object object) throws ConversionException
    {
        try
        {
            HashMap<String, Object> map = (HashMap<String, Object>) object;
            String shortName = map.get("shortname").toString();
            int value = Integer.valueOf(map.get("value").toString());
            return new SubCurrencyConfig(shortName, value);
        }
        catch (Exception e)
        {
            throw new ConversionException("Could not parse SubCurrencyConfig!", e);
        }
    }
}
