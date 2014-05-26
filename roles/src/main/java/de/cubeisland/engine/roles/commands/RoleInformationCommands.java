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

import de.cubeisland.engine.core.command.reflected.context.Flag;
import de.cubeisland.engine.core.command.reflected.context.Flags;
import de.cubeisland.engine.core.command.reflected.context.IParams;
import de.cubeisland.engine.core.command.reflected.context.NParams;
import de.cubeisland.engine.core.command.reflected.context.Named;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.command.reflected.context.Grouped;
import de.cubeisland.engine.core.command.reflected.context.Indexed;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.roles.Roles;
import de.cubeisland.engine.roles.role.Role;
import de.cubeisland.engine.roles.role.RoleProvider;
import de.cubeisland.engine.roles.role.WorldRoleProvider;
import de.cubeisland.engine.roles.role.resolved.ResolvedPermission;

import static de.cubeisland.engine.core.util.formatter.MessageType.*;

public class RoleInformationCommands extends RoleCommandHelper
{
    public RoleInformationCommands(Roles module)
    {
        super(module);
    }

    @Alias(names = "listroles")
    @Command(desc = "Lists all roles in a world or globally")
    @NParams(@Named(names = "in", label = "world", type = World.class))
    @Flags(@Flag(longName = "global", name = "g"))
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
                context.sendTranslated(NEGATIVE, "There are no global roles!");
                return;
            }
            context.sendTranslated(NEGATIVE, "There are no roles in {world}!", world);
            return;
        }
        if (global)
        {
            context.sendTranslated(POSITIVE, "The following global roles are available:");
        }
        else
        {
            context.sendTranslated(POSITIVE, "The following roles are available in {world}:", world);
        }
        for (Role role : provider.getRoles())
        {
            context.sendMessage(String.format(this.LISTELEM, role.getName()));
        }
    }

    @Alias(names = "checkrperm")
    @Command(alias = "checkpermission", desc = "Checks the permission in given role [in world]")
    @IParams({@Grouped(@Indexed(label = "[g:]role")),
              @Grouped(@Indexed(label = "permission"))})
    @NParams(@Named(names = "in", label = "world", type = World.class))
    public void checkperm(ParameterizedContext context)
    {
        String roleName = context.getArg(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        if (!global && world == null) return;
        RoleProvider provider = world == null ? this.manager.getGlobalProvider() : this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role == null) return;
        String permission = context.getArg(1);
        ResolvedPermission myPerm = role.getPermissions().get(permission);
        if (myPerm != null)
        {
            if (myPerm.isSet())
            {
                if (global)
                {
                    context.sendTranslated(POSITIVE, "{name#permission} is set to {text:true:color=DARK_GREEN} for the global role {name}.", permission, role.getName());
                }
                else
                {
                    context.sendTranslated(POSITIVE, "{name#permission} is set to {text:true:color=DARK_GREEN} for the role {name} in {world}.", permission, role.getName(), world);
                }
            }
            else
            {
                if (global)
                {
                    context.sendTranslated(NEGATIVE, "{name#permission} is set to {text:false:color=DARK_RED} for the global role {name}.", permission, role.getName());
                }
                else
                {
                    context.sendTranslated(NEGATIVE, "{name#permission} is set to {text:false:color=DARK_RED} for the role {name} in {world}.", permission, role.getName(), world);
                }
            }
            if (!(myPerm.getOriginPermission() == null && myPerm.getOrigin() == role))
            {
                context.sendTranslated(NEUTRAL, "Permission inherited from:");
                if (myPerm.getOriginPermission() == null)
                {
                    context.sendTranslated(NEUTRAL, "{name#permission} in the role {name}!", myPerm.getKey(), myPerm.getOrigin().getName());
                }
                else
                {
                    context.sendTranslated(NEUTRAL, "{name#permission} in the role {name}!", myPerm.getOriginPermission(), myPerm.getOrigin().getName());
                }
            }
            return;
        }
        if (global)
        {
            context.sendTranslated(NEUTRAL, "The permission {name} is not assigned to the global role {name}.", permission, role.getName());
            return;
        }
        context.sendTranslated(NEUTRAL, "The permission {name} is not assigned to the role {name} in {world}.", permission, role.getName(), world);
    }

    @Alias(names = "listrperm")
    @Command(alias = "listpermission", desc = "Lists all permissions of given role [in world]")
    @IParams(@Grouped(@Indexed(label = "[g:]role")))
    @NParams(@Named(names = "in", label = "world", type = World.class))
    @Flags(@Flag(longName = "all", name = "a"))
    public void listperm(ParameterizedContext context)
    {
        String roleName = context.getArg(0);
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
                context.sendTranslated(NEUTRAL, "No permissions set for the global role {name}.", role.getName());
                return;
            }
            context.sendTranslated(NEUTRAL, "No permissions set for the role {name} in {world}.", role.getName(), world);
            return;
        }
        if (global)
        {
            context.sendTranslated(POSITIVE, "Permissions of the global role {name}.", role.getName());
        }
        else
        {
            context.sendTranslated(POSITIVE, "Permissions of the role {name} in {world}:", role.getName(), world);
        }
        if (context.hasFlag("a"))
        {
            context.sendTranslated(POSITIVE, "(Including inherited permissions)");
        }
        for (String perm : rawPerms.keySet())
        {
            String trueString = ChatFormat.DARK_GREEN + "true";
            String falseString = ChatFormat.DARK_RED + "false";
            if (role.getRawPermissions().get(perm))
            {
                context.sendMessage(String.format(this.LISTELEM_VALUE,perm,trueString));
                continue;
            }
            context.sendMessage(String.format(this.LISTELEM_VALUE,perm,falseString));
        }
    }

    @Alias(names = "listrdata")
    @Command(alias = {"listdata", "listmeta"}, desc = "Lists all metadata of given role [in world]")
    @IParams(@Grouped(@Indexed(label = "[g:]role")))
    @NParams(@Named(names = "in", label = "world", type = World.class))
    @Flags(@Flag(longName = "all", name = "a"))
    public void listmetadata(ParameterizedContext context)
    {
        String roleName = context.getArg(0);
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
                context.sendTranslated(NEUTRAL, "No metadata set for the global role {name}.", role.getName());
                return;
            }
            context.sendTranslated(NEUTRAL, "No metadata set for the role {name} in {world}.", role.getName(), world);
            return;
        }
        if (global)
        {
            context.sendTranslated(POSITIVE, "Metadata of the global role {name}:", role.getName());
        }
        else
        {
            context.sendTranslated(POSITIVE, "Metadata of the role {name} in {world}:", role.getName(), world);
        }
        if (context.hasFlag("a"))
        {
            context.sendTranslated(POSITIVE, "(Including inherited metadata)");
        }
        for (Entry<String, String> data : rawMetadata.entrySet())
        {
            context.sendMessage(String.format(this.LISTELEM_VALUE,data.getKey(), data.getValue()));
        }
    }

    @Alias(names = "listrparent")
    @Command(desc = "Lists all parents of given role [in world]")
    @IParams(@Grouped(@Indexed(label = "[g:]role")))
    @NParams(@Named(names = "in", label = "world", type = World.class))
    public void listParent(ParameterizedContext context)
    {
        String roleName = context.getArg(0);
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
                context.sendTranslated(NEUTRAL, "The global role {name} has no parent roles.", role.getName());
                return;
            }
            context.sendTranslated(NEUTRAL, "The role {name} in {world} has no parent roles.", role.getName(), world);
            return;
        }
        if (global)
        {
            context.sendTranslated(NEUTRAL, "The global role {name} has following parent roles:", role.getName());
        }
        else
        {
            context.sendTranslated(NEUTRAL, "The role {name} in {world} has following parent roles:", role.getName(), world);
        }
        for (Role parent : role.getRoles())
        {
            context.sendMessage(String.format(this.LISTELEM,parent.getName()));
        }
    }

    @Command(alias = "prio", desc = "Show the priority of given role [in world]")
    @IParams(@Grouped(@Indexed(label = "[g:]role")))
    @NParams(@Named(names = "in", label = "world", type = World.class))
    public void priority(ParameterizedContext context)
    {
        String roleName = context.getArg(0);
        boolean global = roleName.startsWith(GLOBAL_PREFIX);
        World world = global ? null : this.getWorld(context);
        if (!global && world == null) return;
        RoleProvider provider = world == null ? this.manager.getGlobalProvider() : this.manager.getProvider(world);
        Role role = this.getRole(context, provider, roleName, world);
        if (role == null) return;
        if (global)
        {
            context.sendTranslated(NEUTRAL, "The priority of the global role {name} is: {integer#priority}", role.getName(), role.getPriorityValue());
            return;
        }
        context.sendTranslated(NEUTRAL, "The priority of the role {name} in {world} is: {integer#priority}", role.getName(), world, role.getPriorityValue());
    }

    @Command(alias = {"default","defaultroles","listdefroles"}, desc = "Lists all default roles [in world]")
    @NParams(@Named(names = "in", label = "world", type = World.class))
    public void listDefaultRoles(ParameterizedContext context)
    {
        World world = this.getWorld(context);
        if (world == null) return;
        WorldRoleProvider provider = this.manager.getProvider(world);
        Set<Role> defaultRoles = provider.getDefaultRoles();
        if (defaultRoles.isEmpty())
        {
            context.sendTranslated(NEGATIVE, "There are no default roles set for {world}!", world);
            return;
        }
        context.sendTranslated(POSITIVE, "The following roles are default roles in {world}!", world);
        for (Role role : defaultRoles)
        {
            context.sendMessage(String.format(this.LISTELEM,role.getName()));
        }
    }
}
