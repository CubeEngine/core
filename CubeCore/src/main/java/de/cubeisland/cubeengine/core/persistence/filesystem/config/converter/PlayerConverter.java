package de.cubeisland.cubeengine.core.persistence.filesystem.config.converter;

import de.cubeisland.cubeengine.core.CubeCore;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author Faithcaio
 */
public class PlayerConverter implements Converter<OfflinePlayer>
{
    public Object from(OfflinePlayer object)
    {
        return object.getName();
    }

    public OfflinePlayer to(Object object)
    {
        return CubeCore.getInstance().getServer().getOfflinePlayer((String) object);
    }
}