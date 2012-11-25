package de.cubeisland.cubeengine.guests.prevention.punishments;

import de.cubeisland.cubeengine.guests.prevention.Punishment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Kills a player.
 */
public class KillPunishment implements Punishment
{
    @Override
    public String getName()
    {
        return "kill";
    }

    @Override
    public void punish(Player player, ConfigurationSection config)
    {
        player.setHealth(0);
    }
}
