package de.cubeisland.cubeengine.guests.prevention.punishments;

import de.cubeisland.cubeengine.guests.prevention.Punishment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Bans a player.
 */
public class LightningPunishment implements Punishment
{
    @Override
    public String getName()
    {
        return "lightning";
    }

    @Override
    public void punish(Player player, ConfigurationSection config)
    {
        player.getWorld().strikeLightningEffect(player.getLocation());
        player.damage(config.getInt("damage", 3));
    }
}
