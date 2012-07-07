package de.cubeisland.cubeengine.core.persistence.filesystem.config.converter;

import de.cubeisland.cubeengine.CubeEngine;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;

/**
 *
 * @author Faithcaio
 */
public class PlayerConverter implements Converter<OfflinePlayer>
{
    private Server server;

    public PlayerConverter()
    {
        this.server = CubeEngine.getCore().getServer();
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