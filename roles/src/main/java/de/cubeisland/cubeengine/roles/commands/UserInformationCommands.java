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

import java.util.Collection;
import java.util.Map;

import org.bukkit.World;

import de.cubeisland.cubeengine.core.command.parameterized.Param;
import de.cubeisland.cubeengine.core.command.parameterized.ParameterizedContext;
import de.cubeisland.cubeengine.core.command.reflected.Alias;
import de.cubeisland.cubeengine.core.command.reflected.Command;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.roles.Roles;
import de.cubeisland.cubeengine.roles.role.UserSpecificRole;

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
        Role role = this.getUserRole(user, world);
        // List all assigned roles
        Collection<ConfigRole> roles = role.getParentRoles();
        context.sendTranslated("&eRoles of &2%s&e in &6%s&e:", user.getName(), world.getName());
        for (Role pRole : roles)
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
        UserSpecificRole role = this.getUserRole(user, world);
        // Search for permission
        String permission = context.getString(1);
        RolePermission myPerm = role.getPerms().get(permission);
        if (user.isOp())
        {
            context.sendTranslated("&2%s &ais Op!", user.getName());
        }
        if (user.isOnline() && !permission.endsWith("*")) // Can have superperm
        {
            boolean superPerm = user.hasPermission(permission);
            context.sendTranslated("&eSuperPerm Node: %s", superPerm);
        }
        if (myPerm == null)
        {
            context.sendTranslated("&cPermission not declared!");
        }
        else
        {
            context.sendTranslated((myPerm.isSet()
                ? "&aThe player &2%s &adoes have access to &f\"&6%s&f\"&a"
                : "&cThe player &2%s &cdoes not have access to &f\"&6%s&f\"&c")
                                       + " in &6%s", user.getName(), permission, world.getName());

            // Display origin
            Role originRole = myPerm.getOrigin();
            if (!originRole.getLitaralPerms().containsKey(permission))
            {
                while (!permission.equals("*"))
                {
                    if (permission.endsWith("*"))
                    {
                        permission = permission.substring(0, permission.lastIndexOf("."));
                    }
                    permission = permission.substring(0, permission.lastIndexOf(".") + 1) + "*";

                    if (originRole.getLitaralPerms().containsKey(permission))
                    {
                        if (originRole.getLitaralPerms().get(permission) == myPerm.isSet())
                        {
                            break;
                        }
                    }
                }
            }
            context.sendTranslated("&ePermission inherited from:");
            if (user.getName().equals(originRole.getName()))
            {
                context.sendTranslated("&6%s &ein the users role!", permission);
            }
            else
            {
                context.sendTranslated("&6%s &ein the role &6%s&e!", permission, originRole.getName());
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
        UserSpecificRole role = this.getUserRole(user, world);
        // List permissions
        if (role.getAllLiteralPerms().isEmpty())
        {
            context.sendTranslated("&2%s &ehas no permissions set in &6%s&e.", user.getName(), world.getName());
        }
        else
        {
            context.sendTranslated("&ePermissions of &2%s&e in &6%s&e.", user.getName(), world.getName());
            for (Map.Entry<String, Boolean> entry : role.getAllLiteralPerms().entrySet())
            {
                context.sendMessage("- &e" + entry.getKey() + ": &6" + entry.getValue());
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
        UserSpecificRole role = this.getUserRole(user, world);
        // Check metadata
        String metaKey = context.getString(1);
        if (!role.getMetaData().containsKey(metaKey))
        {
            context.sendTranslated("&6%s &is not set for &2%s &ein &6%s&e.", metaKey, user.getName(), world.getName());
            return;
        }
        RoleMetaData value = role.getMetaData().get(metaKey);
        context.sendTranslated("&6%s&e: &6%s&e is set for &2%s &ein &6%s&e.", metaKey, value.getValue(), user.getName(), world.getName());
        if (value.getOrigin() != role)
        {
            context.sendTranslated("&eOrigin: &&%s&e.", value.getOrigin().getName());
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
        UserSpecificRole role = this.getUserRole(user, world);
        // List all metadata
        context.sendTranslated("&eMetadata of &2%s&e in &6%s&e.:", user.getName(), world.getName());
        for (Map.Entry<String, RoleMetaData> entry : role.getMetaData().entrySet())
        {
            context.sendMessage("- &e" + entry.getKey() + ": &6" + entry.getValue().getValue());
        }
    }
}
