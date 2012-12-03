package de.cubeisland.cubeengine.roles;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.roles.role.PermissionTree;
import de.cubeisland.cubeengine.roles.role.PermissionTreeConverter;
import de.cubeisland.cubeengine.roles.role.Priority;
import de.cubeisland.cubeengine.roles.role.PriorityConverter;
import de.cubeisland.cubeengine.roles.role.RoleManager;

public class Roles extends Module
{
    private RolesConfig config;
    private RoleManager manager;

    @Override
    public void onEnable()
    {
        Convert.registerConverter(PermissionTree.class, new PermissionTreeConverter());
        Convert.registerConverter(Priority.class, new PriorityConverter());
        this. manager = new RoleManager(this);
    }

    public RolesConfig getConfiguration()
    {
        return this.config;
    }

    public RoleManager getManager()
    {
        return manager;
    }
    
}
