package de.cubeisland.cubeengine.guests.prevention.punishments;

import de.cubeisland.cubeengine.guests.prevention.Punishment;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Creates an explosion on the player's position.
 */
public class ExplosionPunishment implements Punishment
{
    private final Location helper = new Location(null, 0, 0, 0);

    @Override
    public String getName()
    {
        return "explosion";
    }

    @Override
    public void punish(Player player, ConfigurationSection config)
    {
        player.getWorld().createExplosion(player.getLocation(this.helper), 0);
        player.damage(config.getInt("damage", 3));
    }
}
