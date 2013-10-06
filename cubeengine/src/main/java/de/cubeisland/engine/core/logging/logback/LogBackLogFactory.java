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
package de.cubeisland.engine.core.logging.logback;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.bukkit.BukkitCore;
import de.cubeisland.engine.core.bukkit.BukkitCoreConfiguration;
import de.cubeisland.engine.core.bukkit.BukkitUtils;
import de.cubeisland.engine.core.logging.Log;
import de.cubeisland.engine.core.logging.LogFactory;
import de.cubeisland.engine.core.logging.LoggingException;
import de.cubeisland.engine.core.module.ModuleInfo;
import org.slf4j.LoggerFactory;

import static java.util.logging.Level.WARNING;

public class LogBackLogFactory implements LogFactory
{

    private final BukkitCore core;
    private ch.qos.logback.classic.Logger coreLogger;
    private LoggerContext loggerContext;
    private ch.qos.logback.classic.Logger parentLogger;

    public LogBackLogFactory(BukkitCore core)
    {
        this.core = core;
        // Create the logger context with the default settings
        this.loggerContext = (LoggerContext)org.slf4j.LoggerFactory.getILoggerFactory();
        this.loggerContext.start();
    }

    @Override
    public long getBirthTime()
    {
        return this.loggerContext.getBirthTime();
    }

    @Override
    public Log createCoreLogger(java.util.logging.Logger log, File dataFolder)
    {
        // Change the settings of the logger context to the ones from the config.
        try
        {
            File logbackXML = new File(dataFolder, "logback.xml");
            JoranConfigurator logbackConfigurator = new JoranConfigurator();
            logbackConfigurator.setContext(this.loggerContext);
            this.loggerContext.reset();
            if (logbackXML.exists())
            {
                logbackConfigurator.doConfigure(logbackXML.getAbsolutePath());
            }
            else
            {
                logbackConfigurator.doConfigure(new ContextInitializer(this.loggerContext)
                                                    .findURLOfDefaultConfigurationFile(true));
            }
        }
        catch (JoranException e)
        {
            log.log(WARNING, "An error occurred when loading a logback.xml file from the CubeEngine folder: " + e
                .getLocalizedMessage(), e);
        }
        // Configure the logging
        ch.qos.logback.classic.Logger parentLogger = (ch.qos.logback.classic.Logger)org.slf4j.LoggerFactory
                                                                                             .getLogger("cubeengine");
        JULAppender consoleAppender = new JULAppender();
        consoleAppender.setContext(parentLogger.getLoggerContext());
        consoleAppender.setName("cubeengine-console");
        consoleAppender.setLogger(log);
        PatternLayout consoleLayout = new PatternLayout();
        consoleLayout.setContext(parentLogger.getLoggerContext());
        consoleLayout.setPattern("%color(%msg)\n");// The trailing \n is kind of a workaround, have a look in JULAppender.java:83
        consoleAppender.setLayout(consoleLayout);
        parentLogger.addAppender(consoleAppender);
        consoleLayout.start();
        consoleAppender.start();

        ch.qos.logback.classic.Logger logger;

        logger = (ch.qos.logback.classic.Logger)org.slf4j.LoggerFactory.getLogger("cubeengine.core");
        // TODO RemoteHandler is not yet implemented this.logging.addHandler(new RemoteHandler(LogLevel.ERROR, this));
        logger.setLevel(Level.DEBUG);
        ColorConverter.setANSISupport(BukkitUtils.isANSISupported());

        this.parentLogger = parentLogger;
        this.coreLogger = logger;
        return new LogbackLog(logger);
    }

    public void configureLoggers(BukkitCoreConfiguration config)
    {
        parentLogger.setLevel(Level.ALL);
        this.coreLogger.setLevel(parentLogger.getLevel());
        // Set a filter for the console log, so sub loggers don't write logs with lower level than the user wants
        ThresholdFilter consoleFilter = new ThresholdFilter();
        consoleFilter.setLevel(config.loggingConsoleLevel.toString());
        parentLogger.getAppender("cubeengine-console").addFilter(consoleFilter);
        consoleFilter.start();
        // Set a filter for the file log, so sub loggers don't write logs with lower level than the user wants
        ThresholdFilter fileFilter = new ThresholdFilter();
        fileFilter.setLevel(config.loggingFileLevel.toString());
        this.coreLogger.getAppender("core-file").addFilter(fileFilter);
        fileFilter.start();

        if (!config.logCommands)
        {
            BukkitUtils.disableCommandLogging();
            ((ch.qos.logback.classic.Logger)org.slf4j.LoggerFactory.getLogger("cubeengine.commands")).setAdditive(false);
        }
    }

    @Override
    public Log createModuleLogger(ModuleInfo module)
    {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)org.slf4j.LoggerFactory
                                                                                       .getLogger("cubeengine." + module.getName().toLowerCase());
        logger.setLevel(Level.ALL);
        //The module has it's own logging
        logger.setAdditive(false);
        // Setup the module's console logging
        JULAppender consoleAppender = new JULAppender();
        consoleAppender.setContext(logger.getLoggerContext());
        consoleAppender.setLogger(((BukkitCore)CubeEngine.getCore()).getLogger());
        PatternLayout consoleLayout = new PatternLayout();
        consoleLayout.setContext(logger.getLoggerContext());
        consoleLayout.setPattern("[" + module.getName() + "] %color(%msg)\n"); // The trailing \n is kind of a workaround, have a look in JULAppender.java:83
        consoleAppender.setLayout(consoleLayout);
        ThresholdFilter consoleFilter = new ThresholdFilter();
        consoleFilter.setLevel(this.core.getConfiguration().loggingConsoleLevel.toString());
        consoleAppender.addFilter(consoleFilter);
        consoleFilter.start();

        // Setup the module's file logging
        String logFile = System.getProperty("cubeengine.logging.default-path") + "/" +
            new SimpleDateFormat("yyyy-MM-dd--HHmm").format(new Date(logger.getLoggerContext().getBirthTime()))
            + "/" + module.getName().toLowerCase();
        RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<>();
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
        rollingPolicy.setMaxIndex(Integer.valueOf(System.getProperty("cubeengine.logging.max-file-count")));
        rollingPolicy.setFileNamePattern(logFile + ".%i.log");
        fileAppender.setRollingPolicy(rollingPolicy);
        SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<>();
        triggeringPolicy.setContext(logger.getLoggerContext());
        triggeringPolicy.setMaxFileSize(System.getProperty("cubeengine.logging.max-size"));
        fileAppender.setTriggeringPolicy(triggeringPolicy);
        ThresholdFilter fileFilter = new ThresholdFilter();
        fileFilter.setLevel(this.core.getConfiguration().loggingFileLevel.toString());
        fileAppender.addFilter(fileFilter);
        fileFilter.start();

        // Add the appenders to the logging and start everything
        logger.addAppender(consoleAppender);
        logger.addAppender(fileAppender);
        logger.addAppender(((ch.qos.logback.classic.Logger)org.slf4j.LoggerFactory.getLogger("cubeengine"))
                               .getAppender("exceptions-file"));
        rollingPolicy.start();
        triggeringPolicy.start();
        fileAppender.start();
        fileEnconder.start();
        consoleLayout.start();
        consoleAppender.start();

        return new LogbackLog(logger);
    }

    @Override
    public Log createCommandLogger()
    {
        return new LogbackLog(this.loggerContext.getLogger("cubeengine.commands"));
    }

    @Override
    public Log createPermissionLog()
    {
        return new LogbackLog((Logger)LoggerFactory.getLogger("cubeengine.permissions"));
    }

    @Override
    public Log createLanguageLog()
    {
        return new LogbackLog((Logger)LoggerFactory.getLogger("cubeengine.language"));
    }

    private Log webApiLog;

    @Override
    public Log getWebApiLog()
    {
        if (webApiLog == null)
        {
            webApiLog = new LogbackLog((Logger)LoggerFactory.getLogger("cubeengine.webapi"));
        }
        return webApiLog;
    }

    @Override
    public Log getLog(String name)
    {
        return new LogbackLog(this.loggerContext.getLogger(name));
    }

    public void stop()
    {
        if (this.loggerContext != null)
        {
            this.loggerContext.stop();
        }
    }

    public void configure(InputStream is) throws LoggingException
    {
        JoranConfigurator logbackConfig = new JoranConfigurator();
        logbackConfig.setContext(this.loggerContext);
        try
        {
            logbackConfig.doConfigure(is);
        }
        catch (JoranException ex)
        {
            throw new LoggingException("Could not load the config into LogBack's LoggerContext", ex);
        }
    }
}
