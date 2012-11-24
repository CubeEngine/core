package de.cubeisland.cubeengine.core.command.readers;

import de.cubeisland.cubeengine.core.command.ArgumentReader;
import de.cubeisland.cubeengine.core.command.InvalidArgumentException;
import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.core.util.matcher.MaterialMatcher;
import org.bukkit.inventory.ItemStack;

public class ItemStackReader extends ArgumentReader<ItemStack>
{
    public ItemStackReader()
    {
        super(ItemStack.class);
    }

    @Override
    public Pair<Integer, ItemStack> read(String... args) throws InvalidArgumentException
    {
        ItemStack value = MaterialMatcher.get().matchItemStack(args[0]);
        return new Pair<Integer, ItemStack>(1, value);
    }
}
