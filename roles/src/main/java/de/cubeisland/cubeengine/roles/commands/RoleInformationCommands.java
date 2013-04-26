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
package de.cubeisland.cubeengine.roles.commands;

import java.util.Set;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.command.parameterized.Flag;
import de.cubeisland.cubeengine.core.command.parameterized.Param;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Alias;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.util.ChatFormat;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.Role;
import de.cubeisland.cubeengine.roles.role.RoleProvider;
import de.cubeisland.cubeengine.roles.role.WorldRoleProvider;
import de.cubeisland.cubeengine.roles.role.resolved.ResolvedMetadata;
import de.cubeisland.cubeengine.roles.role.resolved.ResolvedPermission;

public class RoleInformationCommands extends RoleCommandHelper
{
    public RoleInformationCommands(Roles module)
    {
        super(module);
    }

    @Alias(names = "listroles")
    @Command(desc = "Lists all roles [in world]|[-global]", usage = "[in <world>]", params = @Param(names = "in", type = World.class), flags = @Flag(longName = "global", name = "g"), max = 1)
    public void list(ParameterizedContext context)
    {
        boolean global = context.hasFlag("g");
        World world = global ? null : this.getWorld(context);
        RoleProvider provider = this.manager.getProvider(world);
        if (provider.getRoles().isEmpty())
        {
            if (global)
            {
                context.sendTranslated("&eNo global roles found!");
            }
            else
            {
                context.sendTranslated("&eNo roles found in &6%s&e!", world.getName());
            }
        }
        else
        {
            if (global)
            {
                context.sendTranslated("&aThe following global roles are available:");
            }
            else
            {
                context.sendTranslated("&aThe following roles are available in &6%s&a:", world.getName());
            }
            for (Role role : provider.getRoles())
            {
                context.sendMessage(String.format(this.LISTELEM,role.getName()));
            }
        }
    }

    @Alias(names = "checkrperm")
    @Command(names = {
        "checkperm", "checkpermission"
    }, desc = "Checks the permission in given role [in world]", usage = "<[g:]role> <permission> [in <world>]", params = @Param(names = "in", type = World.class), max = 3, min = 2)
    public void checkperm(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        String permission = context.getString(1);
        ResolvedPermission myPerm = role.getPermissions().get(permission);
        if (myPerm != null)
        {
            if (myPerm.isSet())
            {
                if (global)
                {
                    context.sendTranslated("&6%s &ais set to &2true &afor the global role &6%s&a.",
                                           context.getString(1), role.getName());
                }
                else
                {
                    context.sendTranslated("&6%s &ais set to &2true &afor the role &6%s &ain &6%s&a.",
                                           context.getString(1), role.getName(), world.getName());
                }
            }
            else
            {
                if (global)
                {
                    context.sendTranslated("&6%s &cis set to &4false &cfor the global role &6%s&c.",
                                           context.getString(1), role.getName());
                }
                else
                {
                    context.sendTranslated("&6%s &cis set to &4false &cfor the role &6%s &cin &6%s&c.",
                                           context.getString(1), role.getName(), world.getName());
                }
            }
        }
        else
        {
            if (global)
            {
                context.sendTranslated("&eThe permission &6%s &eis not assigned in the global role &6%s&e.",
                                       context.getString(1), role.getName());
            }
            else
            {
                context.sendTranslated("&eThe permission &6%s &eis not assigned in the role &6%s &ein &6%s&e.",
                                       context.getString(1), role.getName(), world.getName());
            }
            return;
        }
        Role originRole = (Role)myPerm.getOrigin();
        if (!originRole.getRawPermissions().containsKey(permission))
        {
            boolean found = false;
            while (!permission.equals("*"))
            {
                if (permission.endsWith("*"))
                {
                    permission = permission.substring(0, permission.lastIndexOf("."));
                }
                permission = permission.substring(0, permission.lastIndexOf(".") + 1) + "*";

                if (originRole.getRawPermissions().containsKey(permission))
                {
                    if (originRole.getRawPermissions().get(permission) == myPerm.isSet())
                    {
                        found = true;
                        break;
                    }
                }
            }
            if (!found)
            {
                throw new IllegalStateException("Found permission not found in literal permissions");
            }
        }
        context.sendTranslated("&ePermission inherited from:");
        context.sendTranslated("&6%s &ein the role &6%s&e!", permission, originRole.getName());
    }

    @Alias(names = "listrperm")
    @Command(names = {
        "listperm", "listpermission"
    }, desc = "Lists all permissions of given role [in world]", usage = "<[g:]role> [in <world>]", params = @Param(names = "in", type = World.class), max = 2, min = 1)
    public void listperm(ParameterizedContext context)
    {
        // TODO list ALL perm OR only list perm of this role
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role.getRawPermissions().isEmpty())
        {
            if (global)
            {
                context.sendTranslated("&eNo permissions set for the global role &6%s&e.", role.getName());
            }
            else
            {
                context.sendTranslated("&eNo permissions set for the role &6%s&e in &6%s&e.", role.getName(), world.getName());
            }
        }
        else
        {
            if (global)
            {
                context.sendTranslated("&aPermissions of the global role &6%s&a.", role.getName());
            }
            else
            {
                context.sendTranslated("&aPermissions of the role &6%s &ain &6%s&a:", role.getName(), world.getName());
            }
            for (String perm : role.getRawPermissions().keySet())
            {
                String trueString = ChatFormat.parseFormats("&2true");
                String falseString = ChatFormat.parseFormats("&4false");
                if (role.getRawPermissions().get(perm))
                {
                    context.sendMessage(String.format(this.LISTELEM_VALUE,perm,trueString));
                }
                else
                {
                    context.sendMessage(String.format(this.LISTELEM_VALUE,perm,falseString));
                }
            }
        }
    }

    @Alias(names = "listrdata")
    @Command(names = {
        "listdata", "listmeta", "listmetadata"
    }, desc = "Lists all metadata of given role [in world]", usage = "<[g:]role> [in <world>]", params = @Param(names = "in", type = World.class), max = 2, min = 1)
    public void listmetadata(ParameterizedContext context)
    {
        // TODO list ALL metadata OR only list metadata of this role
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role.getRawMetadata().isEmpty())
        {
            if (global)
            {
                context.sendTranslated("&eNo metadata set for the global role &6%s&e.", role.getName());
            }
            else
            {
                context.sendTranslated("&eNo metadata set for the role &6%s &ein &6%s&e.", role.getName(), world.getName());
            }
        }
        else
        {
            if (global)
            {
                context.sendTranslated("&aMetadata of the global role &6%s&a:", role.getName());
            }
            else
            {
                context.sendTranslated("&aMetadata of the role &6%s &ain &6%s&a:", role.getName(), world.getName());
            }
            for (ResolvedMetadata data : role.getMetadata().values())
            {
                context.sendMessage(String.format(this.LISTELEM_VALUE,data.getKey(), data.getValue()));
            }
        }
    }

    @Command(desc = "Lists all parents of given role [in world]", usage = "<[g:]role> [in <world>]", params = @Param(names = "in", type = World.class), max = 2, min = 1)
    public void listParent(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role.getAssignedRoles().isEmpty())
        {
            if (global)
            {
                context.sendTranslated("&eThe global role &6%s &ehas no parent roles.", role.getName());
            }
            else
            {
                context.sendTranslated("&eThe role &6%s &ein &6%s &ehas no parent roles.", role.getName(), world.getName());
            }
        }
        else
        {
            if (global)
            {
                context.sendTranslated("&eThe global role &6%s &ehas following parent roles:", role.getName());
            }
            else
            {
                context.sendTranslated("&eThe role &6%s &ein &6%s &ehas following parent roles:", role.getName(), world.getName());
            }
            for (Role parent : role.getAssignedRoles())
            {
                context.sendMessage(String.format(this.LISTELEM,parent.getName()));
            }
        }
    }

    @Command(names = {
        "prio", "priotory"
    }, desc = "Show the priority of given role [in world]", usage = "<[g:]role> [in <world>]", params = @Param(names = "in", type = World.class), max = 2, min = 1)
    public void priority(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        World world = roleName.startsWith(GLOBAL_PREFIX) ? null : this.getWorld(context);
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (world == null)
        {
            context.sendTranslated("&eThe priority of the global role &6%s &eis: &6%d", role.getName(), role.getPriorityValue());
        }
        else
        {
            context.sendTranslated("&eThe priority of the role &6%s &ein &6%s &eis: &6%d", role.getName(), world.getName(), role.getPriorityValue());
        }
    }

    @Command(names = {
        "listdefault", "listdefroles", "listdefaultroles"
    }, desc = "Lists all default roles [in world]", usage = "[in <world>]", params = @Param(names = "in", type = World.class), max = 1)
    public void listDefaultRoles(ParameterizedContext context)
    {
        World world = this.getWorld(context);
        WorldRoleProvider provider = this.manager.getProvider(world);
        Set<Role> defaultRoles = provider.getDefaultRoles();
        if (defaultRoles.isEmpty())
        {
            context.sendTranslated("&cThere are no default roles set for &6%s&c!", world.getName());
        }
        else
        {
            context.sendTranslated("&aThe following roles are default roles in &6%s&a!", world.getName());
            for (Role role : defaultRoles)
            {
                context.sendMessage(String.format(this.LISTELEM,role.getName()));
            }
        }
    }
}
