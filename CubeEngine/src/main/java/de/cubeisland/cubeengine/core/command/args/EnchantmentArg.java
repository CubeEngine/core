package de.cubeisland.cubeengine.core.command.args;

import de.cubeisland.cubeengine.core.command.AbstractArgument;
import de.cubeisland.cubeengine.core.command.InvalidArgumentException;
import de.cubeisland.cubeengine.core.util.matcher.EnchantMatcher;
import org.bukkit.enchantments.Enchantment;

public class EnchantmentArg extends AbstractArgument<Enchantment>
{

    public EnchantmentArg()
    {
        super(Enchantment.class);
    }

    @Override
    public int read(String... args) throws InvalidArgumentException
    {
        this.value = EnchantMatcher.get().matchEnchantment(args[0]);
        return this.value == null ? 0 : 1;
    }
}
