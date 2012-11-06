package de.cubeisland.cubeengine.guests.prevention.punishments;

import de.cubeisland.cubeengine.guests.prevention.Punishment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Shoots a player into the skys
 *
 * @author Phillip Schichtel
 */
public class RocketPunishment implements Punishment
{
    public String getName()
    {
        return "rocket";
    }

    public void punish(Player player, ConfigurationSection config)
    {
        player.setVelocity(player.getVelocity().add(new Vector(0, config.getInt("height", 50), 0)));
    }
}
