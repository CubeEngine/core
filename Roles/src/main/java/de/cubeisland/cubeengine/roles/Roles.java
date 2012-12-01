package de.cubeisland.cubeengine.roles;

import de.cubeisland.cubeengine.core.config.annotations.LoadFrom;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.roles.role.PermissionTree;
import de.cubeisland.cubeengine.roles.role.PermissionTreeConverter;
import de.cubeisland.cubeengine.roles.role.Priority;
import de.cubeisland.cubeengine.roles.role.PriorityConverter;
import de.cubeisland.cubeengine.roles.role.RoleConfig;

public class Roles extends Module
{
    private RolesConfig config;
    @LoadFrom("testconfig")
    private RoleConfig testconfig;

    static
    {
        Convert.registerConverter(PermissionTree.class, new PermissionTreeConverter());
        Convert.registerConverter(Priority.class, new PriorityConverter());
    }

    @Override
    public void onEnable()
    {
    }
}
