package de.cubeisland.cubeengine.core.util.converter;

import de.cubeisland.cubeengine.core.Core;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

public class PlayerConverter implements Converter<OfflinePlayer>
{
    private Server server;

    public PlayerConverter(Core core)
    {
        this.server = ((Plugin)core).getServer();
    }

    @Override
    public Object toObject(OfflinePlayer object)
    {
        return this.toString(object);
    }

    @Override
    public OfflinePlayer fromObject(Object object)
    {
        return this.fromString(String.valueOf(object));
    }

    @Override
    public String toString(OfflinePlayer object)
    {
        return object.getName();
    }

    @Override
    public OfflinePlayer fromString(String string)
    {
        return this.server.getOfflinePlayer(string);
    }
}