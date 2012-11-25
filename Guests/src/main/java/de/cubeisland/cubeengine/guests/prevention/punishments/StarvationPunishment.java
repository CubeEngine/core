package de.cubeisland.cubeengine.guests.prevention.punishments;

import de.cubeisland.cubeengine.guests.prevention.Punishment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Lets a user starve.
 */
public class StarvationPunishment implements Punishment
{
    @Override
    public String getName()
    {
        return "starvation";
    }

    @Override
    public void punish(Player player, ConfigurationSection config)
    {
        player.setSaturation(0);
        player.setFoodLevel(0);
    }
}
