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
package de.cubeisland.engine.core.command;

import de.cubeisland.engine.command.result.CommandResult;
import de.cubeisland.engine.core.command.context.CubeContext;
import de.cubeisland.engine.core.command.context.CubeContextFactory;
import de.cubeisland.engine.command.exception.CommandException;
import de.cubeisland.engine.core.command.exception.PermissionDeniedException;

import static de.cubeisland.engine.core.util.ChatFormat.GREY;
import static de.cubeisland.engine.core.util.ChatFormat.WHITE;
import static de.cubeisland.engine.core.util.ChatFormat.YELLOW;
import static de.cubeisland.engine.core.util.formatter.MessageType.NEUTRAL;
import static de.cubeisland.engine.core.util.formatter.MessageType.NONE;

public class HelpCommand extends CubeCommand
{
    protected CubeCommand target;

    public HelpCommand(CubeCommand target)
    {
        super(target.module, "?", "Displays Help", new CubeContextFactory());
        this.target = target;
    }

    @Override
    public CubeContextFactory getContextFactory()
    {
        return (CubeContextFactory)target.getContextFactory();
    }

    @Override
    public void checkContext(CubeContext ctx) throws CommandException
    {
        if (this.target.isCheckperm() && !this.target.isAuthorized(ctx.getSource()))
        {
            throw new PermissionDeniedException(this.target.getPermission());
        }
    }



    @Override
    public CommandResult run(CubeContext context)
    {
        context.sendTranslated(NONE, "{text:Description:color=GREY}: {input}", target.getDescription());
        context.sendTranslated(NONE, "{text:Usage:color=GREY}: {input}", target.getUsage(context));

        if (this.hasChildren())
        {
            context.sendMessage(" ");
            context.sendTranslated(NEUTRAL, "The following subcommands are available:");
            context.sendMessage(" ");

            final CommandSender sender = context.getSource();
            for (CubeCommand command : target.getChildren())
            {
                if (command == this)
                {
                    continue;
                }
                if (!command.isCheckperm() || command.isAuthorized(sender))
                {
                    context.sendMessage(YELLOW + command.getName() + WHITE + ": " + GREY + sender.getTranslation(NONE, command.getDescription()));
                }
            }
        }
        context.sendMessage(" ");
        context.sendTranslated(NONE, "{text:Detailed help:color=GREY}: {input#link:color=INDIGO}", "http://engine.cubeisland.de/c/" + target.getModule().getId() + "/" + target.implodeCommandParentNames("/"));
        return null;
    }
}
