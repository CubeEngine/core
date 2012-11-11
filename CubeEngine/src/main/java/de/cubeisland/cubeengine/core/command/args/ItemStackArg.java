package de.cubeisland.cubeengine.core.command.args;

import de.cubeisland.cubeengine.core.command.AbstractArgument;
import de.cubeisland.cubeengine.core.command.InvalidArgumentException;
import de.cubeisland.cubeengine.core.util.matcher.MaterialMatcher;
import org.bukkit.inventory.ItemStack;

public class ItemStackArg extends AbstractArgument<ItemStack>
{
    public ItemStackArg()
    {
        super(ItemStack.class);
    }

    @Override
    public int read(String... args) throws InvalidArgumentException
    {
        this.value = MaterialMatcher.get().matchItemStack(args[0]);
        return this.value == null ? 0 : 1;
    }
}
