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

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import de.cubeisland.engine.basics.command.general.DisplayOnlinePlayerListEvent;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.hide.event.FakePlayerJoinEvent;
import de.cubeisland.engine.hide.event.FakePlayerQuitEvent;

public class Hide extends Module
{
    private HideConfig config;
    private Set<String> hiddenPlayers;
    private Set<String> canSeeHiddens;

    @Override
    public void onEnable()
    {
        this.getCore().getCommandManager().registerCommands(this, new HideCommands(this));
        this.getCore().getEventManager().registerListener(this, new HideListener(this));

        if (this.getCore().getModuleManager().getModule("basics") != null)
        {
            this.getCore().getEventManager().registerListener(this, new Listener() {
                @EventHandler
                public void onListPlayers(DisplayOnlinePlayerListEvent event)
                {

                }
            });
        }
    }

    public void hidePlayer(Player player, boolean message)
    {
        final Server server = player.getServer();
        PlayerQuitEvent playerQuit = new FakePlayerQuitEvent(player, ChatColor.YELLOW + player.getName() + " left the game.");
        server.getPluginManager().callEvent(playerQuit);
        if (message)
        {
            server.broadcastMessage(String.valueOf(playerQuit.getQuitMessage()));
        }

        final User user = this.getCore().getUserManager().getUser(player.getName());

        this.hiddenPlayers.add(player.getName());

        for (Player p : server.getOnlinePlayers())
        {
            if (!this.canSeeHiddens.contains(p))
            {
                p.hidePlayer(player);
            }
        }

        Player p;
        for (String playerName : this.hiddenPlayers)
        {
            p = server.getPlayer(playerName);
            if (p != player && !this.canSeeHiddens.contains(playerName))
            {
                p.hidePlayer(player);
            }
        }
    }

    public void showPlayer(Player player)
    {
        final Server server = player.getServer();
        this.hiddenPlayers.remove(player);

        FakePlayerJoinEvent playerJoin = new FakePlayerJoinEvent(player, ChatColor.YELLOW + player.getName() + " joined the game.");
        server.getPluginManager().callEvent(playerJoin);
        final String msg = playerJoin.getJoinMessage();
        if (msg != null)
        {
            server.broadcastMessage(msg);
        }

        for (Player p : server.getOnlinePlayers())
        {
            if (!this.canSeeHiddens.contains(p))
            {
                p.showPlayer(player);
            }
        }

        Player p;
        for (String playerName : this.hiddenPlayers)
        {
            p = server.getPlayer(playerName);
            if (p != player && !this.canSeeHiddens.contains(playerName))
            {
                p.showPlayer(player);
            }
        }
    }

    public HideConfig getConfig()
    {
        return config;
    }

    public Set<String> getHiddenPlayers()
    {
        return hiddenPlayers;
    }

    public Set<String> getCanSeeHiddens()
    {
        return canSeeHiddens;
    }
}
