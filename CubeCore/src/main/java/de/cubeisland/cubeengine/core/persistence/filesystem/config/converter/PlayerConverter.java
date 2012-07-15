package de.cubeisland.cubeengine.core.persistence.filesystem.config.converter;

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
        this.server = ((Plugin)core.getBootstrapper()).getServer();
    }

    public Object from(OfflinePlayer object)
    {
        return object.getName();
    }

    public OfflinePlayer to(Object object)
    {
        return server.getOfflinePlayer((String) object);
    }
}