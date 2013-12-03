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
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

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
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.logging.Log;
import de.cubeisland.engine.core.logging.LoggingException;
import de.cubeisland.engine.core.module.ModuleInfo;

import static java.util.logging.Level.WARNING;

public class LogBackLogFactory
{
    private static final String BASE_NAME = "cubeengine";

    private final Core core;
    private LogbackLog coreLog;
    private LoggerContext loggerContext;
    private final Logger parentLogger;
    private final java.util.logging.Logger julLogger;

    public LogBackLogFactory(Core core, java.util.logging.Logger julLogger, boolean ansiSupport)
    {
        this.core = core;
        this.julLogger = julLogger;
        // Create the logger context with the default settings
        this.loggerContext = (LoggerContext)org.slf4j.LoggerFactory.getILoggerFactory();
        this.loggerContext.start();

        this.parentLogger = this.createParentLogger();

        ColorConverter.setANSISupport(ansiSupport);
        if (!core.getConfiguration().logging.logCommands)
        {
            //this.getLog("commands").getHandle().setAdditive(false);
        }
    }

    private Logger createParentLogger()
    {
        Logger logger = (Logger)org.slf4j.LoggerFactory.getLogger(BASE_NAME);

        // Configure the logging
        JULAppender consoleAppender = new JULAppender();
        consoleAppender.setContext(logger.getLoggerContext());
        consoleAppender.setName("cubeengine-console");
        consoleAppender.setLogger(julLogger);
        PatternLayout consoleLayout = new PatternLayout();
        consoleLayout.setContext(logger.getLoggerContext());
        consoleLayout.setPattern("%color(%msg)\n");// The trailing \n is kind of a workaround, have a look in JULAppender.java:83
        consoleAppender.setLayout(consoleLayout);

        // Set a filter for the console log, so sub loggers don't write logs with lower level than the user wants
        ThresholdFilter consoleFilter = new ThresholdFilter();
        consoleFilter.setLevel(core.getConfiguration().logging.consoleLevel.toString());
        consoleAppender.addFilter(consoleFilter);
        consoleFilter.start();

        logger.addAppender(consoleAppender);
        consoleLayout.start();
        consoleAppender.start();

        logger.setLevel(Level.ALL);

        return logger;
    }


    public long getBirthTime()
    {
        return this.loggerContext.getBirthTime();
    }

    public synchronized Log getCoreLog()
    {
        if (this.coreLog != null)
        {
            //return this.coreLog;
        }
        // Change the settings of the logger context to the ones from the config.
        try
        {
            File xmlConfig = new File(this.core.getFileManager().getDataPath().toFile(), "legacy_logging_logback.xml");
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(this.loggerContext);
            this.loggerContext.reset();
            if (xmlConfig.exists())
            {
                configurator.doConfigure(xmlConfig.getAbsolutePath());
            }
            else
            {
                configurator.doConfigure(new ContextInitializer(this.loggerContext).findURLOfDefaultConfigurationFile(true));
            }
        }
        catch (JoranException e)
        {
            julLogger.log(WARNING, "An error occurred when loading a legacy_logging_logback.xml file from the CubeEngine folder: " + e.getLocalizedMessage(), e);
        }

        this.coreLog = this.getLog("core");

        Logger logger = null;// coreLog.getHandle();
        logger.setLevel(parentLogger.getLevel());

        JULAppender consoleAppender = new JULAppender();
        consoleAppender.setContext(logger.getLoggerContext());
        consoleAppender.setLogger(this.julLogger);

        PatternLayout consoleLayout = new PatternLayout();
        consoleLayout.setContext(logger.getLoggerContext());
        consoleLayout.setPattern("%color(%msg)\n"); // The trailing \n is kind of a workaround, have a look in JULAppender.java:83
        consoleAppender.setLayout(consoleLayout);

        ThresholdFilter consoleFilter = new ThresholdFilter();
        consoleFilter.setLevel(this.core.getConfiguration().logging.consoleLevel.toString());
        consoleAppender.addFilter(consoleFilter);

        consoleAppender.start();
        consoleLayout.start();
        consoleFilter.start();

        logger.addAppender(consoleAppender);

        // Set a filter for the file log, so sub loggers don't write logs with lower level than the user wants
        ThresholdFilter fileFilter = new ThresholdFilter();
        fileFilter.setLevel(core.getConfiguration().logging.fileLevel.toString());
        logger.getAppender("core-file").addFilter(fileFilter);
        fileFilter.start();

        return null;//return this.coreLog;
    }

    public Log createModuleLog(ModuleInfo info)
    {
        // Load the modules legacy_logging_logback.xml, if it exists
        try (JarFile jarFile = new JarFile(info.getPath().toFile()))
        {
            ZipEntry entry = jarFile.getEntry("legacy_logging_logback.xml");
            if (entry != null)
            {
                try (InputStream is = jarFile.getInputStream(entry))
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
        }
        catch (IOException ignored)
        {
        } // This should never happen
        catch (LoggingException ex)
        {
            this.getCoreLog().warn(ex, "An error occurred while loading the modules legacy_logging_logback.xml config");
        }

        Logger logger = (Logger)org.slf4j.LoggerFactory.getLogger(BASE_NAME + "." + info.getId());
        logger.setLevel(Level.ALL);
        //The module has it's own logging
        logger.setAdditive(false);
        // Setup the module's console logging

        JULAppender consoleAppender = new JULAppender();
        consoleAppender.setContext(logger.getLoggerContext());
        consoleAppender.setLogger(this.julLogger);
        PatternLayout consoleLayout = new PatternLayout();
        consoleLayout.setContext(logger.getLoggerContext());
        consoleLayout.setPattern("[" + info.getName() + "] %color(%msg)\n"); // The trailing \n is kind of a workaround, have a look in JULAppender.java:83
        consoleAppender.setLayout(consoleLayout);
        ThresholdFilter consoleFilter = new ThresholdFilter();
        consoleFilter.setLevel(this.core.getConfiguration().logging.consoleLevel.toString());
        consoleAppender.addFilter(consoleFilter);
        consoleFilter.start();

        // Setup the module's file logging
        String logFile = System.getProperty(BASE_NAME + ".logging.default-path") + "/" +
            new SimpleDateFormat("yyyy-MM-dd--HHmm").format(new Date(logger.getLoggerContext().getBirthTime()))
            + "/" + info.getName().toLowerCase();
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
        rollingPolicy.setMaxIndex(Integer.valueOf(System.getProperty(BASE_NAME + ".logging.max-file-count")));
        rollingPolicy.setFileNamePattern(logFile + ".%i.log");
        fileAppender.setRollingPolicy(rollingPolicy);
        SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<>();
        triggeringPolicy.setContext(logger.getLoggerContext());
        triggeringPolicy.setMaxFileSize(System.getProperty(BASE_NAME + ".logging.max-size"));
        fileAppender.setTriggeringPolicy(triggeringPolicy);
        ThresholdFilter fileFilter = new ThresholdFilter();
        fileFilter.setLevel(this.core.getConfiguration().logging.fileLevel.toString());
        fileAppender.addFilter(fileFilter);
        fileFilter.start();

        // Add the appenders to the logging and start everything
        logger.addAppender(consoleAppender);
        logger.addAppender(fileAppender);
        logger.addAppender(this.parentLogger.getAppender("exceptions-file"));
        rollingPolicy.start();
        triggeringPolicy.start();
        fileAppender.start();
        fileEnconder.start();
        consoleLayout.start();
        consoleAppender.start();

        return null;//new LogbackLog(logger);
    }

    public LogbackLog getLog(String id)
    {
        return new LogbackLog(this.loggerContext.getLogger(BASE_NAME + "." + id));
    }

    public void shutdown()
    {
        if (this.loggerContext != null)
        {
            this.loggerContext.stop();
        }
    }

    public void shutdown(Log log)
    {
      //  if (log instanceof LogbackLog)
        {
        //    ((LogbackLog)log).getHandle().detachAndStopAllAppenders();
        }
       // else
        {
            this.getCoreLog().warn(new IllegalArgumentException(), "Only logback logs can be shutdown by the LogbackLogFactory!");
        }
    }
}
