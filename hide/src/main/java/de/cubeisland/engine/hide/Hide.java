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
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.module.Reloadable;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.hide.event.UserHideEvent;
import de.cubeisland.engine.hide.event.UserShowEvent;

public class Hide extends Module implements Reloadable
{
    private HideConfig config;
    private Set<String> hiddenUsers;
    private Set<String> canSeeHiddens;
    private HidePerm perm;

    @Override
    public void onEnable()
    {
        hiddenUsers = new HashSet<>();
        canSeeHiddens = new HashSet<>();
        this.getCore().getCommandManager().registerCommands(this, new HideCommands(this));
        this.getCore().getEventManager().registerListener(this, new HideListener(this));

        this.perm = new HidePerm(this);
        // TODO player listing in basics?
    }

    @Override
    public void onDisable()
    {
        this.canSeeHiddens.clear();
        Set<User> onlineUsers = getCore().getUserManager().getOnlineUsers();
        for (String hiddenName : hiddenUsers)
        {
            User hidden = getCore().getUserManager().getExactUser(hiddenName);
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
        this.hiddenUsers.add(user.getName());

        getCore().getEventManager().fireEvent(new UserHideEvent(user.getCore(), user));

        for (User onlineUser : getCore().getUserManager().getOnlineUsers())
        {
            if (!this.canSeeHiddens.contains(onlineUser.getName()))
            {
                onlineUser.hidePlayer(user);
            }
        }

        for (String hiddenUserName : this.hiddenUsers)
        {
            User hiddenUser = getCore().getUserManager().getExactUser(hiddenUserName);
            if (hiddenUser != user && !this.canSeeHiddens.contains(hiddenUserName))
            {
                hiddenUser.hidePlayer(user);
            }
        }
    }

    public void showPlayer(final User user)
    {
        this.hiddenUsers.remove(user.getName());

        getCore().getEventManager().fireEvent(new UserShowEvent(user.getCore(), user));

        for (User onlineUser : getCore().getUserManager().getOnlineUsers())
        {
            if (!this.canSeeHiddens.contains(onlineUser.getName()))
            {
                onlineUser.showPlayer(user);
            }
        }

        for (String hiddenUserName : this.hiddenUsers)
        {
            User hiddenUser = getCore().getUserManager().getExactUser(hiddenUserName);
            if (hiddenUser != user && !this.canSeeHiddens.contains(hiddenUserName))
            {
                hiddenUser.showPlayer(user);
            }
        }
    }

    public HideConfig getConfig()
    {
        return config;
    }

    public Set<String> getHiddenUsers()
    {
        return hiddenUsers;
    }

    public Set<String> getCanSeeHiddens()
    {
        return canSeeHiddens;
    }

    public boolean isHidden(User user)
    {
        return this.hiddenUsers.contains(user.getName());
    }

    public boolean canSeeHiddens(User user)
    {
        return this.canSeeHiddens.contains(user.getName());
    }

    public boolean toggleCanSeeHiddens(User user)
    {
        if (canSeeHiddens(user))
        {
            for (String hiddenName : hiddenUsers)
            {
                user.hidePlayer(getCore().getUserManager().getExactUser(hiddenName));
            }
            canSeeHiddens.remove(user.getName());
            return false;
        }
        else
        {
            for (String hiddenName : hiddenUsers)
            {
                user.showPlayer(getCore().getUserManager().getExactUser(hiddenName));
            }
            canSeeHiddens.add(user.getName());
            return true;
        }
    }
}
