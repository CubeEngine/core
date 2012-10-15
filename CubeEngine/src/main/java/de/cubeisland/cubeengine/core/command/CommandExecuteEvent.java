package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.bukkit.CubeEvent;
import org.bukkit.command.Command;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * This event is fired right before a command is executed
 */
public class CommandExecuteEvent extends CubeEvent implements Cancellable
{
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
    
    private Command command;

    public CommandExecuteEvent(Core core, Command command)
    {
        super(core);
        this.command = command;
    }
    
    /**
     * Returns the command of this event
     *
     * @return the command
     */
    public Command getCommand()
    {
        return this.command;
    }

    /**
     * Sets the command to be executed
     *
     * @param command the new command
     */
    public void setCommand(Command command)
    {
        this.command = command;
    }
}
