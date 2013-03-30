package de.cubeisland.cubeengine.guests.prevention.punishments;

import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.guests.prevention.Punishment;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Kicks a player.
 */
public class KickPunishment implements Punishment
{
    @Override
    public String getName()
    {
        return "kick";
    }

    @Override
    public void punish(Player player, ConfigurationSection config)
    {
        player.kickPlayer(ChatFormat.parseFormats(config.getString("reason", "&cYou were kicked as a punishment!")));
    }
}
