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
package de.cubeisland.cubeengine.core.command.result.paginated;


import de.cubeisland.engine.core.command.CommandContext;
import de.cubeisland.engine.core.command.CommandHolder;
import de.cubeisland.engine.core.command.CubeCommand;
import de.cubeisland.engine.core.command.reflected.ReflectedCommand;
import de.cubeisland.engine.core.util.formatter.MessageType;

public class PaginationCommands implements CommandHolder
{
    private PaginationManager pgManager;

    public Class<? extends CubeCommand> getCommandType()
    {
        return ReflectedCommand.class;
    }

    public void next(CommandContext context)
    {
        if (!this.pgManager.hasResult(context.getSender()))
        {
            context.sendTranslated(MessageType.NEGATIVE, "You don't have any results to show!");
            return;
        }
        context.sendTranslated(MessageType.NONE, PaginationManager.HEADER);
        for (String line : pgManager.getNextPage(context.getSender()))
        {
            context.sendMessage(line);
        }
        context.sendTranslated(MessageType.NONE, PaginationManager.FOOTER);
    }

    public void prev(CommandContext context)
    {
        if (!this.pgManager.hasResult(context.getSender()))
        {
            context.sendTranslated(MessageType.NEGATIVE, "You don't have any results to show!");
            return;
        }
        context.sendTranslated(MessageType.NONE, PaginationManager.HEADER);
        for (String line : pgManager.getPrevPage(context.getSender()))
        {
            context.sendMessage(line);
        }
        context.sendTranslated(MessageType.NONE, PaginationManager.FOOTER);
    }
}
