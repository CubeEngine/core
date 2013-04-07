package de.cubeisland.cubeengine.log.action;

import org.bukkit.event.Listener;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.storage.LogEntry;
import de.cubeisland.cubeengine.log.storage.QueryParameter;

public abstract class LogActionType extends ActionType implements Listener
{
    @Override
    public void enable()
    {
        this.logModule.getCore().getEventManager().registerListener(this.logModule,this);
    }

    protected abstract void showLogEntry(User user, LogEntry logEntry, String time, String loc);

    @Override
    public void showLogEntry(User user, QueryParameter params, LogEntry logEntry)
    {
        //TODO time OR time-frame if attached
        String time = "{time} - ";
        //TODO location OR area if attached
        String loc = "";
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
                if (entry.location.x < yMin)
                {
                    yMin = entry.location.x;
                }
                else if (entry.location.x > yMax)
                {
                    yMax = entry.location.x;
                }
                if (entry.location.x < zMin)
                {
                    zMin = entry.location.x;
                }
                else if (entry.location.x > zMax)
                {
                    zMax = entry.location.x;
                }
            }
            if (xMax == xMin && yMax == yMin && zMax == zMin)
            {
                loc = " &aat &3%d&f:&3%d&f:&3%d&a in &3%s";
                loc = String.format(loc,xMax,yMax,zMax,logEntry.world.getName());
            }
            else
            {
                loc = " &ain between &3%d&f:&3%d&f:&3%d&a and &3%d&f:&3%d&f:&3%d&a in &3%s";
                loc = String.format(loc,xMax,yMax,zMax,logEntry.world.getName());
            }
        }
        else
        {
            loc = " &aat &3%d&f:&3%d&f:&3%d&a in &3%s";
            loc = String.format(loc,logEntry.location.x,logEntry.location.y,logEntry.location.z,logEntry.world.getName());
        }
        this.showLogEntry(user,logEntry,time,loc);
    }

    @Override
    public boolean canRollback()
    {
        return true;
    }
}