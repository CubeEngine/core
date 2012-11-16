package de.cubeisland.cubeengine.guests.prevention.punishments;

import de.cubeisland.cubeengine.guests.prevention.Punishment;
import java.util.Random;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Slaps a player.
 */
public class SlapPunishment implements Punishment
{
    private final Random rand;

    public SlapPunishment()
    {
        this.rand = new Random();
    }

    @Override
    public String getName()
    {
        return "slap";
    }

    @Override
    public void punish(Player player, ConfigurationSection config)
    {
        player.damage(config.getInt("damage", 3));
        player.setVelocity(player.getVelocity().add(new Vector(-rand.nextInt(5), rand.nextInt(2), -rand.nextInt(5))));
    }
}
