/**
 * This file is part of CubeEngine.
 * CubeEngine is licensed under the GNU General Public License Version 3.
 *
 * CubeEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CubeEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CubeEngine.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cubeisland.cubeengine.roles;

import de.cubeisland.cubeengine.core.command.CommandManager;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.roles.commands.*;
import de.cubeisland.cubeengine.roles.commands.ManagementCommands;
import de.cubeisland.cubeengine.roles.role.RolesEventHandler;
import de.cubeisland.cubeengine.roles.config.PermissionTree;
import de.cubeisland.cubeengine.roles.config.PermissionTreeConverter;
import de.cubeisland.cubeengine.roles.config.Priority;
import de.cubeisland.cubeengine.roles.config.PriorityConverter;
import de.cubeisland.cubeengine.roles.config.RoleMirror;
import de.cubeisland.cubeengine.roles.config.RoleMirrorConverter;
import de.cubeisland.cubeengine.roles.storage.AssignedRoleManager;
import de.cubeisland.cubeengine.roles.storage.UserMetaDataManager;
import de.cubeisland.cubeengine.roles.storage.UserPermissionsManager;

public class Roles extends Module
{
    private RolesConfig config;
    private RoleManager roleManager;
    private AssignedRoleManager dbManager;
    private UserMetaDataManager dbUserMeta;
    private UserPermissionsManager dbUserPerm;
    private RolesAPI api;

    public Roles()
    {
        Convert.registerConverter(PermissionTree.class, new PermissionTreeConverter(this));
        Convert.registerConverter(Priority.class, new PriorityConverter());
        Convert.registerConverter(RoleMirror.class, new RoleMirrorConverter(this));
    }

    @Override
    public void onEnable()
    {
        this.getCore().getUserManager().addDefaultAttachment(RolesAttachment.class, this);

        final Database db = this.getCore().getDB();
        this.dbManager = new AssignedRoleManager(db);
        this.dbUserMeta = new UserMetaDataManager(db);
        this.dbUserPerm = new UserPermissionsManager(db);
        this.roleManager = new RoleManager(this);

        final CommandManager cm = this.getCore().getCommandManager();
        cm.registerCommand(new RoleCommands(this));
        cm.registerCommand(new RoleManagementCommands(this), "roles");
        cm.registerCommands(this, new RoleInformationCommands(this), "roles", "role");
        cm.registerCommand(new UserManagementCommands(this), "roles");
        cm.registerCommands(this, new UserInformationCommands(this), "roles", "user");
        cm.registerCommand(new ManagementCommands(this), "roles");

        this.getCore().getEventManager().registerListener(this, new RolesEventHandler(this));
        //init on FinishedLoadModulesEvent

        this.api = new RolesAPI(this);
    }

    public RolesConfig getConfiguration()
    {
        return this.config;
    }

    public AssignedRoleManager getDbManager()
    {
        return this.dbManager;
    }

    public UserMetaDataManager getDbUserMeta()
    {
        return this.dbUserMeta;
    }

    public UserPermissionsManager getDbUserPerm()
    {
        return this.dbUserPerm;
    }

    public RoleManager getRoleManager()
    {
        return this.roleManager;
    }

    public RolesAPI getApi()
    {
        return this.api;
    }
}
