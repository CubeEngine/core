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

import org.bukkit.World;
import org.bukkit.entity.Player;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.permission.PermissionManager;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.world.WorldManager;
import de.cubeisland.engine.roles.Roles;
import de.cubeisland.engine.roles.role.DataStore.PermissionValue;
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
    private final WorldManager wm;
    private final PermissionManager pm;

    public CubePermissionService(Vaultlink module, Roles roles)
    {
        this.module = module;
        this.roles = roles;
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
    public boolean playerHas(String worldName, String player, String permission)
    {
        User user = roles.getCore().getUserManager().findExactUser(player);
        if (user == null)
        {
            return false;
        }
        World world;
        if (worldName == null)
        {
            world = user.getWorld();
        }
        else
        {
            world = this.module.getCore().getWorldManager().getWorld(worldName);
        }
        if (world == null)
        {
            return false;
        }
        RolesAttachment attachment = user.get(RolesAttachment.class);
        ResolvedPermission resolved = attachment.getDataHolder(world).getPermissions().get(permission);
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
    public boolean playerAdd(String worldName, String player, String permission)
    {
        User user = roles.getCore().getUserManager().findExactUser(player);
        if (user == null)
        {
            return false;
        }
        World world;
        if (worldName == null)
        {
            world = user.getWorld();
        }
        else
        {
            world = this.module.getCore().getWorldManager().getWorld(worldName);
        }
        if (world == null)
        {
            return false;
        }
        RolesAttachment attachment = user.get(RolesAttachment.class);
        attachment.getDataHolder(world).setPermission(permission, PermissionValue.TRUE);
        attachment.getCurrentDataHolder().apply();
        return true;
    }

    @Override
    public boolean playerAddTransient(Player player, String permission)
    {
        User user = roles.getCore().getUserManager().getExactUser(player.getUniqueId());
        if (user == null)
        {
            return false;
        }
        RolesAttachment attachment = user.get(RolesAttachment.class);
        attachment.getCurrentDataHolder().setTempPermission(permission, PermissionValue.TRUE);
        attachment.getCurrentDataHolder().apply();
        return true;
    }

    @Override
    public boolean playerRemove(String worldName, String player, String permission)
    {
        User user = roles.getCore().getUserManager().findExactUser(player);
        if (user == null)
        {
            return false;
        }
        World world;
        if (worldName == null)
        {
            world = user.getWorld();
        }
        else
        {
            world = this.module.getCore().getWorldManager().getWorld(worldName);
        }
        if (world == null)
        {
            return false;
        }
        RolesAttachment attachment = user.get(RolesAttachment.class);
        attachment.getDataHolder(world).setPermission(permission, PermissionValue.RESET);
        attachment.getDataHolder(world).apply();
        return true;
    }

    @Override
    public boolean playerRemoveTransient(Player player, String permission)
    {
        User user = roles.getCore().getUserManager().getExactUser(player.getUniqueId());
        if (user == null)
        {
            return false;
        }
        RolesAttachment attachment = user.get(RolesAttachment.class);
        attachment.getCurrentDataHolder().setPermission(permission, PermissionValue.RESET);
        attachment.getCurrentDataHolder().apply();
        return true;
    }

    @Override
    public boolean groupHas(String worldName, String group, String permission)
    {
        if (group == null)
        {
            this.module.getLog().warn(new IllegalArgumentException(), "The group name should never be null!");
            return false;
        }
        RoleProvider provider;
        if (worldName != null)
        {
            World world = this.module.getCore().getWorldManager().getWorld(worldName);
            if (world == null)
            {
                return false;
            }
            provider = roles.getRolesManager().getProvider(world);
        }
        else
        {
            provider = roles.getRolesManager().getGlobalProvider();
        }
        Role role = provider.getRole(group);
        if (role == null)
        {
            return false;
        }
        ResolvedPermission resolved = role.getPermissions().get(permission);
        return resolved != null && resolved.isSet();
    }

    @Override
    public boolean groupAdd(String worldName, String group, String permission)
    {
        if (group == null)
        {
            this.module.getLog().warn(new IllegalArgumentException(), "The group name should never be null!");
            return false;
        }
        RoleProvider provider;
        if (worldName != null)
        {
            World world = this.module.getCore().getWorldManager().getWorld(worldName);
            if (world == null)
            {
                return false;
            }
            provider = roles.getRolesManager().getProvider(world);
        }
        else
        {
            provider = roles.getRolesManager().getGlobalProvider();
        }
        Role role = provider.getRole(group);
        if (role == null)
        {
            return false;
        }
        role.setPermission(permission, PermissionValue.TRUE);
        role.save();
        return true;
    }

    @Override
    public boolean groupRemove(String worldName, String group, String permission)
    {
        if (group == null)
        {
            this.module.getLog().warn(new IllegalArgumentException(), "The group name should never be null!");
            return false;
        }
        RoleProvider provider;
        if (worldName != null)
        {
            World world = this.module.getCore().getWorldManager().getWorld(worldName);
            if (world == null)
            {
                return false;
            }
            provider = roles.getRolesManager().getProvider(world);
        }
        else
        {
            provider = roles.getRolesManager().getGlobalProvider();
        }
        Role role = provider.getRole(group);
        if (role == null)
        {
            return false;
        }
        role.setPermission(permission, null);
        role.save();
        return true;
    }

    @Override
    public boolean playerInGroup(String worldName, String player, String group)
    {
        if (group == null)
        {
            this.module.getLog().warn(new IllegalArgumentException(), "The group name should never be null!");
            return false;
        }
        User user = roles.getCore().getUserManager().findExactUser(player);
        if (user == null)
        {
            return false;
        }
        World world;
        if (worldName == null)
        {
            world = user.getWorld();
        }
        else
        {
            world = this.module.getCore().getWorldManager().getWorld(worldName);
        }
        if (world == null)
        {
            return false;
        }
        RolesAttachment attachment = user.get(RolesAttachment.class);
        return attachment.getDataHolder(world).getRawRoles().contains(group);
    }

    @Override
    public boolean playerAddGroup(String worldName, String player, String group)
    {
        if (group == null)
        {
            this.module.getLog().warn(new IllegalArgumentException(), "The group name should never be null!");
            return false;
        }
        User user = roles.getCore().getUserManager().findExactUser(player);
        if (user == null)
        {
            return false;
        }
        World world;
        if (worldName == null)
        {
            world = user.getWorld();
        }
        else
        {
            world = this.module.getCore().getWorldManager().getWorld(worldName);
        }
        if (world == null)
        {
            return false;
        }
        Role role = roles.getRolesManager().getProvider(world).getRole(group);
        if (role == null)
        {
            return false;
        }
        RolesAttachment attachment = user.get(RolesAttachment.class);
        attachment.getDataHolder(world).assignRole(role);
        attachment.getCurrentDataHolder().apply();
        return true;
    }

    @Override
    public boolean playerRemoveGroup(String worldName, String player, String group)
    {
        if (group == null)
        {
            this.module.getLog().warn(new IllegalArgumentException(), "The group name should never be null!");
            return false;
        }
        User user = roles.getCore().getUserManager().findExactUser(player);
        if (user == null)
        {
            return false;
        }
        World world;
        if (worldName == null)
        {
            world = user.getWorld();
        }
        else
        {
            world = this.module.getCore().getWorldManager().getWorld(worldName);
        }
        if (world == null)
        {
            return false;
        }
        Role role = roles.getRolesManager().getProvider(world).getRole(group);
        if (role == null)
        {
            return false;
        }
        RolesAttachment attachment = user.get(RolesAttachment.class);
        attachment.getDataHolder(world).removeRole(role);
        attachment.getCurrentDataHolder().apply();
        return true;
    }

    @Override
    public String[] getPlayerGroups(String worldName, String player)
    {
        User user = roles.getCore().getUserManager().findExactUser(player);
        if (user == null)
        {
            return null;
        }
        World world;
        if (worldName == null)
        {
            world = user.getWorld();
        }
        else
        {
            world = this.module.getCore().getWorldManager().getWorld(worldName);
        }
        if (world == null)
        {
            return null;
        }
        Set<String> roles = new HashSet<>();
        RolesAttachment attachment = user.get(RolesAttachment.class);
        for (Role role : attachment.getDataHolder(world).getRoles())
        {
            roles.add(role.getName());
        }
        return roles.toArray(new String[roles.size()]);
    }

    @Override
    public String getPrimaryGroup(String worldName, String player)
    {
        User user = roles.getCore().getUserManager().findExactUser(player);
        if (user == null)
        {
            return null;
        }
        World world;
        if (worldName == null)
        {
            world = user.getWorld();
        }
        else
        {
            world = this.module.getCore().getWorldManager().getWorld(worldName);
        }
        if (world == null)
        {
            return null;
        }
        RolesAttachment attachment = user.get(RolesAttachment.class);
        return attachment.getDominantRole(world).getName();
    }

    @Override
    public String[] getGroups()
    {
        Set<String> roles = new HashSet<>();
        for (World world : wm.getWorlds())
        {
            RoleProvider provider = this.roles.getRolesManager().getProvider(world);
            for (Role role : provider.getRoles())
            {
                roles.add(role.getName());
            }
        }
        return roles.toArray(new String[roles.size()]);
    }

    @Override
    public boolean hasGroupSupport()
    {
        return true;
    }
}
