package de.cubeisland.cubeengine.roles.role.config;

import de.cubeisland.cubeengine.core.config.node.Node;
import de.cubeisland.cubeengine.core.util.convert.ConversionException;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.convert.Converter;

import java.util.List;

public class PermissionTreeConverter implements Converter<PermissionTree>
{
    @Override
    public Node toNode(PermissionTree object) throws ConversionException
    {
        return Convert.wrapIntoNode(object.convertToConfigObject());
    }

    //TODO check if this is still working

    @Override
    public PermissionTree fromNode(Node node) throws ConversionException
    {
        //TODO FIX IT!!!!!!
        PermissionTree permTree = new PermissionTree();
        if (node instanceof List)
        {
            permTree.loadFromList((List)node, "");
        }
        return permTree;
    }
}
