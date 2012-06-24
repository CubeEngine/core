package de.cubeisland.cubeengine.core.persistence.filesystem.config.converter;

import de.cubeisland.cubeengine.core.CubeCore;
import java.util.ArrayList;
import java.util.List;
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
        this.server = CubeCore.getInstance().getServer();
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