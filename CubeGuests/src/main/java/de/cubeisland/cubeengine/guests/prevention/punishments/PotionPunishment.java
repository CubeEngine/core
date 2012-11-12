package de.cubeisland.cubeengine.guests.prevention.punishments;

import de.cubeisland.cubeengine.guests.prevention.Punishment;
import java.util.Locale;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Poisons a player.
 */
public class PotionPunishment implements Punishment
{
    public String getName()
    {
        return "potion";
    }

    public void punish(Player player, ConfigurationSection config)
    {
        player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(config.getString("effect").toUpperCase(Locale.ENGLISH)), config.getInt("duration", 3) * 20, config.getInt("amplifier", 1)));
    }
}
