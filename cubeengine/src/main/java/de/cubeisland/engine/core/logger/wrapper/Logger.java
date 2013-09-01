package de.cubeisland.engine.core.logger.wrapper;

public abstract class Logger
{
    
    public void trace(String message)
    {
        trace(null, message, new Object[]{});
    }

    public void trace(String message, Object arg)
    {
        trace(null, message, new Object[]{arg});
    }

    public void trace(String message, Object arg1, Object arg2)
    {
        trace(null, message, new Object[]{arg1, arg2});
    }

    public void trace(String message, Object... args)
    {
        trace(null, message, args);
    }

    public void trace(Throwable throwable, String message)
    {
        trace(throwable, message, new Object[]{});
    }

    public void trace(Throwable throwable, String message, Object arg)
    {
        trace(throwable, message, new Object[]{arg});
    }

    public void trace(Throwable throwable, String message, Object arg1, Object arg2)
    {
        trace(throwable, message, new Object[]{arg1, arg2});
    }

    public abstract void trace(Throwable throwable, String message, Object... args);

    public void debug(String message)
    {
        debug(null, message, new Object[]{});
    }

    public void debug(String message, Object arg)
    {
        debug(null, message, new Object[]{arg});
    }

    public void debug(String message, Object arg1, Object arg2)
    {
        debug(null, message, new Object[]{arg1, arg2});
    }

    public void debug(String message, Object... args)
    {
        debug(null, message, args);
    }

    public void debug(Throwable throwable, String message)
    {
        debug(throwable, message, new Object[]{});
    }

    public void debug(Throwable throwable, String message, Object arg)
    {
        debug(throwable, message, new Object[]{arg});
    }

    public void debug(Throwable throwable, String message, Object arg1, Object arg2)
    {
        debug(throwable, message, new Object[]{arg1, arg2});
    }

    public abstract void debug(Throwable throwable, String message, Object... args);

    public void info(String message)
    {
        info(null, message, new Object[]{});
    }

    public void info(String message, Object arg)
    {
        info(null, message, new Object[]{arg});
    }

    public void info(String message, Object arg1, Object arg2)
    {
        info(null, message, new Object[]{arg1, arg2});
    }

    public void info(String message, Object... args)
    {
        info(null, message, args);
    }

    public void info(Throwable throwable, String message)
    {
        info(throwable, message, new Object[]{});
    }

    public void info(Throwable throwable, String message, Object arg)
    {
        info(throwable, message, new Object[]{arg});
    }

    public void info(Throwable throwable, String message, Object arg1, Object arg2)
    {
        info(throwable, message, new Object[]{arg1, arg2});
    }

    public abstract void info(Throwable throwable, String message, Object... args);

    public void warn(String message)
    {
        warn(null, message, new Object[]{});
    }

    public void warn(String message, Object arg)
    {
        warn(null, message, new Object[]{arg});
    }

    public void warn(String message, Object arg1, Object arg2)
    {
        warn(null, message, new Object[]{arg1, arg2});
    }

    public void warn(String message, Object... args)
    {
        warn(null, message, args);
    }

    public void warn(Throwable throwable, String message)
    {
        warn(throwable, message, new Object[]{});
    }

    public void warn(Throwable throwable, String message, Object arg)
    {
        warn(throwable, message, new Object[]{arg});
    }

    public void warn(Throwable throwable, String message, Object arg1, Object arg2)
    {
        warn(throwable, message, new Object[]{arg1, arg2});
    }

    public abstract void warn(Throwable throwable, String message, Object... args);

    public void error(String message)
    {
        error(null, message, new Object[]{});
    }

    public void error(String message, Object arg)
    {
        error(null, message, new Object[]{arg});
    }

    public void error(String message, Object arg1, Object arg2)
    {
        error(null, message, new Object[]{arg1, arg2});
    }

    public void error(String message, Object... args)
    {
        error(null, message, args);
    }

    public void error(Throwable throwable, String message)
    {
        error(throwable, message, new Object[]{});
    }

    public void error(Throwable throwable, String message, Object arg)
    {
        error(throwable, message, new Object[]{arg});
    }

    public void error(Throwable throwable, String message, Object arg1, Object arg2)
    {
        error(throwable, message, new Object[]{arg1, arg2});
    }

    public abstract void error(Throwable throwable, String message, Object... args);

}
