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
package de.cubeisland.engine.core.logging;

public abstract class Log
{
    // The casts to Throwable are necessary
    public void trace(String message)
    {
        trace((Throwable)null, message, new Object[]{});
    }

    public void trace(String message, Object arg)
    {
        trace((Throwable)null, message, new Object[]{arg});
    }

    public void trace(String message, Object arg1, Object arg2)
    {
        trace((Throwable)null, message, new Object[]{arg1, arg2});
    }

    public void trace(String message, Object... args)
    {
        trace((Throwable)null, message, args);
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
        debug((Throwable)null, message, new Object[]{});
    }

    public void debug(String message, Object arg)
    {
        debug((Throwable)null, message, new Object[]{arg});
    }

    public void debug(String message, Object arg1, Object arg2)
    {
        debug((Throwable)null, message, new Object[]{arg1, arg2});
    }

    public void debug(String message, Object... args)
    {
        debug((Throwable)null, message, args);
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
        info((Throwable)null, message, new Object[]{});
    }

    public void info(String message, Object arg)
    {
        info((Throwable)null, message, new Object[]{arg});
    }

    public void info(String message, Object arg1, Object arg2)
    {
        info((Throwable)null, message, new Object[]{arg1, arg2});
    }

    public void info(String message, Object... args)
    {
        info((Throwable)null, message, args);
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
        warn((Throwable)null, message, new Object[]{});
    }

    public void warn(String message, Object arg)
    {
        warn((Throwable)null, message, new Object[]{arg});
    }

    public void warn(String message, Object arg1, Object arg2)
    {
        warn((Throwable)null, message, new Object[]{arg1, arg2});
    }

    public void warn(String message, Object... args)
    {
        warn((Throwable)null, message, args);
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
        error((Throwable)null, message, new Object[]{});
    }

    public void error(String message, Object arg)
    {
        error((Throwable)null, message, new Object[]{arg});
    }

    public void error(String message, Object arg1, Object arg2)
    {
        error((Throwable)null, message, new Object[]{arg1, arg2});
    }

    public void error(String message, Object... args)
    {
        error((Throwable)null, message, args);
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

    public abstract void setLevel(Level level);

    public abstract Level getLevel();
}
