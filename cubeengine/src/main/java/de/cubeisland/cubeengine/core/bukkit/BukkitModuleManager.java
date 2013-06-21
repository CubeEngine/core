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
package de.cubeisland.cubeengine.core.bukkit;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;


import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.logger.JULAppender;
import de.cubeisland.cubeengine.core.module.BaseModuleManager;
import de.cubeisland.cubeengine.core.module.Inject;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.module.ModuleInfo;
import de.cubeisland.cubeengine.core.module.ModuleLoggerFactory;
import de.cubeisland.cubeengine.core.module.exception.CircularDependencyException;
import de.cubeisland.cubeengine.core.module.exception.IncompatibleCoreException;
import de.cubeisland.cubeengine.core.module.exception.IncompatibleDependencyException;
import de.cubeisland.cubeengine.core.module.exception.InvalidModuleException;
import de.cubeisland.cubeengine.core.module.exception.MissingDependencyException;
import de.cubeisland.cubeengine.core.module.exception.MissingPluginDependencyException;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BukkitModuleManager extends BaseModuleManager
{
    private final BukkitCore core;
    private final PluginManager pluginManager;

    public BukkitModuleManager(BukkitCore core, ClassLoader parentClassLoader)
    {
        super(core, parentClassLoader, new BukkitModuleLoggerFactory());
        this.pluginManager = core.getServer().getPluginManager();
        this.core = core;
    }

    void init()
    {
        this.core.getServer().getScheduler().runTask(this.core, new Runnable()
        {
            @Override
            public void run()
            {
                synchronized (BukkitModuleManager.this)
                {
                    for (Module module : getModules())
                    {
                        try
                        {
                            module.onStartupFinished();
                        }
                        catch (Exception e)
                        {
                            module.getLog().warn("An uncaught exception occurred during onFinishLoading(): " + e
                                .getMessage(), e);
                        }
                    }
                }
            }
        });
    }

    @Override
    protected Module loadModule(String name, Map<String, ModuleInfo> moduleInfos, Stack<String> loadStack) throws CircularDependencyException, MissingDependencyException, InvalidModuleException, IncompatibleDependencyException, IncompatibleCoreException, MissingPluginDependencyException
    {
        Module module = super.loadModule(name, moduleInfos, loadStack);
        try
        {
            Field[] fields = module.getClass().getDeclaredFields();
            Map<Class<?>, Plugin> pluginClassMap = this.getPluginClassMap();
            Class<?> fieldType;
            for (Field field : fields)
            {
                fieldType = field.getType();
                if (Plugin.class.isAssignableFrom(fieldType) && field.isAnnotationPresent(Inject.class))
                {
                    Plugin plugin = pluginClassMap.get(fieldType);
                    if (plugin == null)
                    {
                        continue;
                    }
                    this.pluginManager.enablePlugin(plugin); // what their about dependencies?
                    field.setAccessible(true);
                    try
                    {
                        field.set(module, plugin);
                    }
                    catch (Exception e)
                    {
                        module.getLog().warn("Failed to inject a plugin dependency: {}", String.valueOf(plugin));
                    }
                }
            }
        }
        catch (NoClassDefFoundError e)
        {
            module.getLog().warn("Failed to get the fields of the main class: " + e.getLocalizedMessage(), e);
        }
        return module;
    }

    @Override
    protected void validateModuleInfo(ModuleInfo info) throws MissingPluginDependencyException
    {
        for (String plugin : info.getPluginDependencies())
        {
            if (this.pluginManager.getPlugin(plugin) == null)
            {
                throw new MissingPluginDependencyException(plugin);
            }
        }
    }

    protected Map<Class<?>, Plugin> getPluginClassMap()
    {
        Plugin[] plugins = this.pluginManager.getPlugins();
        Map<Class<?>, Plugin> pluginClassMap = new HashMap<Class<?>, Plugin>(plugins.length);
        for (Plugin plugin : plugins)
        {
            pluginClassMap.put(plugin.getClass(), plugin);
        }
        return pluginClassMap;
    }

    public static class BukkitModuleLoggerFactory implements ModuleLoggerFactory
    {

        private final Map<ModuleInfo, Logger> loggers;

        public BukkitModuleLoggerFactory()
        {
            this.loggers = new HashMap<ModuleInfo, Logger>();
        }

        @Override
        public Logger getLogger(ModuleInfo module)
        {
            if (this.loggers.containsKey(module))
            {
                return this.loggers.get(module);
            }
            return createLogger(module);
        }

        private Logger createLogger(ModuleInfo module)
        {
            ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)LoggerFactory
                .getLogger("cubeengine." + module.getName().toLowerCase());
            //The module has it's own logger
            logger.setAdditive(false);
            // Setup the module's console logger
            JULAppender consoleAppender = new JULAppender();
            consoleAppender.setContext(logger.getLoggerContext());
            consoleAppender.setLogger(((BukkitCore)CubeEngine.getCore()).getLogger());
            PatternLayout consoleLayout = new PatternLayout();
            consoleLayout.setContext(logger.getLoggerContext());
            consoleLayout.setPattern("[" + module.getName() + "] color(%msg)");
            consoleAppender.setLayout(consoleLayout);

            // Setup the module's file logger
            String logFile = System.getProperty("cubeengine.logger.default-path") + "/" +
                new SimpleDateFormat("yyyy-MM-dd--HH:mm").format(new Date(logger.getLoggerContext().getBirthTime()))
                + "/" + module.getName().toLowerCase();
            RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<ILoggingEvent>();
            fileAppender.setContext(logger.getLoggerContext());
            fileAppender.setFile(logFile + ".log");
            PatternLayoutEncoder fileEnconder = new PatternLayoutEncoder();
            fileEnconder.setContext(logger.getLoggerContext());
            fileEnconder.setPattern("%date{yyyy-MM-dd HH:mm:ss} [%level] %msg%n");
            fileAppender.setEncoder(fileEnconder);
            FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
            rollingPolicy.setContext(logger.getLoggerContext());
            rollingPolicy.setParent(fileAppender);
            rollingPolicy.setMinIndex(0);
            rollingPolicy.setMaxIndex(Integer.valueOf(System.getProperty("cubeengine.logger.max-file-count")));
            rollingPolicy.setFileNamePattern(logFile + ".%i.log");
            fileAppender.setRollingPolicy(rollingPolicy);
            SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<ILoggingEvent>();
            triggeringPolicy.setContext(logger.getLoggerContext());
            triggeringPolicy.setMaxFileSize(System.getProperty("cubeengine.logger.max-size"));
            fileAppender.setTriggeringPolicy(triggeringPolicy);

            // Add the appenders to the logger and start everything
            logger.addAppender(consoleAppender);
            logger.addAppender(fileAppender);
            logger.addAppender(((ch.qos.logback.classic.Logger)LoggerFactory.getLogger("cubeengine"))
                                   .getAppender("exceptions-file"));
            rollingPolicy.start();
            triggeringPolicy.start();
            fileAppender.start();
            fileEnconder.start();
            consoleLayout.start();
            consoleAppender.start();

            return logger;
        }
    }
}
