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

    private final Command command;
    private final String commandLine;

    public CommandExecuteEvent(Core core, Command command, String commandLine)
    {
        super(core);
        this.command = command;
        this.commandLine = commandLine;
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
     * Returns the command line
     *
     * @return the command line
     */
    public String getCommandLine()
    {
        return this.commandLine;
    }
}
