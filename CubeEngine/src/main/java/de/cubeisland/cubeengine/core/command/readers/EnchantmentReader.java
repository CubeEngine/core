package de.cubeisland.cubeengine.core.command.readers;

import de.cubeisland.cubeengine.core.command.ArgumentReader;
import de.cubeisland.cubeengine.core.command.InvalidArgumentException;
import de.cubeisland.cubeengine.core.util.Pair;
import de.cubeisland.cubeengine.core.util.matcher.EnchantMatcher;
import org.bukkit.enchantments.Enchantment;

public class EnchantmentReader extends ArgumentReader<Enchantment>
{
    public EnchantmentReader()
    {
        super(Enchantment.class);
    }

    @Override
    public Pair<Integer, Enchantment> read(String... args) throws InvalidArgumentException
    {
        Enchantment value = EnchantMatcher.get().matchEnchantment(args[0]);
        return new Pair<Integer, Enchantment>(1, value);
    }
}
