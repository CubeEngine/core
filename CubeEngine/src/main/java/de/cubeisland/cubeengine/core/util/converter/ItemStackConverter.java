package de.cubeisland.cubeengine.core.util.converter;

import de.cubeisland.cubeengine.core.util.matcher.MaterialMatcher;
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
        if (object instanceof ItemStack)
        {
            return (ItemStack)object;
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toString(ItemStack object)
    {
        return object.getType().getId() + ":" + object.getDurability();
    }

    @Override
    public ItemStack fromString(String string) throws ConversionException
    {
        return MaterialMatcher.get().matchItemStack(string);
    }
}