package de.cubeisland.cubeengine.guests.prevention.punishments;

import de.cubeisland.cubeengine.guests.prevention.Punishment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Lets a user starve
 *
 * @author Phillip Schichtel
 */
public class StarvationPunishment implements Punishment
{
    public String getName()
    {
        return "starvation";
    }

    public void punish(Player player, ConfigurationSection config)
    {
        player.setSaturation(0);
    }
}
