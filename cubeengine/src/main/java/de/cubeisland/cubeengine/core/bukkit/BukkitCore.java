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

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CorePerms;
import de.cubeisland.cubeengine.core.CoreResource;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.bukkit.command.CommandBackend;
import de.cubeisland.cubeengine.core.bukkit.command.CubeCommandBackend;
import de.cubeisland.cubeengine.core.bukkit.command.FallbackCommandBackend;
import de.cubeisland.cubeengine.core.bukkit.command.SimpleCommandBackend;
import de.cubeisland.cubeengine.core.bukkit.metrics.MetricsInitializer;
import de.cubeisland.cubeengine.core.bukkit.packethook.PacketEventManager;
import de.cubeisland.cubeengine.core.command.ArgumentReader;
import de.cubeisland.cubeengine.core.command.commands.CoreCommands;
import de.cubeisland.cubeengine.core.command.commands.ModuleCommands;
import de.cubeisland.cubeengine.core.command.commands.VanillaCommands;
import de.cubeisland.cubeengine.core.command.commands.VanillaCommands.WhitelistCommand;
import de.cubeisland.cubeengine.core.command.reflected.ReflectedCommandFactory;
import de.cubeisland.cubeengine.core.command.reflected.readable.ReadableCommandFactory;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.filesystem.FileManager;
import de.cubeisland.cubeengine.core.i18n.I18n;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.storage.TableManager;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.DatabaseFactory;
import de.cubeisland.cubeengine.core.util.InventoryGuardFactory;
import de.cubeisland.cubeengine.core.util.Profiler;
import de.cubeisland.cubeengine.core.util.Version;
import de.cubeisland.cubeengine.core.util.convert.Convert;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import de.cubeisland.cubeengine.core.util.worker.CubeThreadFactory;
import de.cubeisland.cubeengine.core.webapi.ApiConfig;
import de.cubeisland.cubeengine.core.webapi.ApiServer;
import de.cubeisland.cubeengine.core.webapi.exception.ApiStartupException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.filter.ThresholdFilter;
import org.slf4j.LoggerFactory;

import static de.cubeisland.cubeengine.core.util.ReflectionUtils.findFirstField;
import static de.cubeisland.cubeengine.core.util.ReflectionUtils.getFieldValue;

/**
 * This represents the Bukkit-JavaPlugin that gets loaded and implements the Core
 */
public final class BukkitCore extends JavaPlugin implements Core
{
    private Version version;
    private Database database;
    private BukkitPermissionManager permissionManager;
    private BukkitUserManager userManager;
    private FileManager fileManager;
    private BukkitModuleManager moduleManager;
    private I18n i18n;
    private BukkitCoreConfiguration config;
    private Logger logger;
    private EventManager eventRegistration;
    private BukkitCommandManager commandManager;
    private BukkitTaskManager taskManager;
    private TableManager tableManager;
    private ApiServer apiServer;
    private BukkitWorldManager worldManager;
    private Match matcherManager;
    private InventoryGuardFactory inventoryGuard;
    private PacketEventManager packetEventManager;
    private CorePerms corePerms;
    private BukkitBanManager banManager;

    private List<Runnable> initHooks;

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
            this.fileManager = new FileManager(this, this.getDataFolder().getAbsoluteFile());
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
        // Configure the
        Logger parentLogger = (Logger)LoggerFactory.getLogger("cubeengine");
        BukkitAppender consoleAppender = new BukkitAppender();
        consoleAppender.setContext(parentLogger.getLoggerContext());
        consoleAppender.setName("cubeengine-console");
        PatternLayout consoleLayout = new PatternLayout();
        consoleLayout.setContext(parentLogger.getLoggerContext());
        consoleLayout.setPattern("%msg");
        consoleAppender.setLayout(consoleLayout);
        parentLogger.addAppender(consoleAppender);
        consoleLayout.start();
        consoleAppender.start();

        this.logger = (Logger)LoggerFactory.getLogger("cubeengine.core");
        // TODO RemoteHandler is not yet implemented this.logger.addHandler(new RemoteHandler(LogLevel.ERROR, this));
        this.logger.setLevel(Level.ALL);

        this.fileManager.setLogger(this.logger);
        this.fileManager.clearTempDir();

        this.banManager = new BukkitBanManager(this);

        // depends on: file manager
        this.config = Configuration.load(BukkitCoreConfiguration.class, new File(this.fileManager.getDataFolder(), "core.yml"));

        // Set the level for the parent logger to the lowest of either the file or console
        // subloggers inherit this by default, but can override
        parentLogger.setLevel((config.loggingConsoleLevel.toInt() > config.loggingFileLevel
                                                                          .toInt()) ? this.config.loggingFileLevel : this.config.loggingConsoleLevel);
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
        this.database = DatabaseFactory.loadDatabase(this.config.database, new File(this.fileManager.getDataFolder(), "database.yml"));
        if (this.database == null)
        {
            this.logger.error("Could not connect to the database type '{}'", this.config.database);
            pm.disablePlugin(this);
            return;
        }
        // depends on: database
        this.tableManager = new TableManager(this);

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
            this.getLog().info("The server you are using is not fully compatible, some advanced command features will be disabled.");
            this.getLog().debug("The type of the command map: {}", commandMap.getClass().getName());
            commandBackend = new SimpleCommandBackend(this, commandMap);
        }
        else
        {
            this.getLog().warn("We encountered a serious compatibility issues, however basic command features should still work. Please report this issue to the developers!");
            commandBackend = new FallbackCommandBackend(this);
        }
        this.getLog().debug("Chosen command backend: " + commandBackend.getClass().getName());
        this.commandManager = new BukkitCommandManager(this, commandBackend);
        this.commandManager.registerCommandFactory(new ReflectedCommandFactory());
        this.commandManager.registerCommandFactory(new ReadableCommandFactory());

        // depends on: plugin manager, module manager
        this.permissionManager = new BukkitPermissionManager(this);

        // depends on: core module
        this.corePerms = new CorePerms(this.getModuleManager().getCoreModule());

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

        if (this.fileManager != null)
        {
            this.logger.debug("file manager cleanup");
            this.fileManager.clean();
            this.fileManager = null;
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
    }

    public void addInitHook(Runnable runnable)
    {
        assert runnable != null: "The runnble must nto be null!";

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
            this.getLog().warn("CubeEngine was specified as a world generator, you have to specify a module!");
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
    public FileManager getFileManager()
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
    public TableManager getTableManger()
    {
        return this.tableManager;
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
}
