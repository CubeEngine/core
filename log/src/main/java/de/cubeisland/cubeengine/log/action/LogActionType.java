package de.cubeisland.cubeengine.log.action;

import org.bukkit.event.Listener;
import de.cubeisland.cubeengine.log.Log;

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
}
