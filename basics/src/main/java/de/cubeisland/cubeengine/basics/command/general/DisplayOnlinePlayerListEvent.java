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
package de.cubeisland.cubeengine.basics.command.general;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.basics.Basics;

import gnu.trove.map.hash.THashMap;

public class DisplayOnlinePlayerListEvent extends Event implements Cancellable
{
    private final Basics basics;
    private THashMap<User, String> userStrings = new THashMap<User, String>();
    private THashMap<String, List<User>> grouped = new THashMap<String, List<User>>();
    private static final HandlerList handlers = new HandlerList();
    private final CommandSender sender;
    private boolean cancelled = false;

    private List<User> defaultList;

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    public DisplayOnlinePlayerListEvent(Basics basics, CommandSender sender, THashMap<User, String> userStrings, List<User> defaultList)
    {
        this.basics = basics;
        this.sender = sender;
        this.userStrings = userStrings;
        this.grouped.put("&6Players",defaultList);
        this.defaultList = defaultList;
    }

    @Override
    public boolean isCancelled()
    {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean bln)
    {
        this.cancelled = bln;
    }

    /**
     * @return the basics
     */
    public Basics getBasics()
    {
        return basics;
    }

    public THashMap<User, String> getUserStrings()
    {
        return userStrings;
    }

    public THashMap<String, List<User>> getGrouped()
    {
        return grouped;
    }

    public CommandSender getCommandSender()
    {
        return this.sender;
    }

    public List<User> getDefaultList()
    {
        return defaultList;
    }
}
