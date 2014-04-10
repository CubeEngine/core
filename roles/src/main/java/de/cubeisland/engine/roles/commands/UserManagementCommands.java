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

import java.util.Set;

import org.bukkit.World;

import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.roles.Roles;
import de.cubeisland.engine.roles.role.DataStore.PermissionValue;
import de.cubeisland.engine.roles.role.Role;
import de.cubeisland.engine.roles.role.RolesAttachment;
import de.cubeisland.engine.roles.role.UserDatabaseStore;

import static de.cubeisland.engine.core.util.formatter.MessageType.*;

public class UserManagementCommands extends UserCommandHelper
{
    public UserManagementCommands(Roles module)
    {
        super(module);
        this.registerAlias(new String[]{"manuser"},new String[]{});
    }

    @Alias(names = {"manuadd", "assignurole", "addurole", "giveurole"})
    @Command(names = {"assign", "add", "give"},
             desc = "Assign a role to the player [in world] [-temp]",
             usage = "<player> <role> [in <world>]",
             params = @Param(names = "in", type = World.class),
             flags = @Flag(name = "t",longName = "temp"),
             max = 2, min = 2)
    public void assign(ParameterizedContext context)
    {
        User user = this.getUser(context, 0);
        if (user == null) return;
        World world = this.getWorld(context);
        if (world == null) return;
        String roleName = context.getString(1);
        Role role = this.manager.getProvider(world).getRole(roleName);
        if (role == null)
        {
            context.sendTranslated(NEUTRAL, "Could not find the role {name} in {world}.", roleName, world);
            return;
        }
        if (!role.canAssignAndRemove(context.getSender()))
        {
            context.sendTranslated(NEGATIVE, "You are not allowed to assign the role {name} in {world}!", role.getName(), world);
            return;
        }
        RolesAttachment attachment = this.manager.getRolesAttachment(user);
        if (context.hasFlag("t"))
        {
            if (!user.isOnline())
            {
                context.sendTranslated(NEGATIVE, "You cannot assign a temporary role to a offline player!");
                return;
            }
            if (attachment.getDataHolder(world).assignTempRole(role))
            {
                attachment.getCurrentDataHolder().apply();
                context.sendTranslated(POSITIVE, "Added the role {name} temporarily to {user} in {world}.", roleName, user, world);
                return;
            }
            context.sendTranslated(NEUTRAL, "{user} already had the role {name} in {world}.", user, roleName, world);
            return;
        }
        if (attachment.getDataHolder(world).assignRole(role))
        {
            attachment.getCurrentDataHolder().apply();
            context.sendTranslated(POSITIVE, "Added the role {name} to {user} in {world}.", roleName, user, world);
            return;
        }
        context.sendTranslated(NEUTRAL, "{user} already has the role {name} in {world}.", user, roleName, world);
    }

    @Alias(names = {"remurole", "manudel"})
    @Command(desc = "Removes a role from the player [in world]",
             usage = "<player> <role> [in <world>]",
             params = @Param(names = "in", type = World.class),
             max = 2, min = 2)
    public void remove(ParameterizedContext context)
    {
        User user = this.getUser(context, 0);
        if (user == null) return;
        World world = this.getWorld(context);
        if (world == null) return;
        Role role = this.manager.getProvider(world).getRole(context.getString(1));
        if (role == null)
        {
            context.sendTranslated(NEUTRAL, "Could not find the role {name} in {world}.", context.getString(1), world);
            return;
        }
        if (!role.canAssignAndRemove(context.getSender()))
        {
            context.sendTranslated(NEGATIVE, "You are not allowed to remove the role {name} in {world}!", role.getName(), world);
            return;
        }
        RolesAttachment attachment = this.manager.getRolesAttachment(user);
        if (attachment.getDataHolder(world).removeRole(role))
        {
            attachment.reload();
            attachment.getCurrentDataHolder().apply();
            context.sendTranslated(POSITIVE, "Removed the role {name} from {user} in {world}.", role.getName(), user, world);
            return;
        }
        context.sendTranslated(NEUTRAL, "{user} did not have the role {name} in {world}.", user, role.getName(), world);
    }

    @Alias(names = {"clearurole", "manuclear"})
    @Command(desc = "Clears all roles from the player and sets the defaultroles [in world]",
             usage = "<player> [in <world>]",
             params = @Param(names = "in", type = World.class),
             max = 1, min = 1)
    public void clear(ParameterizedContext context)
    {
        User user = this.getUser(context, 0);
        if (user == null) return;
        World world = this.getWorld(context);
        if (world == null) return;
        RolesAttachment attachment = this.manager.getRolesAttachment(user);
        UserDatabaseStore dataHolder = attachment.getDataHolder(world);
        dataHolder.clearRoles();
        Set<Role> defaultRoles = this.manager.getProvider(world).getDefaultRoles();
        for (Role role : defaultRoles)
        {
            dataHolder.assignTempRole(role);
        }
        dataHolder.apply();
        context.sendTranslated(NEUTRAL, "Cleared the roles of {user} in {world}.", user, world);
        if (!defaultRoles.isEmpty())
        {
            context.sendTranslated(NEUTRAL, "Default roles assigned:");
            for (Role role : defaultRoles)
            {
                context.sendMessage(String.format(this.LISTELEM, role.getName()));
            }
        }
    }

    @Alias(names = "setuperm")
    @Command(names = {"setperm", "setpermission"},
             desc = "Sets a permission for this user [in world]",
             usage = " <player> <permission> [true|false|reset] [in <world>]",
             params = @Param(names = "in", type = World.class),
             max = 3, min = 2)
    public void setpermission(ParameterizedContext context)
    {
        User user = this.getUser(context, 0);
        if (user == null) return;
        String perm = context.getString(1);
        String setTo = "true";
        if (context.hasArg(2))
        {
            setTo = context.getString(2);
        }
        try
        {
            PermissionValue value = PermissionValue.valueOf(setTo.toUpperCase());
            World world = this.getWorld(context);
            if (world == null) return;
            RolesAttachment attachment = this.manager.getRolesAttachment(user);
            attachment.getDataHolder(world).setPermission(perm, value);
            attachment.getCurrentDataHolder().apply();
            if (value == PermissionValue.RESET)
            {
                context.sendTranslated(NEUTRAL, "Permission {input} of {user} reset!", perm, user);
                return;
            }
            if (value == PermissionValue.TRUE)
            {
                context.sendTranslated(POSITIVE, "Permission {input} of {user} set to true!", perm, user);
                return;
            }
            context.sendTranslated(NEGATIVE, "Permission {input} of {user} set to false!", perm, user);
        }
        catch (IllegalArgumentException e)
        {
            context.sendTranslated(NEGATIVE, "Unknown setting: \"Unknown setting: {input} Use {text:true},{text:false} or {text:reset}!", setTo);
        }
    }

    @Alias(names = "resetuperm")
    @Command(names = {"resetperm", "resetpermission"},
             desc = "Resets a permission for this user [in world]",
             usage = " <player> <permission> [in <world>]",
             params = @Param(names = "in", type = World.class),
             max = 2, min = 2)
    public void resetpermission(ParameterizedContext context)
    {
        User user = this.getUser(context, 0);
        if (user == null) return;
        String perm = context.getString(1);
        World world = this.getWorld(context);
        if (world == null) return;
        RolesAttachment attachment = this.manager.getRolesAttachment(user);
        attachment.getDataHolder(world).setPermission(perm, PermissionValue.RESET);
        attachment.getCurrentDataHolder().apply();
        context.sendTranslated(NEUTRAL, "Permission {input} of {user} resetted!", perm, user);
    }

    @Alias(names = {"setudata","setumeta","setumetadata"})
    @Command(names = {"setdata", "setmeta", "setmetadata"},
             desc = "Sets metadata for this user [in world]",
             usage = "<player> <metaKey> <metaValue> [in <world>]",
             params = @Param(names = "in", type = World.class),
             max = 3, min = 3)
    public void setmetadata(ParameterizedContext context)
    {
        String metaKey = context.getString(1);
        String metaVal = context.getString(2);
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated(NEGATIVE, "User {user} not found!", context.getString(0));
            return;
        }
        World world = this.getWorld(context);
        if (world == null) return;
        RolesAttachment attachment = this.manager.getRolesAttachment(user);
        attachment.getDataHolder(world).setMetadata(metaKey, metaVal);
        attachment.getCurrentDataHolder().apply();
        context.sendTranslated(POSITIVE, "Metadata {input#key} of {user} set to {input#value} in {world}!", metaKey, user, metaVal, world);
    }

    @Alias(names = {"resetudata","resetumeta","resetumetadata"})
    @Command(names = {"resetdata", "resetmeta", "resetmetadata", "deletedata", "deletemetadata", "deletemeta"},
             desc = "Resets metadata for this user [in world]",
             usage = "<player> <metaKey> [in <world>]",
             params = @Param(names = "in", type = World.class),
             max = 2, min = 2)
    public void resetmetadata(ParameterizedContext context)
    {
        String metaKey = context.getString(1);
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated(NEGATIVE, "User {user} not found!", context.getString(0));
            return;
        }
        World world = this.getWorld(context);
        if (world == null) return;
        RolesAttachment attachment = this.manager.getRolesAttachment(user);
        attachment.getDataHolder(world).removeMetadata(metaKey);
        attachment.getCurrentDataHolder().apply();
        context.sendTranslated(NEUTRAL, "Metadata {input#key} of {user} removed in {world}!", metaKey, user, world);
    }

    @Alias(names = {"clearudata","clearumeta","clearumetadata"})
    @Command(names = {"cleardata", "clearmeta", "clearmetadata"},
             desc = "Resets metadata for this user [in world]",
             usage = "<player> [in <world>]",
             params = @Param(names = "in", type = World.class),
             max = 1, min = 1)
    public void clearMetaData(ParameterizedContext context)
    {
        User user = this.getUser(context, 0);
        if (user == null) return;
        World world = this.getWorld(context);
        if (world == null) return;
        RolesAttachment attachment = this.manager.getRolesAttachment(user);
        attachment.getDataHolder(world).clearMetadata();
        attachment.getCurrentDataHolder().apply();
        context.sendTranslated(NEUTRAL, "Metadata of {user} cleared in {world}!", user, world);
    }
}
