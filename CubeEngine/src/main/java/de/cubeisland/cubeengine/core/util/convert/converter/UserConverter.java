package de.cubeisland.cubeengine.core.util.convert.converter;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class UserConverter implements Converter<User>
{
    @Override
    public Object toObject(User object) throws ConversionException
    {
        return object.getName();
    }

    @Override
    public User fromObject(Object object) throws ConversionException
    {
        if (object instanceof OfflinePlayer)
        {
            return CubeEngine.getUserManager().getExactUser((OfflinePlayer)object);
        }
        if (object instanceof CommandSender)
        {
            return CubeEngine.getUserManager().getExactUser((CommandSender)object);
        }
        if (object instanceof String)
        {
            return CubeEngine.getUserManager().findUser(object.toString());
        }
        throw new ConversionException("Could not convert to User!");
    }
}
