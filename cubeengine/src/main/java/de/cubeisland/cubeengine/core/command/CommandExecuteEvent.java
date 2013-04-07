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
