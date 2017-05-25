/*
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
package org.cubeengine.libcube.service.command.exception;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;
import de.cubeisland.engine.logscribe.Log;
import org.cubeengine.butler.CommandBase;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.exception.CommandException;
import org.cubeengine.butler.exception.PriorityExceptionHandler;
import org.cubeengine.butler.exception.SilentException;
import org.cubeengine.butler.filter.RestrictedSourceException;
import org.cubeengine.butler.parameter.TooFewArgumentsException;
import org.cubeengine.butler.parameter.TooManyArgumentsException;
import org.cubeengine.butler.parameter.argument.ParserException;
import org.cubeengine.libcube.service.i18n.I18n;
import org.spongepowered.api.command.CommandSource;

import static org.cubeengine.libcube.service.i18n.formatter.MessageType.NEGATIVE;
import static org.cubeengine.libcube.service.i18n.formatter.MessageType.NEUTRAL;

public class CommandExceptionHandler implements PriorityExceptionHandler
{
    private final Log logger;
    private final I18n i18n;

    public CommandExceptionHandler(Log log, I18n i18n)
    {
        this.logger = log;
        this.i18n = i18n;
    }

    @Override
    public boolean handleException(Throwable t, CommandBase command, CommandInvocation invocation)
    {
        if (t instanceof InvocationTargetException || t instanceof ExecutionException)
        {
            t = t.getCause();
        }

        CommandSource sender = (CommandSource)invocation.getCommandSource();
        if (!(t instanceof CommandException))
        {
            return false;
        }
        logger.debug("Command failed: {}: {}", t.getClass(), t.getMessage());
        if (t instanceof PermissionDeniedException)
        {
            PermissionDeniedException e = (PermissionDeniedException)t;
            if (e.getMessage() != null)
            {
                i18n.sendTranslated(sender, NEGATIVE, e.getMessage(), e.getArgs());
            }
            else
            {
                i18n.sendTranslated(sender, NEGATIVE, "You're not allowed to do this!");
                i18n.sendTranslated(sender, NEGATIVE, "Contact an administrator if you think this is a mistake!");
            }
            i18n.sendTranslated(sender, NEGATIVE, "Missing permission: {name}", e.getPermission().getName());
        }
        else if (t instanceof TooFewArgumentsException)
        {
            i18n.sendTranslated(sender, NEGATIVE, "You've given too few arguments.");
            i18n.sendTranslated(sender, NEUTRAL, "Proper usage: {input#usage}", command.getDescriptor().getUsage(invocation));
        }
        else if (t instanceof TooManyArgumentsException)
        {
            i18n.sendTranslated(sender, NEGATIVE, "You've given too many arguments.");
            i18n.sendTranslated(sender, NEUTRAL, "Proper usage: {input#usage}", command.getDescriptor().getUsage(invocation));
        }
        else if (t instanceof ParserException)
        {
            i18n.sendTranslated(sender, NEGATIVE, t.getMessage(), ((ParserException)t).getArgs());
        }
        else if (t instanceof RestrictedSourceException)
        {
            // TODO handle Restriction when its not for CommandSource (maybe programming error)
            i18n.sendTranslated(sender, NEGATIVE, "You cannot execute this command!");
            if (t.getMessage() != null)
            {
                i18n.sendTranslated(sender, NEUTRAL, t.getMessage());
            }
        }
        else if (t instanceof SilentException)
        {
            // do nothing
        }
        else // Unknown cmd exception. Handle later
        {
            return false;
        }
        return true;
    }

    @Override
    public int priority()
    {
        return 0;
    }
}
