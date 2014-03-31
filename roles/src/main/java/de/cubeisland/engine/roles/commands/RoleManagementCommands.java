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

import org.bukkit.World;

import de.cubeisland.engine.reflect.codec.converter.Converter;
import de.cubeisland.engine.reflect.exception.ConversionException;
import de.cubeisland.engine.reflect.node.StringNode;
import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.util.formatter.MessageType;
import de.cubeisland.engine.roles.Roles;
import de.cubeisland.engine.roles.config.Priority;
import de.cubeisland.engine.roles.config.PriorityConverter;
import de.cubeisland.engine.roles.exception.CircularRoleDependencyException;
import de.cubeisland.engine.roles.role.DataStore.PermissionValue;
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
        RoleProvider provider = world == null ? this.manager.getGlobalProvider() : this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role == null) return;
        String permission = context.getString(1);
        String setTo = "true";
        if (context.getArgCount() > 2)
        {
            setTo = context.getString(2);
        }
        try
        {
            PermissionValue type = PermissionValue.valueOf(setTo.toUpperCase());
            if (type == PermissionValue.RESET)
            {
                if (global)
                {
                    context.sendTranslated(MessageType.NEUTRAL, "{name#permission} has been reset for the global role {name}!", permission, role.getName());
                }
                else
                {
                    context.sendTranslated(MessageType.NEUTRAL, "{name#permission} has been reset for the role {name} in {world}!", permission, role.getName(), world);
                }
            }
            else if (type == PermissionValue.TRUE)
            {
                if (global)
                {
                    context.sendTranslated(MessageType.POSITIVE, "{name#permission} set to {text:true:color=DARK_GREEN} for the global role {name}!", permission, role.getName());
                }
                else
                {
                    context.sendTranslated(MessageType.POSITIVE, "{name#permission} set to {text:true:color=DARK_GREEN} for the role {name} in {world}!", permission, role.getName(), world);
                }
            }
            else if (type == PermissionValue.FALSE)
            {
                if (global)
                {
                    context.sendTranslated(MessageType.NEGATIVE, "{name#permission} set to {text:false:color=DARK_RED} for the global role {name}!", permission, role.getName());
                }
                else
                {
                    context.sendTranslated(MessageType.NEGATIVE, "{name#permission} set to {text:false:color=DARK_RED} for the role {name} in {world}!", permission, role.getName(), world);
                }
            }
            role.setPermission(permission, type);
            role.save();
        }
        catch (IllegalArgumentException e)
        {
            context.sendTranslated(MessageType.NEGATIVE, "Unknown setting: {input} Use {text:true},{text:false} or {text:reset}!", setTo);
        }
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
        RoleProvider provider = world == null ? this.manager.getGlobalProvider() : this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role == null) return;
        String key = context.getString(1);
        String value = context.getString(2);
        role.setMetadata(key, value);
        role.save();
        if (value == null)
        {
            if (global)
            {
                context.sendTranslated(MessageType.NEUTRAL, "Metadata {input#key} reset for the global role {name}!", key, role.getName());
                return;
            }
            context.sendTranslated(MessageType.NEUTRAL, "Metadata {input#key} reset for the role {name} in {world}!", key, role.getName(), world);
            return;
        }
        if (global)
        {
            context.sendTranslated(MessageType.POSITIVE, "Metadata {input#key} set to {input#value} for the global role {name}!", key, value, role.getName());
            return;
        }
        context.sendTranslated(MessageType.POSITIVE, "Metadata {input#key} set to {input#value} for the role {name} in {world}!", key, value, role.getName(), world);
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
        RoleProvider provider = world == null ? this.manager.getGlobalProvider() : this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role == null) return;
        String key = context.getString(1);
        role.setMetadata(key,null);
        role.save();
        if (global)
        {
            context.sendTranslated(MessageType.NEUTRAL, "Metadata {input#key} reset for the global role {name}!", key, role.getName());
        }
        else
        {
            context.sendTranslated(MessageType.NEUTRAL, "Metadata {input#key} reset for the role {name} in {world}!", key, role.getName(), world);
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
        RoleProvider provider = world == null ? this.manager.getGlobalProvider() : this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role == null) return;
        role.clearMetadata();
        role.save();
        if (global)
        {
            context.sendTranslated(MessageType.NEUTRAL, "Metadata cleared for the global role {name}!", role.getName());
            return;
        }
        context.sendTranslated(MessageType.NEUTRAL, "Metadata cleared for the role {name} in {world}!", role.getName(), world);
    }

    @Alias(names = {"addrparent","manradd"})
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
        RoleProvider provider = world == null ? this.manager.getGlobalProvider() : this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role == null) return;
        Role pRole = provider.getRole(context.getString(1));
        if (pRole == null)
        {
            if (global)
            {
                context.sendTranslated(MessageType.NEUTRAL, "Could not find the global parent role {name}.", context.getString(1));
                return;
            }
            context.sendTranslated(MessageType.NEUTRAL, "Could not find the parent role {name} in {world}.", context.getString(1), world);
            return;
        }
        try
        {
            if (role.assignRole(pRole))
            {
                role.save();
                if (global)
                {
                    if (pRole.isGlobal())
                    {
                        context.sendTranslated(MessageType.NEGATIVE, "{name#role} is a global role and cannot inherit from a non-global role!", role.getName());
                        return;
                    }
                    context.sendTranslated(MessageType.POSITIVE, "Added {name#role} as parent role for the global role {name}!", pRole.getName(), role.getName());
                    return;
                }
                context.sendTranslated(MessageType.POSITIVE, "Added {name#role} as parent role for the role {name} in {world}", pRole.getName(), role.getName(), world);
                return;
            }
            if (global)
            {
                context.sendTranslated(MessageType.NEUTRAL, "{name#role} is already parent role of the global role {name}!", pRole.getName(), role.getName());
                return;
            }
            context.sendTranslated(MessageType.NEUTRAL, "{name#role} is already parent role of the role {name} in {world}!", pRole.getName(), role.getName(), world);
        }
        catch (CircularRoleDependencyException ex)
        {
            context.sendTranslated(MessageType.NEGATIVE, "Circular Dependency! {name#role} depends on the role {name}!", pRole.getName(), role.getName());
        }
    }

    @Alias(names = "remrparent")
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
        RoleProvider provider = world == null ? this.manager.getGlobalProvider() : this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role == null) return;
        Role pRole = provider.getRole(context.getString(1));
        if (pRole == null)
        {
            if (global)
            {
                context.sendTranslated(MessageType.NEUTRAL, "Could not find the global parent role {name}.", context.getString(1));
                return;
            }
            context.sendTranslated(MessageType.NEUTRAL, "Could not find the parent role {name} in {world}.", context.getString(1), world);
            return;
        }
        if (role.removeRole(pRole))
        {
            role.save();
            if (global)
            {
                context.sendTranslated(MessageType.POSITIVE, "Removed the parent role {name} from the global role {name}!", pRole.getName(), role.getName());
                return;
            }
            context.sendTranslated(MessageType.POSITIVE, "Removed the parent role {name} from the role {name} in {world}!", pRole.getName(), role.getName(), world);
            return;
        }
        if (global)
        {
            context.sendTranslated(MessageType.NEUTRAL, "{name#role} is not a parent role of the global role {name}!", pRole.getName(), role.getName());
            return;
        }
        context.sendTranslated(MessageType.NEUTRAL, "{name#role} is not a parent role of the role {name} in {world}!", pRole.getName(), role.getName(), world);
    }

    @Alias(names = "clearrparent")
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
        RoleProvider provider = world == null ? this.manager.getGlobalProvider() : this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        role.clearRoles();
        role.save();
        if (global)
        {
            context.sendTranslated(MessageType.NEUTRAL, "All parent roles of the global role {name} cleared!", role.getName());
            return;
        }
        context.sendTranslated(MessageType.NEUTRAL, "All parent roles of the role {name} in {world} cleared!", role.getName(), world);
    }

    @Alias(names = "setrolepriority")
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
        RoleProvider provider = world == null ? this.manager.getGlobalProvider() : this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role == null) return;
        Converter<Priority> converter = new PriorityConverter();
        Priority priority;
        try
        {
            priority = converter.fromNode(new StringNode(context.getString(1)), null);
            role.setPriorityValue(priority.value);
            role.save();
            if (global)
            {
                context.sendTranslated(MessageType.POSITIVE, "Priority of the global role {name} set to {input#priority}!", role.getName(), context.getString(1));
                return;
            }
            context.sendTranslated(MessageType.POSITIVE, "Priority of the role {name} set to {input#priority} in {world}!", role.getName(), context.getString(1), world);
        }
        catch (ConversionException ex)
        {
            context.sendTranslated(MessageType.NEGATIVE, "{input#priority} is not a valid priority!", context.getString(1));
        }

    }

    @Alias(names = "renamerole")
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
        RoleProvider provider = world == null ? this.manager.getGlobalProvider() : this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role == null) return;
        String newName = context.getString(1);
        String oldName = role.getName();
        if (role.getName().equalsIgnoreCase(newName))
        {
            context.sendTranslated(MessageType.NEGATIVE, "These are the same names!");
            return;
        }
        if (role.rename(newName))
        {
            if (global)
            {
                context.sendTranslated(MessageType.POSITIVE, "Global role {name} renamed to {name#new}!", oldName, newName);
                return;
            }
            context.sendTranslated(MessageType.POSITIVE, "{name#role} renamed to {name#new} in {world}", oldName, newName, world);
            return;
        }
        if (global)
        {
            context.sendTranslated(MessageType.NEGATIVE, "Renaming failed! The role global {name} already exists!", newName);
            return;
        }
        context.sendTranslated(MessageType.NEGATIVE, "Renaming failed! The role {name} already exists in {world}!", newName, world);
    }

    @Alias(names = "createrole")
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
        RoleProvider provider = world == null ? this.manager.getGlobalProvider() : this.manager.getProvider(world);
        if (provider.createRole(roleName) != null)
        {
            if (world == null)
            {
                context.sendTranslated(MessageType.POSITIVE, "Global role {name} created!", roleName);
                return;
            }
            context.sendTranslated(MessageType.POSITIVE, "Role {name} created!", roleName);
            return;
        }
        if (world == null)
        {
            context.sendTranslated(MessageType.NEUTRAL, "There is already a global role named {name}.", roleName);
            return;
        }
        context.sendTranslated(MessageType.NEUTRAL, "There is already a role named {name} in {world}.", roleName, world);
    }

    @Alias(names = "deleteRole")
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
        RoleProvider provider = world == null ? this.manager.getGlobalProvider() : this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role == null) return;
        role.delete();
        this.manager.recalculateAllRoles();
        if (global)
        {
            context.sendTranslated(MessageType.POSITIVE, "Global role {name} deleted!", role.getName());
            return;
        }
        context.sendTranslated(MessageType.POSITIVE, "Deleted the role {name} in {world}!", role.getName(), world);
    }


    @Command(names = {"toggledefault", "toggledef", "toggledefaultrole"},
             desc = "Toggles whether given role is a default role [in world]",
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
            context.sendTranslated(MessageType.POSITIVE, "{name#role} is now a default role in {world}!", role.getName(), world);
            return;
        }
        context.sendTranslated(MessageType.POSITIVE, "{name#role} is no longer a default role in {world}!", role.getName(), world);
    }
}
