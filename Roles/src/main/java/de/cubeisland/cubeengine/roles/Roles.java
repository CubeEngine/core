package de.cubeisland.cubeengine.roles;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.roles.role.PermissionTree;
import de.cubeisland.cubeengine.roles.role.PermissionTreeConverter;
import de.cubeisland.cubeengine.roles.role.Priority;
import de.cubeisland.cubeengine.roles.role.PriorityConverter;
import de.cubeisland.cubeengine.roles.role.RoleManager;
import de.cubeisland.cubeengine.roles.role.RoleProvider;
import de.cubeisland.cubeengine.roles.role.RoleProviderConverter;
import de.cubeisland.cubeengine.roles.role.RolesEventHandler;
import de.cubeisland.cubeengine.roles.storage.AssignedRoleManager;

public class Roles extends Module
{

    private RolesConfig config;
    private RoleManager manager;
    private AssignedRoleManager dbManager;
    private static Roles instance;

    public Roles()
    {
        instance = this; // Needed in configuration loading
        Convert.registerConverter(PermissionTree.class, new PermissionTreeConverter());
        Convert.registerConverter(Priority.class, new PriorityConverter());
        Convert.registerConverter(RoleProvider.class, new RoleProviderConverter());
    }

    @Override
    public void onEnable()
    {
        this.dbManager = new AssignedRoleManager(this.getDatabase());
        this.manager = new RoleManager(this);
        
        this.getEventManager().registerListener(this, new RolesEventHandler(this));
    }

    public RolesConfig getConfiguration()
    {
        return this.config;
    }

    public AssignedRoleManager getDbManager()
    {
        return dbManager;
    }

    public RoleManager getManager()
    {
        return manager;
    }

    public static Roles getInstance()
    {
        return instance;
    }
}
