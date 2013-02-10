package de.cubeisland.cubeengine.core.command.readers;

import de.cubeisland.cubeengine.core.command.ArgumentReader;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import org.bukkit.enchantments.Enchantment;

public class EnchantmentReader extends ArgumentReader<Enchantment>
{
    public EnchantmentReader()
    {
        super(Enchantment.class);
    }

    @Override
    public Enchantment read(String arg)
    {
        return Match.enchant().enchantment(arg);
    }
}
