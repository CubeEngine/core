package org.cubeengine.service.command.exception;

import de.cubeisland.engine.logscribe.Log;
import org.cubeengine.butler.*;
import org.cubeengine.butler.exception.PriorityExceptionHandler;
import org.spongepowered.api.util.command.CommandSource;

public class UnknownSourceExceptionHandler implements PriorityExceptionHandler
{
    private Log logger;

    public UnknownSourceExceptionHandler(Log logger)
    {
        this.logger = logger;
    }

    @Override
    public boolean handleException(Throwable e, CommandBase command, CommandInvocation invocation)
    {
        if (!(invocation.getCommandSource() instanceof CommandSource))
        {
           logger.info("An unknown CommandSource ({}) caused an exception: {}",
                    invocation.getCommandSource().getClass().getName(), e.getMessage());
            return true;
        }
        return false;
    }

    @Override
    public int priority()
    {
        return Integer.MIN_VALUE;
    }
}
