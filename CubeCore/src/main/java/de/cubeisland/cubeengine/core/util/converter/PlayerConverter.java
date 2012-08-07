package de.cubeisland.cubeengine.core.util.converter;

import de.cubeisland.cubeengine.core.Core;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Anselm Brehme
 */
public class PlayerConverter implements Converter<OfflinePlayer>
{
    private Server server;

    public PlayerConverter(Core core)
    {
        this.server = ((Plugin)core).getServer();
    }

    public Object toObject(OfflinePlayer object)
    {
        return this.toString(object);
    }

    public OfflinePlayer fromObject(Object object)
    {
        return this.fromString(String.valueOf(object));
    }

    public String toString(OfflinePlayer object)
    {
        return object.getName();
    }

    public OfflinePlayer fromString(String string)
    {
        return this.server.getOfflinePlayer(string);
    }
}