package de.cubeisland.cubeengine.roles.role;

import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Converter;

public class PermissionTreeConverter implements Converter<PermissionTree>
{
    @Override
    public Object toObject(PermissionTree object) throws ConversionException
    {
        return object.convertToConfigObject();
    }

    @Override
    public PermissionTree fromObject(Object object) throws ConversionException
    {
        return PermissionTree.fromConfigObject(object);
    }
}
