package de.cubeisland.cubeengine.core.util.converter;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.user.User;

public class UserConverter implements Converter<User>
{
    @Override
    public Object toObject(User object) throws ConversionException
    {
        return this.toString(object);
    }

    @Override
    public User fromObject(Object object) throws ConversionException
    {
        return this.fromString(String.valueOf(object));
    }

    @Override
    public String toString(User object)
    {
        return object.getName();
    }

    @Override
    public User fromString(String string) throws ConversionException
    {
        return CubeEngine.getUserManager().findUser(string);
    }
}
