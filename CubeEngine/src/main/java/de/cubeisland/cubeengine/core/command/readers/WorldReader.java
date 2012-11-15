package de.cubeisland.cubeengine.core.command.readers;

import de.cubeisland.cubeengine.core.command.ArgumentReader;
import de.cubeisland.cubeengine.core.command.InvalidArgumentException;
import de.cubeisland.cubeengine.core.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;

public class WorldReader extends ArgumentReader<World>
{
    private final Server server;

    public WorldReader()
    {
        super(World.class);
        this.server = Bukkit.getServer();
    }

    @Override
    public Pair<Integer, World> read(String... args) throws InvalidArgumentException
    {
        World value = this.server.getWorld(args[0]);
        return new Pair<Integer, World>(0, value);
    }
}
