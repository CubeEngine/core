package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import de.cubeisland.cubeengine.core.util.matcher.MaterialMatcher;
import org.bukkit.inventory.ItemStack;

public class ItemStackConverter implements Converter<ItemStack>
{
    @Override
    public Object toObject(ItemStack object) throws ConversionException
    {
        return object.getType().getId() + ":" + object.getDurability();
    }

    @Override
    public ItemStack fromObject(Object object) throws ConversionException
    {
        if (object instanceof ItemStack)
        {
            return (ItemStack)object;
        }
        else if (object instanceof String)
        {
            return MaterialMatcher.get().matchItemStack(object.toString());
        }
        throw new ConversionException("Could not convert to ItemStack!");
    }
}
