package de.cubeisland.cubeengine.roles.role;

import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Converter;
import java.util.List;

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
        PermissionTree permTree = new PermissionTree();
        if (object instanceof List)
        {
            permTree.loadFromList((List) object, "");
        }
        return permTree;
    }
}
