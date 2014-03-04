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

import org.bukkit.World;

import de.cubeisland.engine.core.command.parameterized.Flag;
import de.cubeisland.engine.core.command.parameterized.Param;
import de.cubeisland.engine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.engine.core.command.reflected.Alias;
import de.cubeisland.engine.core.command.reflected.Command;
import de.cubeisland.engine.core.permission.PermDefault;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.roles.Roles;
import de.cubeisland.engine.roles.role.Role;
import de.cubeisland.engine.roles.role.TempDataStore;
import de.cubeisland.engine.roles.role.RolesAttachment;
import de.cubeisland.engine.roles.role.UserDatabaseStore;
import de.cubeisland.engine.roles.role.resolved.ResolvedMetadata;
import de.cubeisland.engine.roles.role.resolved.ResolvedPermission;

public class UserInformationCommands extends UserCommandHelper
{
    public UserInformationCommands(Roles module)
    {
        super(module);
    }

    @Alias(names = "listuroles")
    @Command(desc = "Lists roles of a user [in world]",
             usage = "[player] [in <world>]",
             params = @Param(names = "in", type = World.class),
             max = 1)
    public void list(ParameterizedContext context)
    {
        User user = this.getUser(context, 0);
        if (user == null) return;
        World world = this.getWorld(context);
        if (world == null) return;
        RolesAttachment rolesAttachment = this.manager.getRolesAttachment(user);
        // List all assigned roles
        context.sendTranslated("&eRoles of &2%s&e in &6%s&e:", user.getName(), world.getName());
        for (Role pRole : rolesAttachment.getDataHolder(world).getRoles())
            {
            if (pRole.isGlobal())
            {
                context.sendMessage(String.format(this.LISTELEM_VALUE,"global",pRole.getName()));
                continue;
            }
            context.sendMessage(String.format(this.LISTELEM_VALUE,world.getName(),pRole.getName()));
        }
    }

    @Alias(names = "checkuperm")
    @Command(names = {"checkperm", "checkpermission"},
             desc = "Checks for permissions of a user [in world]",
             usage = "<player> <permission> [in <world>]",
             params = @Param(names = "in", type = World.class),
             max = 2, min = 2)
    public void checkpermission(ParameterizedContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated("&cUser %s not found!", context.getString(0));
            return;
        }
        World world = this.getWorld(context);
        if (world == null) return;
        RolesAttachment rolesAttachment = this.manager.getRolesAttachment(user);
        // Search for permission
        String permission = context.getString(1);
        ResolvedPermission resolvedPermission = rolesAttachment.getDataHolder(world).getPermissions().get(permission);
        if (user.isOp())
        {
            context.sendTranslated("&2%s&a is Op!", user.getName());
        }
        if (user.isOnline()) // Can have superperm
        {
            boolean superPerm = user.hasPermission(permission);
            context.sendTranslated("&eSuperPerm Node: %s", superPerm);
        }
        if (resolvedPermission == null)
        {

            PermDefault defaultFor = this.module.getCore().getPermissionManager().getDefaultFor(permission);
            if (defaultFor == null)
            {
                context.sendTranslated("&cPermission &6%s&c neither set nor registered!", permission);
            }
            else
            {
                context.sendTranslated("&cPermission &6%s&c not set but default is: &6%s&c!", permission, defaultFor.name());
            }
            return;
        }
        context.sendTranslated((resolvedPermission.isSet()
            ? "&aThe player &2%s&a does have access to &f\"&6%s&f\"&a"
            : "&cThe player &2%s&c does not have access to &f\"&6%s&f\"&c")
                                   + " in &6%s", user.getName(), permission, world.getName());

        // Display origin
        TempDataStore store = resolvedPermission.getOrigin();
        if (resolvedPermission.getOriginPermission() != null) // indirect permission
        {
            permission = resolvedPermission.getOriginPermission();
        }
        context.sendTranslated("&ePermission inherited from:");
        if (user.getName().equals(store.getName()))
        {
            context.sendTranslated("&6%s&e directly assigned to the user!", permission);
            return;
        }
        context.sendTranslated("&6%s&e in the role &6%s&e!", permission, store.getName());
    }

    @Alias(names = "listuperm")
    @Command(names = {"listperm", "listpermission"},
             desc = "List permission assigned to a user in a world",
             usage = "[player] [in <world>] [-all]",
             params = @Param(names = "in", type = World.class),
             flags = @Flag(longName = "all", name = "a"),
             max = 1)
    public void listpermission(ParameterizedContext context)
    {
        User user = this.getUser(context, 0);
        if (user == null) return;
        World world = this.getWorld(context);
        if (world == null) return;
        RolesAttachment rolesAttachment = this.manager.getRolesAttachment(user);
        UserDatabaseStore rawData = rolesAttachment.getDataHolder(world);
        Map<String,Boolean> perms = context.hasFlag("a") ? rawData.getAllRawPermissions() : rawData.getRawPermissions();
        if (perms.isEmpty())
        {
            context.sendTranslated("&2%s &ehas no permissions set in &6%s&e.", user.getName(), world.getName());
            return;
        }
        context.sendTranslated("&ePermissions of &2%s&e in &6%s&e.", user.getName(), world.getName());
        for (Map.Entry<String, Boolean> entry : perms.entrySet())
        {
            context.sendMessage(String.format(this.LISTELEM_VALUE,entry.getKey(), entry.getValue()));
        }
    }

    @Alias(names = "checkumeta")
    @Command(names = {"checkdata", "checkmeta", "checkmetadata"},
             desc = "Checks for metadata of a user [in world]",
             usage = "<player> <metadatakey> [in <world>]",
             params = @Param(names = "in", type = World.class),
             max = 2, min = 2)
    public void checkmetadata(ParameterizedContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated("&cUser %s not found!", context.getString(0));
            return;
        }
        World world = this.getWorld(context);
        if (world == null) return;
        RolesAttachment rolesAttachment = this.manager.getRolesAttachment(user);
        // Check metadata
        String metaKey = context.getString(1);
        UserDatabaseStore dataHolder = rolesAttachment.getDataHolder(world);
        Map<String,ResolvedMetadata> metadata = dataHolder.getMetadata();
        if (!metadata.containsKey(metaKey))
        {
            context.sendTranslated("&6%s &is not set for &2%s&e in &6%s&e.", metaKey, user.getName(), world.getName());
            return;
        }
        context.sendTranslated("&6%s&e: &6%s&e is set for &2%s&e in &6%s&e.", metaKey, metadata.get(metaKey).getValue(), user.getName(), world.getName());
        if (metadata.get(metaKey).getOrigin() != dataHolder)
        {
            context.sendTranslated("&eOrigin: &6%s&e", metadata.get(metaKey).getOrigin().getName());
        }
        else
        {
            context.sendTranslated("&eOrigin: &6directly assigned");
        }
    }

    @Alias(names = "listumeta")
    @Command(names = {"listdata", "listmeta", "listmetadata"},
             desc = "Lists assigned metadata from a user [in world]",
             usage = "[player] [in <world>]",
             params = @Param(names = "in", type = World.class),
             flags = @Flag(longName = "all", name = "a"),
             max = 1)
    public void listmetadata(ParameterizedContext context)
    {
        User user = this.getUser(context, 0);
        if (user == null) return;
        World world = this.getWorld(context);
        if (world == null) return;
        RolesAttachment rolesAttachment = this.manager.getRolesAttachment(user);
        UserDatabaseStore rawData = rolesAttachment.getDataHolder(world);
        Map<String, String> metadata = context.hasFlag("a") ? rawData.getAllRawMetadata() : rawData.getRawMetadata();
        // List all metadata
        context.sendTranslated("&eMetadata of &2%s&e in &6%s&e.:", user.getName(), world.getName());
        for (Map.Entry<String, String> entry : metadata.entrySet())
        {
            context.sendMessage(String.format(this.LISTELEM_VALUE,entry.getKey(), entry.getValue()));
        }
    }
}
