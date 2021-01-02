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
package org.cubeengine.libcube.service.logging;

import com.google.inject.Inject;
import org.cubeengine.libcube.ModuleManager;
import org.cubeengine.logscribe.Log;
import org.cubeengine.logscribe.LogFactory;
import org.cubeengine.logscribe.LogTarget;
import org.cubeengine.logscribe.filter.PrefixFilter;

import java.util.HashMap;
import java.util.Map;

public class LogProvider
{
    private final LogFactory logFactory;
    private final Map<String, Log> loggers = new HashMap<>();

    @Inject
    public LogProvider(LogFactory logFactory)
    {
        this.logFactory = logFactory;
    }

    public Log getLogger(Class<?> owner, String name)
    {
        return loggers.computeIfAbsent(owner.getName() + "#" + name, longName -> {
            Log baseLogger = logFactory.getLog(LogFactory.class, ModuleManager.MAIN_LOGGER_ID);
            Log logger = logFactory.getLog(owner, name);
            LogTarget parentTarget = logger.addDelegate(baseLogger); // delegate to main logger
            parentTarget.appendFilter(new PrefixFilter("[" + name + "] "));
            return logger;
        });
    }
}
