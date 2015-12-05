package org.cubeengine.service.command.exception;

import de.cubeisland.engine.logscribe.Log;
import org.cubeengine.butler.CommandBase;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.exception.PriorityExceptionHandler;
import org.cubeengine.service.i18n.I18n;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextBuilder;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.command.CommandSource;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

import static org.cubeengine.service.i18n.formatter.MessageType.CRITICAL;
import static org.spongepowered.api.text.action.TextActions.showText;
import static org.spongepowered.api.text.format.TextColors.*;
import static org.spongepowered.api.text.format.TextColors.GRAY;

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
        TextBuilder stackTrace = Texts.builder();
        for (StackTraceElement element : r.getStackTrace())
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
        Text hover = Texts.builder().append(Texts.of(GRAY, r.getClass().getName(), ": ", r.getMessage())).onHover(showText(stackTrace.build())).build();
        sender.sendMessage(Texts.of(Texts.of(i18n.getTranslation(sender, CRITICAL, "Unexpected command failure:")), " ", hover));
        return true;
    }

    @Override
    public int priority()
    {
        return Integer.MAX_VALUE;
    }
}
