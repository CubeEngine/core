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

import java.io.IOException;

import org.bukkit.World;

import de.cubeisland.engine.configuration.convert.Converter;
import de.cubeisland.engine.configuration.exception.ConversionException;
import de.cubeisland.engine.configuration.node.StringNode;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.roles.Roles;
import de.cubeisland.engine.roles.config.Priority;
import de.cubeisland.engine.roles.config.PriorityConverter;
import de.cubeisland.engine.roles.exception.CircularRoleDependencyException;
import de.cubeisland.engine.roles.role.Role;
import de.cubeisland.engine.roles.role.RoleProvider;
import de.cubeisland.engine.roles.role.WorldRoleProvider;

public class RoleManagementCommands extends RoleCommandHelper
{
    public RoleManagementCommands(Roles module)
    {
        super(module);
        this.registerAlias(new String[]{"manrole"},new String[]{});
    }

    @Alias(names = "setrperm")
    @Command(names = {"setperm", "setpermission"},
             desc = "Sets the permission for given role [in world]",
             usage = "<[g:]role> <permission> [true|false|reset] [in <world>]",
             params = @Param(names = "in", type = World.class),
             max = 3, min = 2)
    public void setpermission(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        if (!global && world == null) return;
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role == null) return;
        String permission = context.getString(1);
        Boolean set;
        String setTo = "true";
        if (context.getArgCount() > 2)
        {
            setTo = context.getString(2);
        }
        if (setTo.equalsIgnoreCase("true"))
        {
            set = true;
            if (global)
            {
                context.sendTranslated("&6%s&a is now set to &2true &afor the global role &6%s&a!",
                                       permission, role.getName());
            }
            else
            {
                context.sendTranslated("&6%s&a is now set to &2true &afor the role &6%s&a in &6%s&a!",
                                       permission, role.getName(), world.getName());
            }
        }
        else if (setTo.equalsIgnoreCase("false"))
        {
            set = false;
            if (global)
            {
                context.sendTranslated("&6%s&c is now set to &4false &cfor the global role &6%s&c!",
                                       permission, role.getName());
            }
            else
            {
                context.sendTranslated("&6%s&c is now set to &4false &cfor the role &6%s &cin &6%s&c!",
                                       permission, role.getName(), world.getName());
            }
        }
        else if (setTo.equalsIgnoreCase("reset"))
        {
            set = null;
            if (global)
            {
                context.sendTranslated("&6%s&e is now resetted for the global role &6%s&e!",
                                       permission, role.getName());
            }
            else
            {
                context.sendTranslated("&6%s&e is now resetted for the role &6%s &ein &6%s&e!",
                                       permission, role.getName(), world.getName());
            }
        }
        else
        {
            context.sendTranslated("&cUnkown setting: &6%s &cUse &6true&c,&6false&c or &6reset&c!", setTo);
            return;
        }
        role.setPermission(permission, set);
        role.saveToConfig();
        this.manager.recalculateAllRoles();
    }

    @Alias(names = "setrdata")
    @Command(names = {"setdata", "setmeta", "setmetadata"},
             desc = "Sets the metadata for given role [in world]",
             usage = "<[g:]role> <key> [value] [in <world>]",
             params = @Param(names = "in", type = World.class),
             max = 3, min = 2)
    public void setmetadata(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        if (!global && world == null) return;
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role == null) return;
        String key = context.getString(1);
        String value = context.getString(2);
        role.setMetadata(key, value);
        role.saveToConfig();
        this.manager.recalculateAllRoles();
        if (value == null)
        {
            if (global)
            {
                context.sendTranslated("&eMetadata &6%s&e resetted for the global role &6%s&e!", key, role.getName());
                return;
            }
            context.sendTranslated("&eMetadata &6%s&e resetted for the role &6%s&e in &6%s&e!",
                                   key, role.getName(), world.getName());
            return;
        }
        if (global)
        {
            context.sendTranslated("&aMetadata &6%s&a set to &6%s &afor the global role &6%s&a!",
                                   key, value, role.getName());
            return;
        }
        context.sendTranslated("&aMetadata &6%s&a set to &6%s &afor the role &6%s&a in &6%s&a!",
                                   key, value, role.getName(), world.getName());
    }

    @Alias(names = "resetrdata")
    @Command(names = {"resetdata", "resetmeta", "resetmetadata"},
             desc = "Resets the metadata for given role [in world]",
             usage = "<[g:]role> <key> [in <world>]",
             params = @Param(names = "in", type = World.class),
             max = 2, min = 2)
    public void resetmetadata(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        if (!global && world == null) return;
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role == null) return;
        String key = context.getString(1);
        role.setMetadata(key,null);
        role.saveToConfig();
        this.manager.recalculateAllRoles();
        if (global)
        {
            context.sendTranslated("&eMetadata &6%s &eresetted for the global role &6%s&e!",
                                   key, role.getName());
        }
        else
        {
            context.sendTranslated("&eMetadata &6%s &eresetted for the role &6%s &ein &6%s&e!",
                                   key, role.getName(), world.getName());
        }
    }

    @Alias(names = "clearrdata")
    @Command(names = {"cleardata", "clearmeta", "clearmetadata"},
             desc = "Clears the metadata for given role [in world]",
             usage = "<[g:]role> [in <world>]",
             params = @Param(names = "in", type = World.class),
             max = 1, min = 1)
    public void clearmetadata(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        if (!global && world == null) return;
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role == null) return;
        role.clearMetadata();
        role.saveToConfig();
        this.manager.recalculateAllRoles();
        if (global)
        {
            context.sendTranslated("&eMetadata cleared for the global role &6%s&e!", role.getName());
            return;
        }
        context.sendTranslated("&eMetadata cleared for the role &6%s&e in &6%s&e!",
                               role.getName(), world.getName());
    }

    @Command(desc = "Adds a parent role to given role [in world]",
             usage = "<[g:]role> <[g:]parentrole> [in <world>]",
             params = @Param(names = "in", type = World.class),
             max = 2, min = 2)
    public void addParent(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        if (!global && world == null) return;
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role == null) return;
        Role pRole = provider.getRole(context.getString(1));
        if (pRole == null)
        {
            if (global)
            {
                context.sendTranslated("&eCould not find the global parent-role &6%s&e.", context.getString(1));
                return;
            }
            context.sendTranslated("&eCould not find the parent-role &6%s &ein &6%s&e.", context.getString(1), world.getName());
            return;
        }
        try
        {
            if (role.assignRole(pRole))
            {
                role.saveToConfig();
                this.manager.recalculateAllRoles();
                if (global)
                {
                    if (pRole.isGlobal())
                    {
                        context.sendTranslated("&&%s&c is a global role and cannot inherit from a non-global role!", role.getName());
                        return;
                    }
                    context.sendTranslated("&aAdded &6%s&a as parent-role for the global role &6%s&a!", pRole.getName(), role.getName());
                    return;
                }
                context.sendTranslated("&aAdded &6%s&a as parent-role for the role &6%s&a in &6%s&a!",
                               pRole.getName(), role.getName(), world.getName());
                return;
            }
            if (global)
            {
                context.sendTranslated("&6%s&e is already parent-role of the global role &6%s&e!", pRole.getName(), role.getName());
                return;
            }
            context.sendTranslated("&6%s&e is already parent-role of the role &6%s&a in &6%s&e!",
                               pRole.getName(), role.getName(), world.getName());
        }
        catch (CircularRoleDependencyException ex)
        {
            context.sendTranslated("&cCircular Dependency! &6%s &cdepends on &6%s&c!", pRole.getName(), role.getName());
        }
    }

    @Command(desc = "Removes a parent role from given role [in world]",
             usage = "<[g:]role> <[g:]parentrole> [in <world>]",
             params = @Param(names = "in", type = World.class),
             max = 2, min = 2)
    public void removeParent(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        if (!global && world == null) return;
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role == null) return;
        Role pRole = provider.getRole(context.getString(1));
        if (pRole == null)
        {
            if (global)
            {
                context.sendTranslated("&eCould not find the global parent-role &6%s&e.", context.getString(1));
                return;
            }
            context.sendTranslated("&eCould not find the parent-role &6%s&e in &6%s&e.", context.getString(1), world.getName());
            return;
        }
        if (role.removeRole(pRole))
        {
            role.saveToConfig();
            this.manager.recalculateAllRoles();
            if (global)
            {
                context.sendTranslated("&aRemoved the parent-role &6%s&a from the global role &6%s&a!",
                                       pRole.getName(), role.getName());
                return;
            }
            context.sendTranslated("&aRemoved the parent-role &6%s&a from the role &6%s&a in &6%s&a!",
                           pRole.getName(), role.getName(), world.getName());
            return;
        }
        if (global)
        {
            context.sendTranslated("&6%s&e is not a parent-role of the global role &6%s&e!", pRole.getName(), role.getName());
            return;
        }
        context.sendTranslated("&6%s&e is not a parent-role of the role &6%s &ein &6%s&e!",
                               pRole.getName(), role.getName(), world.getName());
    }

    @Command(desc = "Removes all parent roles from given role [in world]",
             usage = "<[g:]role> [in <world>]",
             params = @Param(names = "in", type = World.class),
             max = 1, min = 1)
    public void clearParent(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        if (!global && world == null) return;
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        role.clearAssignedRoles();
        role.saveToConfig();
        this.manager.recalculateAllRoles();
        if (global)
        {
            context.sendTranslated("&eAll parent-roles of the global role &6%s &ecleared!", role.getName());
            return;
        }
        context.sendTranslated("&eAll parent-roles of the role &6%s &ein &6%s cleared!",  role.getName(), world.getName());
    }

    @Command(names = {"setprio", "setpriority"},
             desc = "Sets the priority of given role [in world]",
             usage = "<[g:]role> <priority> [in <world>]",
             params = @Param(names = "in", type = World.class),
             max = 2, min = 2)
    public void setPriority(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        if (!global && world == null) return;
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role == null) return;
        Converter<Priority> converter = new PriorityConverter();
        Priority priority;
        try
        {
            priority = converter.fromNode(new StringNode(context.getString(1)), null);
            role.setPriorityValue(priority.value);
            role.saveToConfig();
            this.manager.recalculateAllRoles();
            if (global)
            {
                context.sendTranslated("&aPriority of the global role &6%s&a set to &6%s&a!",
                                       role.getName(), context.getString(1));
                return;
            }
            context.sendTranslated("&aPriority of the role &6%s&a set to &6%s&a in &6%s&a!",
                                   role.getName(), context.getString(1), world.getName());
        }
        catch (ConversionException ex)
        {
            context.sendTranslated("&6%s&c is not a valid priority!", context.getString(1));
        }

    }

    @Command(desc = "Renames given role [in world]",
             usage = "<[g:]role> <new name> [in <world>]|[-global]",
             params = @Param(names = "in", type = World.class),
             max = 2, min = 2)
    public void rename(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        if (!global && world == null) return;
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role == null) return;
        String newName = context.getString(1);
        String oldName = role.getName();
        if (role.getName().equalsIgnoreCase(newName))
        {
            context.sendTranslated("&cThese are the same names!");
            return;
        }
        if (role.rename(newName))
        {
            this.manager.recalculateAllRoles();
            if (global)
            {
                context.sendTranslated("&aGlobal role &6%s &arenamed to &6%s&a!", oldName, newName);
                return;
            }
            context.sendTranslated("&6%s &arenamed to &6%s&a in &6%s&a!", oldName, newName, world.getName());
            return;
        }
        if (global)
        {
            context.sendTranslated("&cRenaming failed! The role global &6%s &calready exists!", newName);
            return;
        }
        context.sendTranslated("&cRenaming failed! The role &6%s &calready exists in &6%s&c!", newName, world.getName());
    }

    @Command(desc = "Creates a new role [in world]",
             usage = "<rolename> [in <world>]|[-global]",
             params = @Param(names = "in", type = World.class),
             flags = @Flag(longName = "global", name = "g"),
             max = 1, min = 1)
    public void create(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        boolean global = context.hasFlag("g");
        World world = global ? null : this.getWorld(context);
        if (!global && world == null) return;
        RoleProvider provider = this.manager.getProvider(world);
        if (provider.createRole(roleName) != null)
        {
            this.manager.recalculateAllRoles();
            if (world == null)
            {
                context.sendTranslated("&aGlobal role &6%s&a created!", roleName);
                return;
            }
            context.sendTranslated("&aRole &6%s&a created!", roleName);
            return;
        }
        if (world == null)
        {
            context.sendTranslated("&eThere is already a global role named &6%s&e.", roleName, world);
            return;
        }
        context.sendTranslated("&eThere is already a role named &6%s&e in &6%s&e.", roleName, world.getName());
    }

    @Command(desc = "Deletes a role [in world]",
             usage = "<[g:]rolename> [in <world>]",
             params = @Param(names = "in", type = World.class),
             max = 1, min = 1)
    public void delete(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        if (!global && world == null) return;
        RoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role == null) return;
        this.manager.recalculateAllRoles();
        try
        {
            role.deleteRole();
            if (global)
            {
                context.sendTranslated("&aGlobal role &6%s &adeleted!", role.getName());
                return;
            }
            context.sendTranslated("&aDeleted the role &6%s&a in &6%s&a!", role.getName(), world.getName());
        }
        catch (IOException ex)
        {
            context.getCommand().getModule().getLog().warn(ex, "Failed to delete the role configuration for {}!", role.getName());
            context.sendTranslated("&eDeleted the role, however its configuration file could not be removed.");
        }
    }


    @Command(names = {"toggledefault", "toggledef", "toggledefaultrole"},
             desc = "Toggles whether given role is a default-role [in world]",
             usage = "<rolename> [in <world>]",
             params = @Param(names = "in", type = World.class),
             max = 1, min = 1)
    public void toggleDefaultRole(ParameterizedContext context)
    {
        String roleName = context.getString(0);
        World world = this.getWorld(context);
        if (world == null) return;
        WorldRoleProvider provider = this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role == null) return;
        role.setDefaultRole(!role.isDefaultRole());
        this.manager.recalculateAllRoles();
        if (role.isDefaultRole())
        {
            context.sendTranslated("&6%s&a is now a default-role in &6%s&a!", role.getName(), world.getName());
            return;
        }
        context.sendTranslated("&6%s&a is no longer a default-role in &6%s&a!", role.getName(), world.getName());
    }
}
