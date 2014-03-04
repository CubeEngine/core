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
package de.cubeisland.engine.roles.commands;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.World;

import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.roles.Roles;
import de.cubeisland.engine.roles.role.Role;
import de.cubeisland.engine.roles.role.RoleProvider;
import de.cubeisland.engine.roles.role.WorldRoleProvider;
import de.cubeisland.engine.roles.role.resolved.ResolvedPermission;

public class RoleInformationCommands extends RoleCommandHelper
{
    public RoleInformationCommands(Roles module)
    {
        super(module);
    }

    @Alias(names = "listroles")
    @Command(desc = "Lists all roles in a world or globally",
             usage = "[in <world>]|[-global]",
             params = @Param(names = "in", type = World.class),
             flags = @Flag(longName = "global", name = "g"))
    public void list(ParameterizedContext context)
    {
        boolean global = context.hasFlag("g");
        World world = global ? null : this.getWorld(context);
        if (!global && world == null) return;
        RoleProvider provider = world == null ? this.manager.getGlobalProvider() : this.manager.getProvider(world);
        if (provider.getRoles().isEmpty())
        {
            if (global)
            {
                context.sendTranslated("&cThere are no global roles!");
                return;
            }
            context.sendTranslated("&cThere are no roles in &6%s&c!", world.getName());
            return;
        }
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
            context.sendMessage(String.format(this.LISTELEM, role.getName()));
        }
    }

    @Alias(names = "checkrperm")
    @Command(names = {"checkperm", "checkpermission"},
             desc = "Checks the permission in given role [in world]",
             usage = "<[g:]role> <permission> [in <world>]",
             params = @Param(names = "in", type = World.class),
             max = 2, min = 2)
    public void checkperm(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        if (!global && world == null) return;
        RoleProvider provider = world == null ? this.manager.getGlobalProvider() : this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role == null) return;
        String permission = context.getString(1);
        ResolvedPermission myPerm = role.getPermissions().get(permission);
        if (myPerm != null)
        {
            if (myPerm.isSet())
            {
                if (global)
                {
                    context.sendTranslated("&6%s&a is set to &2true&a for the global role &6%s&a.",
                                           permission, role.getName());
                }
                else
                {
                    context.sendTranslated("&6%s&a is set to &2true&a for the role &6%s&a in &6%s&a.",
                                           permission, role.getName(), world.getName());
                }
            }
            else
            {
                if (global)
                {
                    context.sendTranslated("&6%s&c is set to &4false&c for the global role &6%s&c.",
                                           permission, role.getName());
                }
                else
                {
                    context.sendTranslated("&6%s&c is set to &4false&c for the role &6%s&c in &6%s&c.",
                                           permission, role.getName(), world.getName());
                }
            }
            if (!(myPerm.getOriginPermission() == null && myPerm.getOrigin() == role))
            {
                context.sendTranslated("&ePermission inherited from:");
                if (myPerm.getOriginPermission() == null)
                {
                    context.sendTranslated("&6%s&e in the role &6%s&e!", myPerm.getKey(), myPerm.getOrigin().getName());
                }
                else
                {
                    context.sendTranslated("&6%s&e in the role &6%s&e!", myPerm.getOriginPermission(), myPerm.getOrigin().getName());
                }
            }
            return;
        }
        if (global)
        {
            context.sendTranslated("&eThe permission &6%s&e is not assigned to the global role &6%s&e.",
                                   permission, role.getName());
            return;
        }
        context.sendTranslated("&eThe permission &6%s&e is not assigned to the role &6%s&e in &6%s&e.",
                               permission, role.getName(), world.getName());
    }

    @Alias(names = "listrperm")
    @Command(names = {"listperm", "listpermission"},
             desc = "Lists all permissions of given role [in world]",
             usage = "<[g:]role> [in <world>]",
             params = @Param(names = "in", type = World.class),
             flags = @Flag(longName = "all", name = "a"),
             max = 1, min = 1)
    public void listperm(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        if (!global && world == null) return;
        RoleProvider provider = world == null ? this.manager.getGlobalProvider() : this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role == null) return;
        Map<String,Boolean> rawPerms = context.hasFlag("a") ? role.getAllRawPermissions() : role.getRawPermissions();
        if (rawPerms.isEmpty())
        {
            if (global)
            {
                context.sendTranslated("&eNo permissions set for the global role &6%s&e.", role.getName());
                return;
            }
            context.sendTranslated("&eNo permissions set for the role &6%s&e in &6%s&e.", role.getName(), world.getName());
            return;
        }
        if (global)
        {
            context.sendTranslated("&aPermissions of the global role &6%s&a.", role.getName());
        }
        else
        {
            context.sendTranslated("&aPermissions of the role &6%s&a in &6%s&a:", role.getName(), world.getName());
        }
        if (context.hasFlag("a"))
        {
            context.sendTranslated("&a(Including inherited permissions)");
        }
        for (String perm : rawPerms.keySet())
        {
            String trueString = ChatFormat.parseFormats("&2true");
            String falseString = ChatFormat.parseFormats("&4false");
            if (role.getRawPermissions().get(perm))
            {
                context.sendMessage(String.format(this.LISTELEM_VALUE,perm,trueString));
                continue;
            }
            context.sendMessage(String.format(this.LISTELEM_VALUE,perm,falseString));
        }
    }

    @Alias(names = "listrdata")
    @Command(names = {"listdata", "listmeta", "listmetadata"},
             desc = "Lists all metadata of given role [in world]",
             usage = "<[g:]role> [in <world>]",
             params = @Param(names = "in", type = World.class),
             flags = @Flag(longName = "all", name = "a"),
             max = 2, min = 1)
    public void listmetadata(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        if (!global && world == null) return;
        RoleProvider provider = world == null ? this.manager.getGlobalProvider() : this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role == null) return;
        Map<String, String> rawMetadata = context.hasFlag("a") ? role.getAllRawMetadata() : role.getRawMetadata();
        if (rawMetadata.isEmpty())
        {
            if (global)
            {
                context.sendTranslated("&eNo metadata set for the global role &6%s&e.", role.getName());
                return;
            }
            context.sendTranslated("&eNo metadata set for the role &6%s&e in &6%s&e.", role.getName(), world.getName());
            return;
        }
        if (global)
        {
            context.sendTranslated("&aMetadata of the global role &6%s&a:", role.getName());
        }
        else
        {
            context.sendTranslated("&aMetadata of the role &6%s&a in &6%s&a:", role.getName(), world.getName());
        }
        if (context.hasFlag("a"))
        {
            context.sendTranslated("&a(Including inherited metadata)");
        }
        for (Entry<String, String> data : rawMetadata.entrySet())
        {
            context.sendMessage(String.format(this.LISTELEM_VALUE,data.getKey(), data.getValue()));
        }
    }

    @Alias(names = "listrparent")
    @Command(desc = "Lists all parents of given role [in world]",
             usage = "<[g:]role> [in <world>]",
             params = @Param(names = "in", type = World.class),
             max = 1, min = 1)
    public void listParent(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        if (!global && world == null) return;
        RoleProvider provider = world == null ? this.manager.getGlobalProvider() : this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role == null) return;
        if (role.getRoles().isEmpty())
        {
            if (global)
            {
                context.sendTranslated("&eThe global role &6%s&e has no parent roles.", role.getName());
                return;
            }
            context.sendTranslated("&eThe role &6%s&e in &6%s&e has no parent roles.", role.getName(), world.getName());
            return;
        }
        if (global)
        {
            context.sendTranslated("&eThe global role &6%s &ehas following parent roles:", role.getName());
        }
        else
        {
            context.sendTranslated("&eThe role &6%s&e in &6%s &ehas following parent roles:", role.getName(), world.getName());
        }
        for (Role parent : role.getRoles())
        {
            context.sendMessage(String.format(this.LISTELEM,parent.getName()));
        }
    }

    @Command(names = {"prio", "priority"},
             desc = "Show the priority of given role [in world]",
             usage = "<[g:]role> [in <world>]",
             params = @Param(names = "in", type = World.class),
             max = 1, min = 1)
    public void priority(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        if (!global && world == null) return;
        RoleProvider provider = world == null ? this.manager.getGlobalProvider() : this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role == null) return;
        if (global)
        {
            context.sendTranslated("&eThe priority of the global role &6%s&e is: &6%d", role.getName(), role.getPriorityValue());
            return;
        }
        context.sendTranslated("&eThe priority of the role &6%s&e in &6%s&e is: &6%d", role.getName(), world.getName(), role.getPriorityValue());
    }

    @Command(names = {"default","defaultroles","listdefroles", "listdefaultroles"},
             desc = "Lists all default roles [in world]",
             usage = "[in <world>]",
             params = @Param(names = "in", type = World.class))
    public void listDefaultRoles(ParameterizedContext context)
    {
        World world = this.getWorld(context);
        if (world == null) return;
        WorldRoleProvider provider = this.manager.getProvider(world);
        Set<Role> defaultRoles = provider.getDefaultRoles();
        if (defaultRoles.isEmpty())
        {
            context.sendTranslated("&cThere are no default roles set for &6%s&c!", world.getName());
            return;
        }
        context.sendTranslated("&aThe following roles are default roles in &6%s&a!", world.getName());
        for (Role role : defaultRoles)
        {
            context.sendMessage(String.format(this.LISTELEM,role.getName()));
        }
    }
}
