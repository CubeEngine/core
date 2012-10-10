package de.cubeisland.cubeengine.core.util.converter;

import de.cubeisland.cubeengine.core.util.EnchantMatcher;
import org.bukkit.enchantments.Enchantment;

public class EnchantmentConverter implements Converter<Enchantment>
{
    @Override
    public Object toObject(Enchantment object) throws ConversionException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Enchantment fromObject(Object object) throws ConversionException
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toString(Enchantment object)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Enchantment fromString(String string) throws ConversionException
    {
        return EnchantMatcher.get().matchEnchantment(string);
    }
}
