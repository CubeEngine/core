package de.cubeisland.cubeengine.core.command.readers;

import de.cubeisland.cubeengine.core.command.ArgumentReader;
import de.cubeisland.cubeengine.core.command.exception.InvalidArgumentException;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class OfflinePlayerReader extends ArgumentReader<OfflinePlayer>
{
    public OfflinePlayerReader()
    {
        super(OfflinePlayer.class);
    }

    @Override
    public OfflinePlayer read(String arg) throws InvalidArgumentException
    {
        return Bukkit.getOfflinePlayer(arg);
    }
}
