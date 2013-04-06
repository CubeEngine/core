package de.cubeisland.cubeengine.log.action.logaction;


import org.bukkit.World;

import de.cubeisland.cubeengine.core.user.User;
import de.cubeisland.cubeengine.log.Log;
import de.cubeisland.cubeengine.log.action.LogActionType;
import de.cubeisland.cubeengine.log.storage.LogEntry;

public class ActionTypeContainer extends LogActionType
{
    private String name;
    public ActionTypeContainer(Log module, String name)
    {
        super(module, false);
        this.setID(-1);
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    protected void showLogEntry(User user, LogEntry logEntry, String time, String loc)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSimilar(LogEntry logEntry, LogEntry other)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isActive(World world)
    {
        throw new UnsupportedOperationException();
    }
}
