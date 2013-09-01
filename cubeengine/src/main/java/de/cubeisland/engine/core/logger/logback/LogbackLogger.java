package de.cubeisland.engine.core.logger.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class LogbackLogger extends de.cubeisland.engine.core.logger.wrapper.Logger
{

    private final Logger logger;

    protected LogbackLogger(Logger logger)
    {
        this.logger = logger;
    }

    public void log(Level level, Throwable throwable, String message, Object... args)
    {
        // TODO
    }

    @Override
    public void trace(Throwable throwable, String message, Object... args)
    {
        this.log(Level.TRACE, throwable, message, args);
    }

    @Override
    public void debug(Throwable throwable, String message, Object... args)
    {
        this.log(Level.DEBUG, throwable, message, args);
    }

    @Override
    public void info(Throwable throwable, String message, Object... args)
    {
        this.log(Level.INFO, throwable, message, args);
    }

    @Override
    public void warn(Throwable throwable, String message, Object... args)
    {
        this.log(Level.WARN, throwable, message, args);
    }

    @Override
    public void error(Throwable throwable, String message, Object... args)
    {
        this.log(Level.ERROR, throwable, message, args);
    }
}
