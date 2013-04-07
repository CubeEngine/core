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
package de.cubeisland.cubeengine.basics.command.teleport;

import de.cubeisland.cubeengine.basics.Basics;
import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.core.user.UserEvent;
import org.bukkit.Location;
import org.bukkit.event.HandlerList;

public class SpawnCommandEvent extends UserEvent
{
    public SpawnCommandEvent(Basics basics, User user, Location loc)
    {
        super(basics.getCore(), user);
        this.basics = basics;
        this.loc = loc;
    }

    private Location loc;
    private final Basics basics;
    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    /**
     * @return the loc
     */
    public Location getLoc()
    {
        return loc;
    }

    /**
     * @param loc the loc to set
     */
    public void setLoc(Location loc)
    {
        this.loc = loc;
    }

    /**
     * @return the basics
     */
    public Basics getBasics()
    {
        return basics;
    }
}
