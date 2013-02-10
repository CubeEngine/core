package de.cubeisland.cubeengine.core.command.readers;

import de.cubeisland.cubeengine.core.command.ArgumentReader;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import org.bukkit.inventory.ItemStack;

public class ItemStackReader extends ArgumentReader<ItemStack>
{
    public ItemStackReader()
    {
        super(ItemStack.class);
    }

    @Override
    public ItemStack read(String arg)
    {
        return Match.material().itemStack(arg);
    }
}
