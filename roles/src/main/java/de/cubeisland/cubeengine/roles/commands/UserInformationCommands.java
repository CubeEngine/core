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

import java.util.Map;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.command.parameterized.Param;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Alias;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.RawDataStore;
import de.cubeisland.cubeengine.roles.role.Role;
import de.cubeisland.cubeengine.roles.role.RolesAttachment;
import de.cubeisland.cubeengine.roles.role.resolved.ResolvedMetadata;
import de.cubeisland.cubeengine.roles.role.resolved.ResolvedPermission;

public class UserInformationCommands extends UserCommandHelper
{
    public UserInformationCommands(Roles module)
    {
        super(module);
    }

    @Alias(names = "listuroles")
    @Command(desc = "Lists roles of a user [in world]", usage = "[player] [in <world>]", params = @Param(names = "in", type = World.class), max = 2)
    public void list(ParameterizedContext context)
    {
        User user = this.getUser(context, 0);
        World world = this.getWorld(context);
        RolesAttachment rolesAttachment = this.manager.getRolesAttachment(user);
        // List all assigned roles
        context.sendTranslated("&eRoles of &2%s&e in &6%s&e:", user.getName(), world.getName());
        for (Role pRole : rolesAttachment.getAssignedRoles(this.worldManager.getWorldId(world)))
        {
            if (pRole.isGlobal())
            {
                context.sendMessage("&6global&e: " + pRole.getName());
            }
            else
            {
                context.sendMessage("&6" + world.getName() + "&e: " + pRole.getName());
            }
        }
    }

    @Alias(names = "checkuperm")
    @Command(names = {
        "checkperm", "checkpermission"
    }, desc = "Checks for permissions of a user [in world]", usage = "<player> <permission> [in <world>]", params = @Param(names = "in", type = World.class), max = 3, min = 1)
    public void checkpermission(ParameterizedContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated("&cUser %s not found!", context.getString(0));
            return;
        }
        World world = this.getWorld(context);
        RolesAttachment rolesAttachment = this.manager.getRolesAttachment(user);
        // Search for permission
        String permission = context.getString(1);
        ResolvedPermission resolvedPermission = rolesAttachment.
            getPermissions(this.worldManager.getWorldId(world)).get(permission);
        if (user.isOp())
        {
            context.sendTranslated("&2%s &ais Op!", user.getName());
        }
        if (user.isOnline() && !permission.endsWith("*")) // Can have superperm
        {
            boolean superPerm = user.hasPermission(permission);
            context.sendTranslated("&eSuperPerm Node: %s", superPerm);
        }
        if (resolvedPermission == null)
        {
            context.sendTranslated("&cPermission not declared!");
        }
        else
        {
            context.sendTranslated((resolvedPermission.isSet()
                ? "&aThe player &2%s &adoes have access to &f\"&6%s&f\"&a"
                : "&cThe player &2%s &cdoes not have access to &f\"&6%s&f\"&c")
                                       + " in &6%s", user.getName(), permission, world.getName());

            // Display origin
            RawDataStore store = resolvedPermission.getOrigin();
            if (!store.getRawPermissions().containsKey(permission))
            {
                while (!permission.equals("*"))
                {
                    if (permission.endsWith("*"))
                    {
                        permission = permission.substring(0, permission.lastIndexOf("."));
                    }
                    permission = permission.substring(0, permission.lastIndexOf(".") + 1) + "*";

                    if (store.getRawPermissions().containsKey(permission))
                    {
                        if (store.getRawPermissions().get(permission) == resolvedPermission.isSet())
                        {
                            break;
                        }
                    }
                }
            }
            context.sendTranslated("&ePermission inherited from:");
            if (user.getName().equals(store.getName()))
            {
                context.sendTranslated("&6%s &ein the users role!", permission);
            }
            else
            {
                context.sendTranslated("&6%s &ein the role &6%s&e!", permission, store.getName());
            }
        }
    }

    @Alias(names = "listuperm")
    @Command(names = {
        "listperm", "listpermission"
    }, desc = "List permission of a user [in world]", usage = "[player] [in <world>]", params = @Param(names = "in", type = World.class), max = 2)
    public void listpermission(ParameterizedContext context)
    {
        User user = this.getUser(context, 0);
        World world = this.getWorld(context);
        RolesAttachment rolesAttachment = this.manager.getRolesAttachment(user);
        // List permissions
        Map<String,Boolean> perms = rolesAttachment.getAllRawPermissions();
        if (perms.isEmpty())
        {
            context.sendTranslated("&2%s &ehas no permissions set in &6%s&e.", user.getName(), world.getName());
        }
        else
        {
            context.sendTranslated("&ePermissions of &2%s&e in &6%s&e.", user.getName(), world.getName());
            for (Map.Entry<String, Boolean> entry : perms.entrySet())
            {
                context.sendMessage(String.format(this.LISTELEM_VALUE,entry.getKey(), entry.getValue()));
            }
        }
    }

    @Alias(names = "checkumeta")
    @Command(names = {
        "checkdata", "checkmeta", "checkmetadata"
    }, desc = "Checks for metadata of a user [in world]", usage = "<player> <metadatakey> [in <world>]", params = @Param(names = "in", type = World.class), max = 3, min = 1)
    public void checkmetadata(ParameterizedContext context)
    {
        User user = context.getUser(0);
        if (user == null)
        {
            context.sendTranslated("&cUser %s not found!", context.getString(0));
            return;
        }
        World world = this.getWorld(context);
        RolesAttachment rolesAttachment = this.manager.getRolesAttachment(user);
        // Check metadata
        String metaKey = context.getString(1);
        Map<String,ResolvedMetadata> metadata = rolesAttachment.getMetadata(this.worldManager.getWorldId(world));
        if (!metadata.containsKey(metaKey))
        {
            context.sendTranslated("&6%s &is not set for &2%s&e in &6%s&e.", metaKey, user.getName(), world.getName());
            return;
        }
        context.sendTranslated("&6%s&e: &6%s&e is set for &2%s &ein &6%s&e.", metaKey, metadata.get(metaKey).getValue(), user.getName(), world.getName());
        if (metadata.get(metaKey).getOrigin() != rolesAttachment.getRawData(this.worldManager.getWorldId(world)))
        {
            context.sendTranslated("&eOrigin: &&%s&e.", metadata.get(metaKey).getOrigin().getName());
        }
    }

    @Alias(names = "listumeta")
    @Command(names = {
        "listdata", "listmeta", "listmetadata"
    }, desc = "List metadata of a user [in world]", usage = "[player] [in <world>]", params = @Param(names = "in", type = World.class), max = 2)
    public void listmetadata(ParameterizedContext context)
    {
        User user = this.getUser(context, 0);
        World world = this.getWorld(context);
        RolesAttachment rolesAttachment = this.manager.getRolesAttachment(user);
        Map<String, ResolvedMetadata> metadata = rolesAttachment.getMetadata(this.worldManager.getWorldId(world));
        // List all metadata
        context.sendTranslated("&eMetadata of &2%s&e in &6%s&e.:", user.getName(), world.getName());
        for (Map.Entry<String, ResolvedMetadata> entry : metadata.entrySet())
        {
            context.sendMessage(String.format(this.LISTELEM_VALUE,entry.getKey(), entry.getValue().getValue()));
        }
    }
}
