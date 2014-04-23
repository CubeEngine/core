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
package de.cubeisland.engine.hide;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.module.Reloadable;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.hide.event.UserHideEvent;
import de.cubeisland.engine.hide.event.UserShowEvent;
import org.dynmap.DynmapAPI;

public class Hide extends Module implements Reloadable
{
    private HideConfig config;
    private Set<UUID> hiddenUsers;
    private Set<UUID> canSeeHiddens;

    public HidePerm perms()
    {
        return perms;
    }

    private HidePerm perms;

    @Override
    public void onEnable()
    {
        hiddenUsers = new HashSet<>();
        canSeeHiddens = new HashSet<>();
        this.getCore().getCommandManager().registerCommands(this, new HideCommands(this));
        this.getCore().getEventManager().registerListener(this, new HideListener(this));

        this.perms = new HidePerm(this);

        Plugin p = Bukkit.getPluginManager().getPlugin("dynmap");
        if (p != null && p.isEnabled() && p instanceof DynmapAPI)
        {
            getCore().getEventManager().registerListener(this, new DynmapListener((DynmapAPI)p));
        }
    }

    @Override
    public void onDisable()
    {
        this.canSeeHiddens.clear();
        Set<User> onlineUsers = getCore().getUserManager().getOnlineUsers();
        for (UUID hiddenId : hiddenUsers)
        {
            User hidden = getCore().getUserManager().getExactUser(hiddenId);
            for (User user : onlineUsers)
            {
                user.showPlayer(hidden);
            }
        }
        this.hiddenUsers.clear();
    }

    @Override
    public void reload()
    {
        this.onDisable();
    }

    public void hidePlayer(final User user)
    {
        this.hiddenUsers.add(user.getUniqueId());

        getCore().getEventManager().fireEvent(new UserHideEvent(user.getCore(), user));

        for (User onlineUser : getCore().getUserManager().getOnlineUsers())
        {
            if (!this.canSeeHiddens.contains(onlineUser.getUniqueId()))
            {
                onlineUser.hidePlayer(user);
            }
        }

        for (UUID hiddenId : this.hiddenUsers)
        {
            User hiddenUser = getCore().getUserManager().getExactUser(hiddenId);
            if (hiddenUser != user && !this.canSeeHiddens.contains(hiddenId))
            {
                hiddenUser.hidePlayer(user);
            }
        }
    }

    public void showPlayer(final User user)
    {
        this.hiddenUsers.remove(user.getUniqueId());

        getCore().getEventManager().fireEvent(new UserShowEvent(user.getCore(), user));

        for (User onlineUser : getCore().getUserManager().getOnlineUsers())
        {
            if (!this.canSeeHiddens.contains(onlineUser.getUniqueId()))
            {
                onlineUser.showPlayer(user);
            }
        }

        for (UUID hiddenId : this.hiddenUsers)
        {
            User hiddenUser = getCore().getUserManager().getExactUser(hiddenId);
            if (hiddenUser != user && !this.canSeeHiddens.contains(hiddenId))
            {
                hiddenUser.showPlayer(user);
            }
        }
    }

    public HideConfig getConfig()
    {
        return config;
    }

    public Set<UUID> getHiddenUsers()
    {
        return hiddenUsers;
    }

    public Set<UUID> getCanSeeHiddens()
    {
        return canSeeHiddens;
    }

    public boolean isHidden(User user)
    {
        return this.hiddenUsers.contains(user.getUniqueId());
    }

    public boolean canSeeHiddens(User user)
    {
        return this.canSeeHiddens.contains(user.getUniqueId());
    }

    public boolean toggleCanSeeHiddens(User user)
    {
        if (canSeeHiddens(user))
        {
            for (UUID hiddenName : hiddenUsers)
            {
                user.hidePlayer(getCore().getUserManager().getExactUser(hiddenName));
            }
            canSeeHiddens.remove(user.getUniqueId());
            return false;
        }
        else
        {
            for (UUID hiddenName : hiddenUsers)
            {
                user.showPlayer(getCore().getUserManager().getExactUser(hiddenName));
            }
            canSeeHiddens.add(user.getUniqueId());
            return true;
        }
    }
}
