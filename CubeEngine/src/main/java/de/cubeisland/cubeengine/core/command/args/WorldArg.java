package de.cubeisland.cubeengine.core.command.args;

import de.cubeisland.cubeengine.core.command.ArgumentReader;
import de.cubeisland.cubeengine.core.command.InvalidArgumentException;
import de.cubeisland.cubeengine.core.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;

public class WorldArg extends ArgumentReader<World>
{
    private final Server server;

    public WorldArg()
    {
        super(World.class);
        this.server = Bukkit.getServer();
    }

    @Override
    public Pair<Integer, World> read(String... args) throws InvalidArgumentException
    {
        World value = this.server.getWorld(args[0]);
        return new Pair<Integer, World>(value == null ? 0 : 1, value);
    }
}
