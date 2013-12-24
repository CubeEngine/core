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

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.ChatFormat;
import de.cubeisland.engine.hide.event.FakePlayerJoinEvent;
import de.cubeisland.engine.hide.event.FakePlayerQuitEvent;

public class Hide extends Module
{
    private HideConfig config;
    private Set<User> hiddenUsers;
    private Set<User> canSeeHiddens;

    @Override
    public void onEnable()
    {
        this.getCore().getCommandManager().registerCommands(this, new HideCommands(this));
        this.getCore().getEventManager().registerListener(this, new HideListener(this));
        hiddenUsers = new HashSet<>();
        canSeeHiddens = new HashSet<>();
        // TODO player listing in basics?
    }

    public void hidePlayer(final User user, final boolean message)
    {
        this.hiddenUsers.add(user);

//        final PlayerQuitEvent event = new FakePlayerQuitEvent(user.getPlayer(), ChatFormat.YELLOW + user.getName() + " left the game.");
//        getCore().getEventManager().fireEvent(event);
//        if (message)
//        {
//            getCore().getUserManager().broadcastMessage(String.valueOf(event.getQuitMessage()));
//        }

        for (User onlineUser : getCore().getUserManager().getOnlineUsers())
        {
            if (!this.canSeeHiddens.contains(onlineUser))
            {
                onlineUser.hidePlayer(user);
            }
        }

        for (User hiddenUser : this.hiddenUsers)
        {
            if (hiddenUser != user && !this.canSeeHiddens.contains(hiddenUser))
            {
                hiddenUser.hidePlayer(user);
            }
        }
    }

    public void showPlayer(final User user)
    {
        this.hiddenUsers.remove(user);

//        final PlayerJoinEvent event = new FakePlayerJoinEvent(user.getPlayer(), ChatFormat.YELLOW + user.getName() + " joined the game.");
//        getCore().getEventManager().fireEvent(event);
//        final String msg = event.getJoinMessage();
//        if (msg != null)
//        {
//            getCore().getUserManager().broadcastMessage(msg);
//        }

        for (User onlineUser : getCore().getUserManager().getOnlineUsers())
        {
            if (!this.canSeeHiddens.contains(onlineUser))
            {
                onlineUser.showPlayer(user);
            }
        }

        for (User hiddenUsers : this.hiddenUsers)
        {
            if (hiddenUsers != user && !this.canSeeHiddens.contains(hiddenUsers))
            {
                hiddenUsers.showPlayer(user);
            }
        }
    }

    public HideConfig getConfig()
    {
        return config;
    }

    public Set<User> getHiddenUsers()
    {
        return hiddenUsers;
    }

    public Set<User> getCanSeeHiddens()
    {
        return canSeeHiddens;
    }
}
