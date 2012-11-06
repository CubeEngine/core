package de.cubeisland.cubeengine.guests.prevention.punishments;

import de.cubeisland.cubeengine.guests.prevention.Punishment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Kills a player
 *
 * @author Phillip Schichtel
 */
public class KillPunishment implements Punishment
{
    public String getName()
    {
        return "kill";
    }

    public void punish(Player player, ConfigurationSection config)
    {
        player.setHealth(0);
    }
}
