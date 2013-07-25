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
package de.cubeisland.engine.log.action;

import org.bukkit.event.Listener;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.storage.LogEntry;
import de.cubeisland.engine.log.storage.QueryParameter;
import de.cubeisland.engine.log.storage.ShowParameter;

public abstract class LogActionType extends ActionType implements Listener
{
    @Override
    public void enable()
    {
        this.logModule.getCore().getEventManager().registerListener(this.logModule,this);
    }

    protected abstract void showLogEntry(User user, LogEntry logEntry, String time, String loc);

    @Override
    public void showLogEntry(User user, QueryParameter params, LogEntry logEntry, ShowParameter show)
    {
        String time = "";
        if (show.showDate)
        {
            time = "&7"+ logEntry.timestamp.toString() + " - ";
            //TODO time-frame if attached
        }
        String loc = "";
        if (show.showCoords)
        {
            if (logEntry.hasAttached())
            {
                int xMin = logEntry.location.x;
                int yMin = logEntry.location.y;
                int zMin = logEntry.location.z;
                int xMax = logEntry.location.x;
                int yMax = logEntry.location.y;
                int zMax = logEntry.location.z;
                for (LogEntry entry : logEntry.getAttached())
                {
                    if (entry.location.x < xMin)
                    {
                        xMin = entry.location.x;
                    }
                    else if (entry.location.x > xMax)
                    {
                        xMax = entry.location.x;
                    }
                    if (entry.location.y < yMin)
                    {
                        yMin = entry.location.y;
                    }
                    else if (entry.location.y > yMax)
                    {
                        yMax = entry.location.y;
                    }
                    if (entry.location.z < zMin)
                    {
                        zMin = entry.location.z;
                    }
                    else if (entry.location.z > zMax)
                    {
                        zMax = entry.location.z;
                    }
                }
                if (xMax == xMin && yMax == yMin && zMax == zMin)
                {
                    loc = "\n&a   at &3%s&f:&3%d&f:&3%d&f:&3%d&a";
                    loc = String.format(loc,logEntry.world.getName(),xMax,yMax,zMax);
                }
                else
                {
                    loc = "\n&a   in between &3%d&f:&3%d&f:&3%d&a and &3%d&f:&3%d&f:&3%d&a in &3%s";
                    loc = String.format(loc,xMin, yMin, zMin, xMax,yMax,zMax,logEntry.world.getName());
                }
            }
            else
            {
                loc = "\n&a   at &3%s&f:&3%d&f:&3%d&f:&3%d&a";
                loc = String.format(loc,logEntry.world.getName(),logEntry.location.x,logEntry.location.y,logEntry.location.z);
            }
        }
        this.showLogEntry(user,logEntry,time,loc);
    }

    @Override
    public boolean canRollback()
    {
        return true;
    }
}
