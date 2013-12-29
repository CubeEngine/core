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
package de.cubeisland.engine.vaultlink.service;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.permission.PermissionManager;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserManager;
import de.cubeisland.engine.core.world.WorldManager;
import de.cubeisland.engine.roles.Roles;
import de.cubeisland.engine.roles.role.RawDataStore;
import de.cubeisland.engine.roles.role.Role;
import de.cubeisland.engine.roles.role.RoleProvider;
import de.cubeisland.engine.roles.role.RolesAttachment;
import de.cubeisland.engine.roles.role.resolved.ResolvedPermission;
import de.cubeisland.engine.vaultlink.Vaultlink;
import net.milkbowl.vault.permission.Permission;

public class CubePermissionService extends Permission
{
    private final Vaultlink module;
    private final Roles roles;
    private final UserManager um;
    private final WorldManager wm;
    private final PermissionManager pm;

    public CubePermissionService(Vaultlink module, Roles roles)
    {
        this.module = module;
        this.roles = roles;
        this.um = roles.getCore().getUserManager();
        this.wm = roles.getCore().getWorldManager();
        this.pm = roles.getCore().getPermissionManager();
    }

    @Override
    public String getName()
    {
        return CubeEngine.class.getSimpleName() + ":" + module.getName();
    }

    @Override
    public boolean isEnabled()
    {
        return roles.isEnabled();
    }

    @Override
    public boolean hasSuperPermsCompat()
    {
        return true;
    }

    @Override
    public boolean playerHas(String world, String player, String permission)
    {
        User user = um.getUser(player);
        if (user == null)
        {
            return false;
        }

        RolesAttachment attachment = user.get(RolesAttachment.class);

        ResolvedPermission resolved = attachment.getPermissions(wm.getWorldId(world)).get(permission);
        if (resolved == null)
        {
            switch (pm.getDefaultFor(permission))
            {
                case TRUE:
                    return true;
                case FALSE:
                    return false;
                case OP:
                    return user.isOp();
                case NOT_OP:
                    return !user.isOp();
                default:
                    return user.isOp();
            }
        }
        return resolved.isSet();
    }

    @Override
    public boolean playerAdd(String world, String player, String permission)
    {
        User user = roles.getCore().getUserManager().getUser(player);
        if (user == null)
        {
            return false;
        }

        RolesAttachment attachment = user.get(RolesAttachment.class);
        attachment.getRawData(wm.getWorldId(world)).setPermission(permission, true);
        attachment.apply();
        return true;
    }

    @Override
    public boolean playerAddTransient(Player player, String permission)
    {
        User user = roles.getCore().getUserManager().getUser(player.getName());
        if (user == null)
        {
            return false;
        }

        RolesAttachment attachment = user.get(RolesAttachment.class);
        RawDataStore store = attachment.getTemporaryRawData(wm.getWorldId(player.getWorld()));

        store.setPermission(permission, true);
        attachment.apply();
        return true;
    }

    @Override
    public boolean playerRemove(String world, String player, String permission)
    {
        User user = roles.getCore().getUserManager().getUser(player);
        if (user == null)
        {
            return false;
        }

        RolesAttachment attachment = user.get(RolesAttachment.class);
        attachment.getRawData(wm.getWorldId(world)).setPermission(permission, null);
        attachment.apply();
        return true;
    }

    @Override
    public boolean playerRemoveTransient(Player player, String permission)
    {
        User user = roles.getCore().getUserManager().getUser(player.getName());
        if (user == null)
        {
            return false;
        }

        RolesAttachment attachment = user.get(RolesAttachment.class);
        RawDataStore store = attachment.getTemporaryRawData(wm.getWorldId(player.getWorld()));

        store.setPermission(permission, true);
        attachment.apply();
        return true;
    }

    @Override
    public boolean groupHas(String world, String group, String permission)
    {
        Role role = roles.getRolesManager().getProvider(wm.getWorldId(world)).getRole(group);
        if (role == null)
        {
            return false;
        }
        ResolvedPermission resolved = role.getPermissions().get(permission);
        return resolved != null && resolved.isSet();
    }

    @Override
    public boolean groupAdd(String world, String group, String permission)
    {
        Role role = roles.getRolesManager().getProvider(wm.getWorldId(world)).getRole(group);
        if (role == null)
        {
            return false;
        }
        role.setPermission(permission, true);
        role.saveToConfig();
        roles.getRolesManager().recalculateAllRoles();
        return true;
    }

    @Override
    public boolean groupRemove(String world, String group, String permission)
    {
        Role role = roles.getRolesManager().getProvider(wm.getWorldId(world)).getRole(group);
        if (role == null)
        {
            return false;
        }
        role.setPermission(permission, null);
        role.saveToConfig();
        return true;
    }

    @Override
    public boolean playerInGroup(String world, String player, String group)
    {
        User user = roles.getCore().getUserManager().getUser(player);
        if (user == null)
        {
            return false;
        }

        RolesAttachment attachment = user.get(RolesAttachment.class);
        RawDataStore store = attachment.getRawData(wm.getWorldId(world));
        return store.getRawAssignedRoles().contains(group);
    }

    @Override
    public boolean playerAddGroup(String world, String player, String group)
    {
        User user = roles.getCore().getUserManager().getUser(player);
        if (user == null)
        {
            return false;
        }

        RolesAttachment attachment = user.get(RolesAttachment.class);
        RawDataStore store = attachment.getRawData(wm.getWorldId(world));
        store.assignRole(roles.getRolesManager().getProvider(wm.getWorldId(world)).getRole(group));
        attachment.apply();
        return true;
    }

    @Override
    public boolean playerRemoveGroup(String world, String player, String group)
    {
        User user = roles.getCore().getUserManager().getUser(player);
        if (user == null)
        {
            return false;
        }

        RolesAttachment attachment = user.get(RolesAttachment.class);
        RawDataStore store = attachment.getRawData(wm.getWorldId(world));
        store.removeRole(roles.getRolesManager().getProvider(wm.getWorldId(world)).getRole(group));
        attachment.apply();
        return true;
    }

    @Override
    public String[] getPlayerGroups(String world, String player)
    {
        User user = roles.getCore().getUserManager().getUser(player);
        if (user == null)
        {
            return null;
        }

        Set<String> roles = new HashSet<>();
        RolesAttachment attachment = user.get(RolesAttachment.class);
        for (Role role : attachment.getRawData(wm.getWorldId(world)).getAssignedRoles())
        {
            roles.add(role.getName());
        }
        for (Role role : attachment.getTemporaryRawData(wm.getWorldId(world)).getAssignedRoles())
        {
            roles.add(role.getName());
        }
        return roles.toArray(new String[roles.size()]);
    }

    @Override
    public String getPrimaryGroup(String world, String player)
    {
        User user = roles.getCore().getUserManager().getUser(player);
        if (user == null)
        {
            return null;
        }

        RolesAttachment attachment = user.get(RolesAttachment.class);
        return attachment.getDominantRole(wm.getWorldId(world)).getName();
    }

    @Override
    public String[] getGroups()
    {
        Set<String> roles = new HashSet<>();
        for (long id : wm.getAllWorldIds())
        {
            RoleProvider provider = this.roles.getRolesManager().getProvider(id);
            for (Role role : provider.getRoles())
            {
                roles.add(role.getName());
            }
        }
        return roles.toArray(new String[roles.size()]);
    }
}
