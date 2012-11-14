package de.cubeisland.cubeengine.core.command.args;

import de.cubeisland.cubeengine.core.command.ArgumentReader;
import de.cubeisland.cubeengine.core.command.InvalidArgumentException;
import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.core.util.matcher.EnchantMatcher;
import org.bukkit.enchantments.Enchantment;

public class EnchantmentArg extends ArgumentReader<Enchantment>
{
    public EnchantmentArg()
    {
        super(Enchantment.class);
    }

    @Override
    public Pair<Integer, Enchantment> read(String... args) throws InvalidArgumentException
    {
        Enchantment value = EnchantMatcher.get().matchEnchantment(args[0]);
        return new Pair<Integer, Enchantment>(0, value);
    }
}
