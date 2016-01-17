package org.cubeengine.service.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

public class CommandLogging
{
    private static CommandLogFilter commandFilter = null;

    public static void disable()
    {
        Logger logger = (Logger)LogManager.getLogger("Minecraft");
        if (commandFilter == null)
        {
            commandFilter = new CommandLogFilter(); // TODO configurable filter
        }
        logger.addFilter(commandFilter);
    }

    public static synchronized void reset()
    {
        if (commandFilter != null)
        {
            Logger logger = (Logger)LogManager.getLogger("Minecraft");
            logger.getContext().removeFilter(commandFilter);
        }
    }

}
