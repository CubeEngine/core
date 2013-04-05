package de.cubeisland.cubeengine.log.action;

import org.bukkit.event.Listener;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.storage.LogEntry;
import de.cubeisland.cubeengine.log.storage.Lookup;

public abstract class LogActionType extends ActionType implements Listener
{
    public LogActionType(Log module, int id, String name)
    {
        super(module,id, name);
    }

    @Override
    public void initialize()
    {
        this.logModule.getCore().getEventManager().registerListener(this.logModule,this);
    }

    protected abstract void showLogEntry(User user, LogEntry logEntry, String time, String loc);

    @Override
    public void showLogEntry(User user, Lookup lookup, LogEntry logEntry)
    {
        //TODO time OR time-frame if attached
        String time = "{time} - ";
        //TODO location OR area if attached
        String loc = " at {loc}";
        this.showLogEntry(user,logEntry,time,loc);
    }
}
