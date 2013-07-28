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
package de.cubeisland.engine.core.bukkit;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.bukkit.Server;
import org.bukkit.command.CommandMap;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.CorePerms;
import de.cubeisland.engine.core.CoreResource;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.bukkit.command.CommandBackend;
import de.cubeisland.engine.core.bukkit.command.CubeCommandBackend;
import de.cubeisland.engine.core.bukkit.command.FallbackCommandBackend;
import de.cubeisland.engine.core.bukkit.command.SimpleCommandBackend;
import de.cubeisland.engine.core.bukkit.metrics.MetricsInitializer;
import de.cubeisland.engine.core.bukkit.packethook.PacketEventManager;
import de.cubeisland.engine.core.command.ArgumentReader;
import de.cubeisland.engine.core.command.commands.CoreCommands;
import de.cubeisland.engine.core.command.commands.ModuleCommands;
import de.cubeisland.engine.core.command.commands.VanillaCommands;
import de.cubeisland.engine.core.command.commands.VanillaCommands.WhitelistCommand;
import de.cubeisland.engine.core.command.reflected.ReflectedCommandFactory;
import de.cubeisland.engine.core.command.reflected.readable.ReadableCommandFactory;
import de.cubeisland.engine.core.config.Configuration;
import de.cubeisland.engine.core.i18n.I18n;
import de.cubeisland.engine.core.logger.ColorConverter;
import de.cubeisland.engine.core.logger.JULAppender;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.service.ServiceManager;
import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.storage.database.mysql.MySQLDatabase;
import de.cubeisland.engine.core.util.InventoryGuardFactory;
import de.cubeisland.engine.core.util.Profiler;
import de.cubeisland.engine.core.util.Version;
import de.cubeisland.engine.core.util.convert.Convert;
import de.cubeisland.engine.core.util.matcher.Match;
import de.cubeisland.engine.core.util.worker.CubeThreadFactory;
import de.cubeisland.engine.core.webapi.ApiConfig;
import de.cubeisland.engine.core.webapi.ApiServer;
import de.cubeisland.engine.core.webapi.exception.ApiStartupException;
import org.slf4j.LoggerFactory;

import static de.cubeisland.engine.core.util.ReflectionUtils.findFirstField;
import static de.cubeisland.engine.core.util.ReflectionUtils.getFieldValue;

/**
 * This represents the Bukkit-JavaPlugin that gets loaded and implements the Core
 */
public final class BukkitCore extends JavaPlugin implements Core
{
    private Version version;
    private Database database;
    private BukkitPermissionManager permissionManager;
    private BukkitUserManager userManager;
    private BukkitFileManager fileManager;
    private BukkitModuleManager moduleManager;
    private I18n i18n;
    private BukkitCoreConfiguration config;
    private Logger logger;
    private EventManager eventRegistration;
    private BukkitCommandManager commandManager;
    private BukkitTaskManager taskManager;
    private ApiServer apiServer;
    private BukkitWorldManager worldManager;
    private Match matcherManager;
    private InventoryGuardFactory inventoryGuard;
    private PacketEventManager packetEventManager;
    private CorePerms corePerms;
    private BukkitBanManager banManager;
    private ServiceManager serviceManager;

    private List<Runnable> initHooks;
    private LoggerContext loggerContext;


    @Override
    public void onLoad()
    {
        final Server server = this.getServer();
        final PluginManager pm = server.getPluginManager();

        if (!BukkitUtils.isCompatible(this) && !BukkitUtils.init(this))
        {
            this.getLogger().log(java.util.logging.Level.SEVERE, "Your Bukkit server is incompatible with this CubeEngine version.");
            pm.disablePlugin(this);
            return;
        }


        this.version = Version.fromString(this.getDescription().getVersion());

        CubeEngine.initialize(this);
        Convert.init(this);

        this.initHooks = Collections.synchronizedList(new LinkedList<Runnable>());

        try
        {
            this.fileManager = new BukkitFileManager(this);
        }
        catch (IOException e)
        {
            this.getLogger().log(java.util.logging.Level.SEVERE, "Failed to initialize the FileManager", e);
            pm.disablePlugin(this);
            return;
        }
        this.fileManager.dropResources(CoreResource.values());

        try
        {
            System.setProperty("cubeengine.logger.default-path", System.getProperty("cubeengine.log", fileManager.getLogDir().getCanonicalPath()));
            System.setProperty("cubeengine.logger.max-size", System.getProperty("cubeengine.log.max-size", "10MB"));
            System.setProperty("cubeengine.logger.max-file-count", System.getProperty("cubeengine.log.max-file-count", "10"));
        }
        catch (IOException e)
        {
            this.getLogger().log(java.util.logging.Level.SEVERE, "Failed to set the system property for the log folder", e);
        }

        this.loggerContext = (LoggerContext)LoggerFactory.getILoggerFactory();
        this.loggerContext.start();

        try
        {
            File logbackXML = new File(this.getDataFolder(), "logback.xml");
            JoranConfigurator logbackConfigurator = new JoranConfigurator();
            logbackConfigurator.setContext((LoggerContext)LoggerFactory.getILoggerFactory());
            ((LoggerContext)LoggerFactory.getILoggerFactory()).reset();
            if (logbackXML.exists())
            {
                logbackConfigurator.doConfigure(logbackXML.getAbsolutePath());
            }
            else
            {
                logbackConfigurator.doConfigure(new ContextInitializer((LoggerContext)LoggerFactory.getILoggerFactory()).findURLOfDefaultConfigurationFile(true));
            }
        }
        catch (JoranException ex)
        {
            this.getLogger().log(java.util.logging.Level.WARNING,
                                 "An error occured when loading a logback.xml file from the CubeEngine folder: "
                                     + ex.getLocalizedMessage(), ex);
        }
        // Configure the logger
        Logger parentLogger = (Logger)LoggerFactory.getLogger("cubeengine");
        JULAppender consoleAppender = new JULAppender();
        consoleAppender.setContext(parentLogger.getLoggerContext());
        consoleAppender.setName("cubeengine-console");
        consoleAppender.setLogger(this.getLogger());
        PatternLayout consoleLayout = new PatternLayout();
        consoleLayout.setContext(parentLogger.getLoggerContext());
        consoleLayout.setPattern("%color(%msg)");
        consoleAppender.setLayout(consoleLayout);
        parentLogger.addAppender(consoleAppender);
        consoleLayout.start();
        consoleAppender.start();

        this.logger = (Logger)LoggerFactory.getLogger("cubeengine.core");
        // TODO RemoteHandler is not yet implemented this.logger.addHandler(new RemoteHandler(LogLevel.ERROR, this));
        this.logger.setLevel(Level.INFO);
        ColorConverter.setANSISupport(BukkitUtils.isANSISupported());

        this.fileManager.setLogger(this.logger);
        this.fileManager.clearTempDir();

        this.banManager = new BukkitBanManager(this);
        this.serviceManager = new ServiceManager(this);

        // depends on: file manager
        this.config = Configuration.load(BukkitCoreConfiguration.class, new File(this.fileManager.getDataFolder(), "core.yml"));

        // Set the level for the parent logger to the lowest of either the file or console
        // subloggers inherit this by default, but can override
        parentLogger.setLevel((config.loggingConsoleLevel.toInt() > config.loggingFileLevel
                                                                          .toInt()) ? this.config.loggingFileLevel
                                                                                    : this.config.loggingConsoleLevel);
        this.logger.setLevel(parentLogger.getLevel());
        // Set a filter for the console log, so sub loggers don't write logs with lower level than the user wants
        ThresholdFilter consoleFilter = new ThresholdFilter();
        consoleFilter.setLevel(this.config.loggingConsoleLevel.toString());
        parentLogger.getAppender("cubeengine-console").addFilter(consoleFilter);
        consoleFilter.start();
        // Set a filter for the file log, so sub loggers don't write logs with lower level than the user wants
        ThresholdFilter fileFilter = new ThresholdFilter();
        fileFilter.setLevel(this.config.loggingFileLevel.toString());
        this.logger.getAppender("core-file").addFilter(fileFilter);
        fileFilter.start();

        if (!this.config.logCommands)
        {
            BukkitUtils.disableCommandLogging();
            ((Logger)LoggerFactory.getLogger("cubeengine.commands")).setAdditive(false);
        }

        if (this.config.catchSystemSignals)
        {
            BukkitUtils.setSignalHandlers(this);
        }

        this.packetEventManager = new PacketEventManager(this.logger);
        //TODO this is not working atm BukkitUtils.registerPacketHookInjector(this);

        // depends on: object mapper
        this.apiServer = new ApiServer(this);
        this.apiServer.configure(Configuration.load(ApiConfig.class, new File(this.fileManager.getDataFolder(), "webapi.yml")));

        // depends on: core config, server
        this.taskManager = new BukkitTaskManager(this, new CubeThreadFactory("CubeEngine"), this.getServer().getScheduler());

        if (this.config.userWebapi)
        {
            try
            {
                this.apiServer.start();
            }
            catch (ApiStartupException e)
            {
                this.logger.error("The web API will not be available as the server failed to start properly...", e);
            }
        }

        // depends on: core config, file manager, task manager
        this.database = MySQLDatabase.loadFromConfig(this, new File(this.fileManager.getDataFolder(), "database.yml"));
        if (this.database == null)
        {
            return;
        }

        // depends on: plugin manager
        this.eventRegistration = new EventManager(this);

        // depends on: executor, database, Server, core config and event registration
        this.userManager = new BukkitUserManager(this);

        // depends on: file manager, core config
        this.i18n = new I18n(this);

        // depends on: database
        this.moduleManager = new BukkitModuleManager(this, this.getClassLoader());

        // depends on: user manager, world manager
        ArgumentReader.init(this);

        // depends on: server
        SimpleCommandMap commandMap = getFieldValue(server, findFirstField(server, CommandMap.class), SimpleCommandMap.class);
        CommandBackend commandBackend;
        if (commandMap.getClass() == SimpleCommandMap.class)
        {
            commandBackend = new CubeCommandBackend(this);
        }
        else if (SimpleCommandMap.class.isAssignableFrom(commandMap.getClass()))
        {
            this.getLog().warn("The server you are using is not fully compatible, some advanced command features will be disabled.");
            this.getLog().debug("The type of the command map: {}", commandMap.getClass().getName());
            commandBackend = new SimpleCommandBackend(this, commandMap);
        }
        else
        {
            this.getLog().warn("We encountered a serious compatibility issue, however basic command features should still work. Please report this issue to the developers!");
            commandBackend = new FallbackCommandBackend(this);
        }
        this.getLog().debug("Chosen command backend: {}", commandBackend.getClass().getName());
        this.commandManager = new BukkitCommandManager(this, commandBackend);
        this.commandManager.registerCommandFactory(new ReflectedCommandFactory());
        this.commandManager.registerCommandFactory(new ReadableCommandFactory());

        // depends on: plugin manager, module manager
        this.permissionManager = new BukkitPermissionManager(this);

        // depends on: core module
        this.corePerms = new CorePerms(this.moduleManager.getCoreModule());

        // depends on: server, module manager
        this.commandManager.registerCommand(new ModuleCommands(this.moduleManager));
        this.commandManager.registerCommand(new CoreCommands(this));
        if (this.config.improveVanillaCommands)
        {
            this.commandManager.registerCommands(this.getModuleManager().getCoreModule(), new VanillaCommands(this));
            this.commandManager.registerCommand(new WhitelistCommand(this));
        }

        this.matcherManager = new Match();
        this.inventoryGuard = new InventoryGuardFactory(this);

        // depends on loaded worlds
        this.worldManager = new BukkitWorldManager(BukkitCore.this);

        MetricsInitializer metricsInit = new MetricsInitializer(BukkitCore.this);

        // depends on: file manager
        this.moduleManager.loadModules(this.fileManager.getModulesDir());

        metricsInit.start();

        // depends on: finished loading modules
        this.userManager.clean();
    }

    @Override
    public void onEnable()
    {
        if (this.database == null)
        {
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        Iterator<Runnable> it = this.initHooks.iterator();
        while (it.hasNext())
        {
            try
            {
                it.next().run();
            }
            catch (Exception e)
            {
                this.getLog().error("An error occurred during startup: " + e.getLocalizedMessage(), e);
            }
            it.remove();
        }

        if (this.config.preventSpamKick)
        {
            this.getServer().getPluginManager().registerEvents(new PreventSpamKickListener(), this);
        }

        this.getServer().getPluginManager().registerEvents(new CoreListener(this), this);

        this.moduleManager.init();
        this.moduleManager.enableModules();
        this.permissionManager.calculatePermissions();
    }

    @Override
    public void onDisable()
    {
        this.logger.debug("utils cleanup");
        BukkitUtils.cleanup();

        if (this.packetEventManager != null)
        {
            this.packetEventManager.clean();
            this.packetEventManager = null;
        }

        if (this.moduleManager != null)
        {
            this.logger.debug("module manager cleanup");
            this.moduleManager.clean();
            this.moduleManager = null;
        }

        if (this.commandManager != null)
        {
            this.logger.debug("command manager cleanup");
            this.commandManager.clean();
            this.commandManager = null;
        }

        if (this.apiServer != null)
        {
            this.logger.debug("api server shutdown and cleanup");
            this.apiServer.stop();
            this.apiServer.unregisterApiHandlers();
            this.apiServer = null;
        }

        if (this.userManager != null)
        {
            this.logger.debug("user manager cleanup");
            this.userManager.shutdown();
            this.userManager = null;
        }

        if (this.permissionManager != null)
        {
            this.logger.debug("permission manager cleanup");
            this.permissionManager.clean();
            this.permissionManager = null;
        }

        if (this.i18n != null)
        {
            this.i18n.clean();
            this.i18n = null;
        }

        if (this.database != null)
        {
            this.logger.debug("database shutdown");
            this.database.shutdown();
            this.database = null;
        }

        if (this.taskManager != null)
        {
            this.logger.debug("task manager cleanup");
            this.taskManager.clean();
            this.taskManager = null;
        }

        CubeEngine.clean();
        Convert.cleanup();
        Profiler.clean();

        if (this.fileManager != null)
        {
            this.logger.debug("file manager cleanup");
            this.fileManager.clean();
        }

        if (this.loggerContext != null)
        {
            this.loggerContext.stop();
        }

        if (this.fileManager != null)
        {
            this.fileManager.cycleLogs();
        }
        this.fileManager = null;
    }

    public void addInitHook(Runnable runnable)
    {
        assert runnable != null: "The runnble must not be null!";

        this.initHooks.add(runnable);
    }


    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id)
    {
        if (id == null)
        {
            return null;
        }
        String[] parts = id.split(":", 2);
        if (parts.length < 2)
        {
            this.getLog().warn("CubeEngine was specified as a world generator, but no module was specified!");
            return null;
        }
        Module module = this.getModuleManager().getModule(parts[0]);
        if (module == null)
        {
            this.getLog().warn("The module {} wasn't found!");
            return null;
        }

        return this.getWorldManager().getGenerator(module, parts[1].toLowerCase(Locale.ENGLISH));
    }

    @Override
    public Version getVersion()
    {
        return this.version;
    }

    @Override
    public String getSourceVersion()
    {
        return this.moduleManager.getCoreModule().getInfo().getSourceVersion();
    }

    @Override
    public Database getDB()
    {
        return this.database;
    }

    @Override
    public BukkitPermissionManager getPermissionManager()
    {
        return this.permissionManager;
    }

    @Override
    public BukkitUserManager getUserManager()
    {
        return this.userManager;
    }

    @Override
    public BukkitFileManager getFileManager()
    {
        return this.fileManager;
    }

    @Override
    public BukkitModuleManager getModuleManager()
    {
        return this.moduleManager;
    }

    @Override
    public I18n getI18n()
    {
        return this.i18n;
    }

    @Override
    public Logger getLog()
    {
        return this.logger;
    }

    @Override
    public EventManager getEventManager()
    {
        return this.eventRegistration;
    }

    @Override
    public BukkitCoreConfiguration getConfiguration()
    {
        return this.config;
    }

    @Override
    public BukkitCommandManager getCommandManager()
    {
        return this.commandManager;
    }

    @Override
    public BukkitTaskManager getTaskManager()
    {
        return this.taskManager;
    }

    @Override
    public boolean isDebug()
    {
        return Level.DEBUG.isGreaterOrEqual(this.logger.getLevel());
    }

    @Override
    public ApiServer getApiServer()
    {
        return this.apiServer;
    }

    @Override
    public BukkitWorldManager getWorldManager()
    {
        return this.worldManager;
    }

    @Override
    public Match getMatcherManager()
    {
        return this.matcherManager;
    }

    @Override
    public InventoryGuardFactory getInventoryGuard()
    {
        return this.inventoryGuard;
    }

    public PacketEventManager getPacketEventManager()
    {
        return this.packetEventManager;
    }

    @Override
    public BukkitBanManager getBanManager()
    {
        return this.banManager;
    }

    @Override
    public ServiceManager getServiceManager()
    {
        return this.serviceManager;
    }
}
