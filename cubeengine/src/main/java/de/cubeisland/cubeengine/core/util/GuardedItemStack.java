package de.cubeisland.cubeengine.core.util;

import org.bukkit.inventory.ItemStack;

public class GuardedItemStack
{
    final ItemStack item;
    final int amount;

    public GuardedItemStack(ItemStack item, int amount)
    {
        this.item = item;
        this.amount = amount;
    }
}
