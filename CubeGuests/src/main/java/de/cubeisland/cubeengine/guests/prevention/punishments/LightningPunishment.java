package de.cubeisland.cubeengine.guests.prevention.punishments;

import de.cubeisland.cubeengine.guests.prevention.Punishment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Bans a player
 *
 * @author Phillip Schichtel
 */
public class LightningPunishment implements Punishment
{
    public String getName()
    {
        return "lightning";
    }

    public void punish(Player player, ConfigurationSection config)
    {
        player.getWorld().strikeLightningEffect(player.getLocation());
        player.damage(config.getInt("damage", 3));
    }
}
