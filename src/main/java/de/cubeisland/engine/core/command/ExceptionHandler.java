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

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

import de.cubeisland.engine.command.CommandBase;
import de.cubeisland.engine.command.CommandException;
import de.cubeisland.engine.command.CommandInvocation;
import de.cubeisland.engine.command.filter.RestrictedSourceException;
import de.cubeisland.engine.command.parameter.TooFewArgumentsException;
import de.cubeisland.engine.command.parameter.TooManyArgumentsException;
import de.cubeisland.engine.command.parameter.reader.ReaderException;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.command.exception.PermissionDeniedException;

import static de.cubeisland.engine.core.util.formatter.MessageType.CRITICAL;
import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;
import static de.cubeisland.engine.core.util.formatter.MessageType.NEUTRAL;

public class ExceptionHandler extends de.cubeisland.engine.command.ExceptionHandler
{
    private Core core;

    public ExceptionHandler(Core core)
    {
        this.core = core;
    }

    @Override
    public void handleException(Throwable t, CommandBase command, CommandInvocation invocation)
    {
        if (!(invocation.getCommandSource() instanceof CommandSender))
        {
            core.getLog().info("An unknown CommandSource ({}) caused an exception: {}",
                               invocation.getCommandSource().getClass().getName(), t.getMessage());
            return;
        }

        if (t instanceof InvocationTargetException || t instanceof ExecutionException)
        {
            t = t.getCause();
        }

        CommandSender sender = (CommandSender)invocation.getCommandSource();
        if (t instanceof CommandException)
        {
            core.getLog().debug("Command failed: {}: {}", t.getClass(), t.getMessage());
            if (t instanceof PermissionDeniedException)
            {
                PermissionDeniedException e = (PermissionDeniedException)t;
                if (e.getMessage() != null)
                {
                    sender.sendTranslated(NEGATIVE, e.getMessage(), e.getArgs());
                }
                else
                {
                    sender.sendTranslated(NEGATIVE, "You're not allowed to do this!");
                    sender.sendTranslated(NEGATIVE, "Contact an administrator if you think this is a mistake!");
                }
                sender.sendTranslated(NEGATIVE, "Missing permission: {name}", e.getPermission().getName());
            }
            else if (t instanceof TooFewArgumentsException)
            {
                sender.sendTranslated(NEGATIVE, "You've given too few arguments.");
                sender.sendTranslated(NEUTRAL, "Proper usage: {input#usage}", command.getDescriptor().getUsage(invocation));

            }
            else if (t instanceof TooManyArgumentsException)
            {
                sender.sendTranslated(NEGATIVE, "You've given too many arguments.");
                sender.sendTranslated(NEUTRAL, "Proper usage: {input#usage}", command.getDescriptor().getUsage(invocation));
            }
            else if (t instanceof ReaderException)
            {
                sender.sendTranslated(NEGATIVE, t.getMessage(), ((ReaderException)t).getArgs());
            }
            else if (t instanceof RestrictedSourceException)
            {
                sender.sendTranslated(NEGATIVE, "You cannot execute this command!");
                if (t.getMessage() != null)
                {
                    sender.sendTranslated(NEUTRAL, t.getMessage());
                }
            }
            else
            {
                sender.sendTranslated(NEGATIVE, "Command failure: {input}: {input}", t.getClass().getName(), String.valueOf(t.getMessage()));
            }
        }
        else
        {
            core.getLog().error(t, "Unexpected Command Exception: " + t.getMessage());
            sender.sendTranslated(CRITICAL, "Unexpected command failure: {text}", t.getMessage());
        }
    }

    /*
    // TODO handle in cmd via property
    private void handleCommandException(@NotNull final CommandBase command, @NotNull final CommandInvocation invocation, Throwable t)
    {
        final CommandSource source = invocation.getCommandSource();
        final Module module = command.getDescriptor().valueFor(ModuleProvider.class);
        if (!(source instanceof CommandSender))
        {
            module.getLog().info("An unknown CommandSource ({}) caused an exception: {}", source.getClass().getName(), t.getMessage());
            return;
        }
        final CommandSender sender = (CommandSender)source;
        if (!CubeEngine.isMainThread())
        {
            final Throwable tmp = t;
            module.getCore().getTaskManager().callSync(new Callable<Void>()
            {
                @Override
                public Void call() throws Exception
                {
                    handleCommandException(command, invocation, tmp);
                    return null;
                }
            });
            return;
        }
        if (t instanceof InvocationTargetException || t instanceof ExecutionException)
        {
            t = t.getCause();
        }
        if (t instanceof MissingParameterException)
        {
            if (t.getMessage().isEmpty())
            {
                sender.sendTranslated(NEGATIVE, "The parameter {name#parameter} is missing!", ((MissingParameterException)t).getParamName());
            }
            else
            {
                sender.sendMessage(t.getMessage());
            }
        }
        else if (t instanceof IncorrectUsageException)
        {
            IncorrectUsageException e = (IncorrectUsageException)t;
            if (e.getMessage() != null)
            {
                sender.sendMessage(t.getMessage());
            }
            else
            {
                sender.sendTranslated(NEGATIVE, "That seems wrong...");
            }
            if (e.getDisplayUsage())
            {
                final String usage;

                CommandDescriptor descriptor = command.getDescriptor();
                usage = descriptor.valueFor(UsageProvider.class).generateUsage(invocation.getCommandSource(), descriptor);

                sender.sendTranslated(MessageType.NEUTRAL, "Proper usage: {message}", usage);
            }
        }
        else if (t instanceof ReaderException)
        {
            ReaderException e = (ReaderException)t;
            if (e.getMessage() != null)
            {
                sender.sendMessage(t.getMessage());
            }
            else
            {
                sender.sendTranslated(NEGATIVE, "Invalid Argument...");
            }
        }
        else if (t instanceof PermissionDeniedException)
        {

        }
        else if (t instanceof IncorrectArgumentException)
        {
            if (((IncorrectArgumentException)t).isNamedArgument())
            {
                sender.sendTranslated(NEGATIVE, "Invalid Argument for {input#named}: {input#reason}", ((IncorrectArgumentException)t).getName(), t.getCause().getMessage());
            }
            else
            {
                sender.sendTranslated(NEGATIVE, "Invalid Argument at {integer#index}: {input#reason}", ((IncorrectArgumentException)t).getIndex(), t.getCause().getMessage());
            }
        }
        else
        {
            sender.sendTranslated(CRITICAL, "An unknown error occurred while executing this command!");
            sender.sendTranslated(CRITICAL, "Please report this error to an administrator.");
            module.getLog().debug(t, t.getLocalizedMessage());
        }
    }
    */
}
