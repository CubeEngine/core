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

import org.bukkit.World;

import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.world.WorldManager;
import de.cubeisland.engine.roles.Roles;
import de.cubeisland.engine.roles.role.Role;
import de.cubeisland.engine.roles.role.RolesAttachment;
import de.cubeisland.engine.roles.role.resolved.ResolvedMetadata;
import de.cubeisland.engine.vaultlink.Vaultlink;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;

public class CubeChatService extends Chat
{
    private final Vaultlink module;
    private final Roles roles;
    private final WorldManager wm;

    public CubeChatService(Vaultlink module, Roles roles, Permission perms)
    {
        super(perms);
        this.module = module;
        this.roles = roles;
        this.wm = roles.getCore().getWorldManager();
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

    private ResolvedMetadata getUserMetadata(String worldName, String username, String key)
    {
        User user = roles.getCore().getUserManager().getUser(username);
        if (user == null)
        {
            return null;
        }
        World world = this.module.getCore().getWorldManager().getWorld(worldName);
        if (world == null)
        {
            return null;
        }
        RolesAttachment attachment = user.get(RolesAttachment.class);
        return attachment.getDataHolder(world).getMetadata().get(key);
    }

    private ResolvedMetadata getRoleMetadata(String worldName, String roleName, String key)
    {
        World world = this.module.getCore().getWorldManager().getWorld(worldName);
        if (world == null)
        {
            return null;
        }
        Role role = roles.getRolesManager().getProvider(world).getRole(roleName);
        if (role == null)
        {
            return null;
        }
        return role.getMetadata().get(key);
    }

    @Override
    public String getPlayerPrefix(String world, String player)
    {
        return getPlayerInfoString(world, player, "prefix", "");
    }

    @Override
    public void setPlayerPrefix(String world, String player, String prefix)
    {
        setPlayerInfoString(world, player, "prefix", prefix);
    }

    @Override
    public String getPlayerSuffix(String world, String player)
    {
        return getPlayerInfoString(world, player, "suffix", "");
    }

    @Override
    public void setPlayerSuffix(String world, String player, String suffix)
    {
        setPlayerInfoString(world, player, "suffix", suffix);
    }

    @Override
    public String getGroupPrefix(String world, String group)
    {
        return getGroupInfoString(world, group, "prefix", "");
    }

    @Override
    public void setGroupPrefix(String world, String group, String prefix)
    {
        setGroupInfoString(world, group, "prefix", prefix);
    }

    @Override
    public String getGroupSuffix(String world, String group)
    {
        return getGroupInfoString(world, group, "suffix", "");
    }

    @Override
    public void setGroupSuffix(String world, String group, String suffix)
    {
        setGroupInfoString(world, group, "suffix", suffix);
    }

    @Override
    public int getPlayerInfoInteger(String world, String player, String node, int defaultValue)
    {
        ResolvedMetadata data = getUserMetadata(world, player, node);
        try
        {
            if (data != null)
            {
                return Integer.parseInt(data.getValue());
            }
        }
        catch (NumberFormatException ignore)
        {}
        return defaultValue;
    }

    @Override
    public void setPlayerInfoInteger(String world, String player, String node, int value)
    {
        setPlayerInfoString(world, player, node, String.valueOf(value));
    }

    @Override
    public int getGroupInfoInteger(String world, String group, String node, int defaultValue)
    {
        ResolvedMetadata data = getRoleMetadata(world, group, node);
        try
        {
            if (data != null)
            {
                return Integer.parseInt(data.getValue());
            }
        }
        catch (NumberFormatException ignore)
        {}
        return defaultValue;
    }

    @Override
    public void setGroupInfoInteger(String world, String group, String node, int value)
    {
        setGroupInfoString(world, group, node, String.valueOf(value));
    }

    @Override
    public double getPlayerInfoDouble(String world, String player, String node, double defaultValue)
    {
        ResolvedMetadata data = getUserMetadata(world, player, node);
        try
        {
            if (data != null)
            {
                return Double.parseDouble(data.getValue());
            }
        }
        catch (NumberFormatException ignore)
        {}
        return defaultValue;
    }

    @Override
    public void setPlayerInfoDouble(String world, String player, String node, double value)
    {
        setPlayerInfoString(world, player, node, String.valueOf(value));
    }

    @Override
    public double getGroupInfoDouble(String world, String group, String node, double defaultValue)
    {
        ResolvedMetadata data = getRoleMetadata(world, group, node);
        try
        {
            if (data != null)
            {
                return Double.parseDouble(data.getValue());
            }
        }
        catch (NumberFormatException ignore)
        {}
        return defaultValue;
    }

    @Override
    public void setGroupInfoDouble(String world, String group, String node, double value)
    {
        setGroupInfoString(world, group, node, String.valueOf(value));
    }

    @Override
    public boolean getPlayerInfoBoolean(String world, String player, String node, boolean defaultValue)
    {
        ResolvedMetadata data = getUserMetadata(world, player, node);
        try
        {
            if (data != null)
            {
                return Boolean.parseBoolean(data.getValue());
            }
        }
        catch (NumberFormatException ignore)
        {}
        return defaultValue;
    }

    @Override
    public void setPlayerInfoBoolean(String world, String player, String node, boolean value)
    {
        setPlayerInfoString(world, player, node, String.valueOf(value));
    }

    @Override
    public boolean getGroupInfoBoolean(String world, String group, String node, boolean defaultValue)
    {
        ResolvedMetadata data = getRoleMetadata(world, group, node);
        try
        {
            if (data != null)
            {
                return Boolean.parseBoolean(data.getValue());
            }
        }
        catch (NumberFormatException ignore)
        {}
        return defaultValue;
    }

    @Override
    public void setGroupInfoBoolean(String world, String group, String node, boolean value)
    {
        setGroupInfoString(world, group, node, String.valueOf(value));
    }

    @Override
    public String getPlayerInfoString(String world, String player, String node, String defaultValue)
    {
        ResolvedMetadata data = getUserMetadata(world, player, node);
        if (data == null)
        {
            return defaultValue;
        }
        return data.getValue();
    }

    @Override
    public void setPlayerInfoString(String worldName, String player, String node, String value)
    {
        User user = roles.getCore().getUserManager().getUser(player);
        if (user == null)
        {
            return;
        }
        World world = this.module.getCore().getWorldManager().getWorld(worldName);
        if (world == null)
        {
            return;
        }
        RolesAttachment attachment = user.attachOrGet(RolesAttachment.class, roles);
        attachment.getDataHolder(world).setMetadata(node, value);
        attachment.getCurrentDataHolder().apply();
    }

    @Override
    public String getGroupInfoString(String world, String group, String node, String defaultValue)
    {
        ResolvedMetadata data = getRoleMetadata(world, group, node);
        if (data == null)
        {
            return defaultValue;
        }
        return data.getValue();
    }

    @Override
    public void setGroupInfoString(String worldName, String group, String node, String value)
    {
        World world = this.module.getCore().getWorldManager().getWorld(worldName);
        if (world == null)
        {
            return;
        }
        Role role = roles.getRolesManager().getProvider(world).getRole(group);
        if (role == null)
        {
            return;
        }
        role.setMetadata(node, value);
        role.save();
    }
}
