package de.cubeisland.cubeengine.core.util.converter;

import de.cubeisland.cubeengine.core.util.MaterialMatcher;
import org.bukkit.inventory.ItemStack;

public class ItemStackConverter implements Converter<ItemStack>
{
    @Override
    public Object toObject(ItemStack object) throws ConversionException
    {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public ItemStack fromObject(Object object) throws ConversionException
    {

        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String toString(ItemStack object)
    {
        // TODO
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ItemStack fromString(String string) throws ConversionException
    {
        return MaterialMatcher.get().matchItemStack(string);
    }
}
