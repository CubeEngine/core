package de.cubeisland.cubeengine.core.command.readers;

import de.cubeisland.cubeengine.core.command.ArgumentReader;
import de.cubeisland.cubeengine.core.command.exception.InvalidArgumentException;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class WorldReader extends ArgumentReader<World>
{
    public WorldReader()
    {
        super(World.class);
    }

    @Override
    public World read(String arg) throws InvalidArgumentException
    {
        return Bukkit.getWorld(arg);
    }
}
