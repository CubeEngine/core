package de.cubeisland.cubeengine.guests.prevention.punishments;

import de.cubeisland.cubeengine.guests.prevention.Punishment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Creates an explosion on the player's position
 *
 * @author Phillip Schichtel
 */
public class ExplosionPunishment implements Punishment
{
    public String getName()
    {
        return "explosion";
    }

    public void punish(Player player, ConfigurationSection config)
    {
        player.getWorld().createExplosion(player.getLocation(), 0);
        player.damage(config.getInt("damage", 3));
    }
}
