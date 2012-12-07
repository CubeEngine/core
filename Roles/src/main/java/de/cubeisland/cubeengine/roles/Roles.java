package de.cubeisland.cubeengine.roles;

import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.roles.commands.ModuleManagementCommands;
import de.cubeisland.cubeengine.roles.commands.RoleCommands;
import de.cubeisland.cubeengine.roles.commands.RoleManagementCommands;
import de.cubeisland.cubeengine.roles.commands.UserManagementCommands;
import de.cubeisland.cubeengine.roles.role.RoleManager;
import de.cubeisland.cubeengine.roles.role.RolesEventHandler;
import de.cubeisland.cubeengine.roles.role.config.PermissionTree;
import de.cubeisland.cubeengine.roles.role.config.PermissionTreeConverter;
import de.cubeisland.cubeengine.roles.role.config.Priority;
import de.cubeisland.cubeengine.roles.role.config.PriorityConverter;
import de.cubeisland.cubeengine.roles.role.config.RoleProvider;
import de.cubeisland.cubeengine.roles.role.config.RoleProviderConverter;
import de.cubeisland.cubeengine.roles.storage.AssignedRoleManager;
import de.cubeisland.cubeengine.roles.storage.UserMetaDataManager;
import de.cubeisland.cubeengine.roles.storage.UserPermissionsManager;

public class Roles extends Module
{
    private RolesConfig config;
    private RoleManager manager;
    private AssignedRoleManager dbManager;
    private UserMetaDataManager dbUserMeta;
    private UserPermissionsManager dbUserPerm;
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
        this.dbUserMeta = new UserMetaDataManager(this.getDatabase());
        this.dbUserPerm = new UserPermissionsManager(this.getDatabase());
        this.manager = new RoleManager(this);
        this.getEventManager().registerListener(this, new RolesEventHandler(this));
        for (User user : this.getUserManager().getOnlineUsers()) // reapply roles on reload
        {
            user.removeAttribute(this, "roleContainer"); // remove potential old calculated roles
            this.manager.preCalculateRoles(user.getName());
            this.manager.applyRole(user.getPlayer(), this.getCore().getWorldManager().getWorldId(user.getWorld()));
        }
        
        this.registerCommand(new RoleCommands(this));
        this.getCommandManager().registerCommand(new RoleManagementCommands(this), "roles");
        this.getCommandManager().registerCommand(new UserManagementCommands(this), "roles");
        this.getCommandManager().registerCommand(new ModuleManagementCommands(this), "roles");
    }

    @Override
    public void onDisable()
    {
        for (User user : this.getUserManager().getLoadedUsers())
        {
            user.clearAttributes(this);
        }
    }

    public RolesConfig getConfiguration()
    {
        return this.config;
    }

    public AssignedRoleManager getDbManager()
    {
        return dbManager;
    }

    public UserMetaDataManager getDbUserMeta()
    {
        return dbUserMeta;
    }

    public UserPermissionsManager getDbUserPerm()
    {
        return dbUserPerm;
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
