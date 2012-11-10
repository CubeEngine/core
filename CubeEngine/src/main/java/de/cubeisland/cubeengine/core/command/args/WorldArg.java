package de.cubeisland.cubeengine.core.command.args;

import de.cubeisland.cubeengine.core.command.AbstractArgument;
import de.cubeisland.cubeengine.core.command.InvalidArgumentException;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;

public class WorldArg extends AbstractArgument<World>
{
    private final Server server;
    
    public WorldArg()
    {
        super(World.class);
        this.server = Bukkit.getServer();
    }

    @Override
    public int read(String... args) throws InvalidArgumentException
    {
        this.value = this.server.getWorld(args[0]);
        return this.value == null ? 0 : 1;
    }
}
