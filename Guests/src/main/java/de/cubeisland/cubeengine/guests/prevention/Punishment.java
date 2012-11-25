package de.cubeisland.cubeengine.guests.prevention;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Represents a punishment.
 */
public interface Punishment
{
    /**
     * Returns the name of the prevention
     *
     * @return the name
     */
    public String getName();

    /**
     * Punishes a player
     *
     * @param player the player to punish
     */
    public void punish(Player player, ConfigurationSection config);
}
