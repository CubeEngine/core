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
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.roles.commands.ManagementCommands;
import de.cubeisland.cubeengine.roles.commands.RoleCommands;
import de.cubeisland.cubeengine.roles.commands.RoleInformationCommands;
import de.cubeisland.cubeengine.roles.commands.RoleManagementCommands;
import de.cubeisland.cubeengine.roles.commands.UserInformationCommands;
import de.cubeisland.cubeengine.roles.commands.UserManagementCommands;
import de.cubeisland.cubeengine.roles.config.PermissionTree;
import de.cubeisland.cubeengine.roles.config.PermissionTreeConverter;
import de.cubeisland.cubeengine.roles.config.Priority;
import de.cubeisland.cubeengine.roles.config.PriorityConverter;
import de.cubeisland.cubeengine.roles.config.RoleMirror;
import de.cubeisland.cubeengine.roles.config.RoleMirrorConverter;
import de.cubeisland.cubeengine.roles.role.RolesAttachment;
import de.cubeisland.cubeengine.roles.role.RolesEventHandler;
import de.cubeisland.cubeengine.roles.role.RolesManager;

public class Roles extends Module
{
    private RolesConfig config;
    private RolesManager rolesManager;

    public Roles()
    {
        Convert.registerConverter(PermissionTree.class, new PermissionTreeConverter(this));
        Convert.registerConverter(Priority.class, new PriorityConverter());
        Convert.registerConverter(RoleMirror.class, new RoleMirrorConverter(this));
    }

    @Override
    public void onEnable()
    {
        this.rolesManager = new RolesManager(this);

        this.getCore().getUserManager().addDefaultAttachment(RolesAttachment.class, this);

        final CommandManager cm = this.getCore().getCommandManager();
        cm.registerCommand(new RoleCommands(this));
        cm.registerCommand(new RoleManagementCommands(this), "roles");
        cm.registerCommands(this, new RoleInformationCommands(this), "roles", "role");
        cm.registerCommand(new UserManagementCommands(this), "roles");
        cm.registerCommands(this, new UserInformationCommands(this), "roles", "user");
        cm.registerCommand(new ManagementCommands(this), "roles");

        this.getCore().getEventManager().registerListener(this, new RolesEventHandler(this));

        Module basicsModule = this.getCore().getModuleManager().getModule("basics");
        if (basicsModule != null)
        {
            this.getCore().getEventManager().registerListener(this, new BasicsOnlinePlayerList(this));
        }
    }

    @Override
    public void onStartupFinished()
    {
        this.config = Configuration.load(RolesConfig.class, this);
        this.rolesManager.initRoleProviders();
        this.rolesManager.recalculateAllRoles();
    }

    @Override
    public void onDisable()
    {
        this.getCore().getEventManager().removeListeners(this);
    }

    public RolesConfig getConfiguration()
    {
        return this.config;
    }

    public RolesManager getRolesManager()
    {
        return this.rolesManager;
    }
}
