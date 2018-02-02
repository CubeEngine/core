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
import org.cubeengine.logscribe.Log;
import org.cubeengine.butler.CommandBase;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.exception.PriorityExceptionHandler;
import org.cubeengine.libcube.service.i18n.I18n;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import static org.cubeengine.libcube.service.i18n.formatter.MessageType.CRITICAL;
import static org.spongepowered.api.text.action.TextActions.showText;
import static org.spongepowered.api.text.format.TextColors.*;

public class UnknownExceptionHandler implements PriorityExceptionHandler
{
    private final Log logger;
    private final I18n i18n;

    public UnknownExceptionHandler(Log log, I18n i18n)
    {
        this.logger = log;
        this.i18n = i18n;
    }

    @Override
    public boolean handleException(Throwable r, CommandBase command, CommandInvocation invocation)
    {
        if (r instanceof InvocationTargetException || r instanceof ExecutionException)
        {
            r = r.getCause();
        }

        CommandSource sender = (CommandSource) invocation.getCommandSource();

        logger.error(r, "Unexpected Command Exception: " + r.getMessage()
                + " - " + invocation.getCommandLine());
        Text.Builder stackTrace = Text.builder();
        for (StackTraceElement element : r.getStackTrace())
        {
            String[] parts = element.toString().split("\\(");
            parts[1] = parts[1].replace(")", "");
            boolean our = parts[0].startsWith("de.cubeisland") || parts[0].startsWith("org.cubeengine");
            String[] lineParts = parts[1].split(":");
            Text.Builder lineBuilder = Text.builder().append(Text.of(our ? GOLD : GRAY, lineParts[0]));
            if (lineParts.length == 2)
            {
                lineBuilder.append(Text.of(WHITE, ":", AQUA, lineParts[1]));
            }
            Text line = Text.of(YELLOW, "(", lineBuilder.build(), YELLOW, ")");
            stackTrace.append(Text.of(DARK_GRAY, "at ", Text.of(our ? GOLD : GRAY, parts[0], line), "\n"));
        }
        Text hover = Text.builder().append(Text.of(GRAY, r.getClass().getName(), ": ", r.getMessage())).onHover(showText(stackTrace.build())).build();
        sender.sendMessage(Text.of(Text.of(i18n.translate(sender, CRITICAL, "Unexpected command failure:")), " ", hover));
        return true;
    }

    @Override
    public int priority()
    {
        return Integer.MAX_VALUE;
    }
}
