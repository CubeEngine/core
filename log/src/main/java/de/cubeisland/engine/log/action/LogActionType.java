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

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import org.bukkit.event.Listener;

import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.log.storage.LogEntry;
import de.cubeisland.engine.log.storage.QueryParameter;
import de.cubeisland.engine.log.storage.ShowParameter;

public abstract class LogActionType extends ActionType implements Listener
{
    private static final SimpleDateFormat timeOnly = new SimpleDateFormat("HH:mm:ss");
    private static final SimpleDateFormat dateOnly = new SimpleDateFormat("yyyy-MM-dd");

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
            if (logEntry.hasAttached())
            {
                Timestamp first = logEntry.getTimestamp();
                Timestamp last = logEntry.getAttached().last().getTimestamp();
                if (first.getTime() > last.getTime())
                {
                    first = last;
                    last = logEntry.getTimestamp();
                }
                String fDate = dateOnly.format(first);
                if (dateOnly.format(last).equals(fDate)) // Same day
                {
                    time = "&7" + fDate + " " + timeOnly.format(first) + " &6-&7 " + timeOnly.format(last) + " - ";
                }
                else
                {
                    time = "&7" + fDate + " " + timeOnly.format(first) +
                        " &6-&7 " + dateOnly.format(last) + " " + timeOnly.format(last) + " - ";
                }
            }
            else
            {
                time = "&7"+ dateOnly.format(logEntry.getTimestamp()) + " " +
                    timeOnly.format(logEntry.getTimestamp()) + " - ";
            }
        }
        String loc = "";
        if (show.showCoords)
        {
            if (logEntry.hasAttached())
            {
                int xMin = logEntry.getVector().x;
                int yMin = logEntry.getVector().y;
                int zMin = logEntry.getVector().z;
                int xMax = logEntry.getVector().x;
                int yMax = logEntry.getVector().y;
                int zMax = logEntry.getVector().z;
                for (LogEntry entry : logEntry.getAttached())
                {
                    if (entry.getVector().x < xMin)
                    {
                        xMin = entry.getVector().x;
                    }
                    else if (entry.getVector().x > xMax)
                    {
                        xMax = entry.getVector().x;
                    }
                    if (entry.getVector().y < yMin)
                    {
                        yMin = entry.getVector().y;
                    }
                    else if (entry.getVector().y > yMax)
                    {
                        yMax = entry.getVector().y;
                    }
                    if (entry.getVector().z < zMin)
                    {
                        zMin = entry.getVector().z;
                    }
                    else if (entry.getVector().z > zMax)
                    {
                        zMax = entry.getVector().z;
                    }
                }
                if (xMax == xMin && yMax == yMin && zMax == zMin)
                {
                    loc = "\n&a   at &3%s&f:&3%d&f:&3%d&f:&3%d&a";
                    loc = String.format(loc,logEntry.getWorld().getName(),xMax,yMax,zMax);
                }
                else
                {
                    loc = "\n&a   in between &3%d&f:&3%d&f:&3%d&a and &3%d&f:&3%d&f:&3%d&a in &3%s";
                    loc = String.format(loc,xMin, yMin, zMin, xMax,yMax,zMax,logEntry.getWorld().getName());
                }
            }
            else
            {
                loc = "\n&a   at &3%s&f:&3%d&f:&3%d&f:&3%d&a";
                loc = String.format(loc,logEntry.getWorld().getName(),logEntry.getVector().x,logEntry.getVector().y,logEntry.getVector().z);
            }
        }
        this.showLogEntry(user,logEntry,time,loc);
    }

    @Override
    public boolean canRollback()
    {
        return true;
    }

    @Override
    public boolean needsModel()
    {
        return true;
    }
}
