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
        return object.getName();
    }

    @Override
    public OfflinePlayer fromObject(Object object) throws ConversionException
    {
        if (object instanceof String)
        {
            return this.server.getOfflinePlayer((String)object);
        }
        throw new ConversionException("Could not convert to OfflinePlayer!");
    }
}