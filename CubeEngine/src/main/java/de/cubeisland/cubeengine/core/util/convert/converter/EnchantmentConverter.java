package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import de.cubeisland.cubeengine.core.util.matcher.EnchantMatcher;
import org.bukkit.enchantments.Enchantment;

public class EnchantmentConverter implements Converter<Enchantment>
{
    @Override
    public Object toObject(Enchantment object) throws ConversionException
    {
        return EnchantMatcher.get().getNameFor(object);
    }

    @Override
    public Enchantment fromObject(Object object) throws ConversionException
    {
        if (object instanceof String)
        {
            return EnchantMatcher.get().matchEnchantment(object.toString());
        }
        throw new ConversionException("Could not convert to Enchantment!");
    }
}