package de.cubeisland.cubeengine.guests.prevention.punishments;

import de.cubeisland.cubeengine.guests.prevention.Punishment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Sends a awkward message from the user.
 */
public class MessagePunishment implements Punishment
{
    public String getName()
    {
        return "message";
    }

    public void punish(Player player, ConfigurationSection config)
    {
        if (config.contains("message"))
        {
            player.chat(config.getString("message"));
        }
    }
}
