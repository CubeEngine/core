package de.cubeisland.cubeengine.core.util.converter.generic;

import de.cubeisland.cubeengine.core.util.converter.Convert;
import org.bukkit.inventory.ItemStack;

public class ItemStackArrayConverter extends ArraysConverter<ItemStack>
{
    public ItemStackArrayConverter()
    {
        this.converter = Convert.matchConverter(ItemStack.class);
        this.clazz = ItemStack.class;
    }
}