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
package org.cubeengine.service.command;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;
import de.cubeisland.engine.butler.CommandBase;
import de.cubeisland.engine.butler.CommandException;
import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.SilentException;
import de.cubeisland.engine.butler.filter.RestrictedSourceException;
import de.cubeisland.engine.butler.parameter.TooFewArgumentsException;
import de.cubeisland.engine.butler.parameter.TooManyArgumentsException;
import de.cubeisland.engine.butler.parameter.reader.ReaderException;

import org.cubeengine.service.command.exception.PermissionDeniedException;
import org.cubeengine.module.core.sponge.CoreModule;
import org.cubeengine.service.i18n.formatter.MessageType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextBuilder;
import org.spongepowered.api.text.Texts;

import static org.spongepowered.api.text.action.TextActions.*;
import static org.spongepowered.api.text.format.TextColors.*;

public class ExceptionHandler implements de.cubeisland.engine.butler.ExceptionHandler
{
    private CoreModule core;

    public ExceptionHandler(CoreModule core)
    {
        this.core = core;
    }

    @Override
    public boolean handleException(Throwable t, CommandBase command, CommandInvocation invocation)
    {
        if (!(invocation.getCommandSource() instanceof CommandSender))
        {
            core.getLog().info("An unknown CommandSource ({}) caused an exception: {}",
                               invocation.getCommandSource().getClass().getName(), t.getMessage());
            return true;
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
                    sender.sendTranslated(MessageType.NEGATIVE, e.getMessage(), e.getArgs());
                }
                else
                {
                    sender.sendTranslated(MessageType.NEGATIVE, "You're not allowed to do this!");
                    sender.sendTranslated(MessageType.NEGATIVE, "Contact an administrator if you think this is a mistake!");
                }
                sender.sendTranslated(MessageType.NEGATIVE, "Missing permission: {name}", e.getPermission().getName());
            }
            else if (t instanceof TooFewArgumentsException)
            {
                sender.sendTranslated(MessageType.NEGATIVE, "You've given too few arguments.");
                sender.sendTranslated(MessageType.NEUTRAL, "Proper usage: {input#usage}", command.getDescriptor().getUsage(invocation));

            }
            else if (t instanceof TooManyArgumentsException)
            {
                sender.sendTranslated(MessageType.NEGATIVE, "You've given too many arguments.");
                sender.sendTranslated(MessageType.NEUTRAL, "Proper usage: {input#usage}", command.getDescriptor().getUsage(invocation));
            }
            else if (t instanceof ReaderException)
            {
                sender.sendTranslated(MessageType.NEGATIVE, t.getMessage(), ((ReaderException)t).getArgs());
            }
            else if (t instanceof RestrictedSourceException)
            {
                sender.sendTranslated(MessageType.NEGATIVE, "You cannot execute this command!");
                if (t.getMessage() != null)
                {
                    sender.sendTranslated(MessageType.NEUTRAL, t.getMessage());
                }
            }
            else if (t instanceof SilentException)
            {
                // do nothing
            }
            else
            {
                sender.sendTranslated(MessageType.NEGATIVE, "Command failure: {input}: {input}", t.getClass().getName(), String.valueOf(t.getMessage()));
            }
        }
        else
        {
            core.getLog().error(t, "Unexpected Command Exception: " + t.getMessage());

            TextBuilder stackTrace = Texts.builder();
            for (StackTraceElement element : t.getStackTrace())
            {
                String[] parts = element.toString().split("\\(");
                parts[1] = parts[1].replace(")", "");
                boolean our = parts[0].startsWith("de.cubeisland") || parts[0].startsWith("org.cubeengine");
                String[] lineParts = parts[1].split(":");
                TextBuilder lineBuilder = Texts.builder().append(Texts.of(our ? GOLD : GRAY, lineParts[0]));
                if (lineParts.length == 2)
                {
                    lineBuilder.append(Texts.of(WHITE, ":", AQUA, lineParts[1]));
                }
                Text line = Texts.of(YELLOW, "(", lineBuilder.build(), YELLOW, ")");
                stackTrace.append(Texts.of(DARK_GRAY, "at ", Texts.of(our ? GOLD : GRAY, parts[0], line), "\n"));
            }
            Text hover = Texts.builder().append(Texts.of(GRAY, t.getClass().getName(), ": ", t.getMessage())).onHover(showText(stackTrace.build())).build();
            sender.sendMessage(Texts.of(Texts.of(sender.getTranslation(MessageType.CRITICAL, "Unexpected command failure:")), " ", hover));
        }
        return true;
    }
}
