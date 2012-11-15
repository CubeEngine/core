package de.cubeisland.cubeengine.fun.commands.help;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.user.User;
import org.bukkit.Location;
import org.bukkit.Material;

public class RocketCMDInstance
{
    private final String name;
    private final int    maxTicks;

    private int          ticks;

    public RocketCMDInstance(String name, int ticks)
    {
        this.name = name;
        this.maxTicks = ticks;
        this.ticks = 0;
    }

    public User getUser()
    {
        return CubeEngine.getUserManager().getUser(name, true);
    }

    public void addTick()
    {
        this.ticks++;
    }

    public int getTicks()
    {
        return this.ticks;
    }

    public String getName()
    {
        return this.name;
    }

    public int getMaxTicks()
    {
        return this.maxTicks;
    }

    public int getNumberOfAirBlocksUnderFeets()
    {
        Location location = this.getUser().getLocation().subtract(0, 1, 0);
        int numberOfAirBlocks = 0;

        while (location.getBlock().getType() == Material.AIR)
        {
            numberOfAirBlocks++;
            location.subtract(0, 1, 0);
        }

        return numberOfAirBlocks;
    }
}
