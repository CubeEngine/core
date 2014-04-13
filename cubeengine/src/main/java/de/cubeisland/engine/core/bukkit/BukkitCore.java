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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.CorePerms;
import de.cubeisland.engine.core.CoreResource;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.bukkit.command.CommandInjector;
import de.cubeisland.engine.core.bukkit.command.PreCommandListener;
import de.cubeisland.engine.core.command.ArgumentReader;
import de.cubeisland.engine.core.command.commands.CoreCommands;
import de.cubeisland.engine.core.command.commands.ModuleCommands;
import de.cubeisland.engine.core.command.commands.VanillaCommands;
import de.cubeisland.engine.core.command.commands.VanillaCommands.WhitelistCommand;
import de.cubeisland.engine.core.command.reflected.ReflectedCommandFactory;
import de.cubeisland.engine.core.filesystem.FileManager;
import de.cubeisland.engine.core.i18n.I18n;
import de.cubeisland.engine.core.logging.LogFactory;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.storage.database.Database;
import de.cubeisland.engine.core.storage.database.mysql.MySQLDatabase;
import de.cubeisland.engine.core.user.TableUser;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.FreezeDetection;
import de.cubeisland.engine.core.util.InventoryGuardFactory;
import de.cubeisland.engine.core.util.Profiler;
import de.cubeisland.engine.core.util.Version;
import de.cubeisland.engine.core.util.WorldLocation;
import de.cubeisland.engine.core.util.converter.BlockVector3Converter;
import de.cubeisland.engine.core.util.converter.DurationConverter;
import de.cubeisland.engine.core.util.converter.EnchantmentConverter;
import de.cubeisland.engine.core.util.converter.ItemStackConverter;
import de.cubeisland.engine.core.util.converter.LevelConverter;
import de.cubeisland.engine.core.util.converter.LocationConverter;
import de.cubeisland.engine.core.util.converter.MaterialConverter;
import de.cubeisland.engine.core.util.converter.PlayerConverter;
import de.cubeisland.engine.core.util.converter.UserConverter;
import de.cubeisland.engine.core.util.converter.VersionConverter;
import de.cubeisland.engine.core.util.converter.WorldConverter;
import de.cubeisland.engine.core.util.converter.WorldLocationConverter;
import de.cubeisland.engine.core.util.matcher.Match;
import de.cubeisland.engine.core.util.math.BlockVector3;
import de.cubeisland.engine.core.webapi.ApiConfig;
import de.cubeisland.engine.core.webapi.ApiServer;
import de.cubeisland.engine.core.webapi.exception.ApiStartupException;
import de.cubeisland.engine.core.world.ConfigWorld;
import de.cubeisland.engine.core.world.ConfigWorldConverter;
import de.cubeisland.engine.core.world.TableWorld;
import de.cubeisland.engine.logging.Log;
import de.cubeisland.engine.logging.LogLevel;
import de.cubeisland.engine.reflect.Reflector;
import de.cubeisland.engine.reflect.codec.ConverterManager;
import org.joda.time.Duration;

import static de.cubeisland.engine.core.contract.Contract.expectNotNull;

/**
 * This represents the Bukkit-JavaPlugin that gets loaded and implements the Core
 */
public final class BukkitCore extends JavaPlugin implements Core
{
    //region Core fields
    private Version version;
    private Database database;
    private BukkitPermissionManager permissionManager;
    private BukkitUserManager userManager;
    private FileManager fileManager;
    private BukkitModuleManager moduleManager;
    private I18n i18n;
    private BukkitCoreConfiguration config;
    private Log logger;
    private EventManager eventManager;
    private BukkitCommandManager commandManager;
    private BukkitTaskManager taskManager;
    private ApiServer apiServer;
    private BukkitWorldManager worldManager;
    private Match matcherManager;
    private InventoryGuardFactory inventoryGuard;
    private CorePerms corePerms;
    private BukkitBanManager banManager;
    private LogFactory logFactory;
    private Reflector configFactory;
    //endregion

    private List<Runnable> initHooks;
    private PluginConfig pluginConfig;
    private FreezeDetection freezeDetection;
    private boolean loaded = false;
    private boolean started = false;

    @Override
    public void onLoad()
    {
        final Server server = this.getServer();
        final PluginManager pm = server.getPluginManager();

        if (!BukkitUtils.isCompatible(this) || !BukkitUtils.init(this))
        {
            this.getLogger().log(java.util.logging.Level.SEVERE, "Your Bukkit server is incompatible with this CubeEngine version.");
            return;
        }

        this.version = Version.fromString(this.getDescription().getVersion());

        CubeEngine.initialize(this);

        this.configFactory = new Reflector();
        ConverterManager manager = this.configFactory.getDefaultConverterManager();
        manager.registerConverter(LogLevel.class, new LevelConverter());
        manager.registerConverter(ItemStack.class, new ItemStackConverter());
        manager.registerConverter(Material.class, new MaterialConverter());
        manager.registerConverter(Enchantment.class, new EnchantmentConverter());
        manager.registerConverter(User.class, new UserConverter());
        manager.registerConverter(World.class, new WorldConverter());
        manager.registerConverter(Duration.class, new DurationConverter());
        manager.registerConverter(Version.class, new VersionConverter());
        manager.registerConverter(OfflinePlayer.class, new PlayerConverter(this));
        manager.registerConverter(Location.class, new LocationConverter(this));
        manager.registerConverter(WorldLocation.class, new WorldLocationConverter());
        manager.registerConverter(BlockVector3.class, new BlockVector3Converter());

        try (InputStream is = this.getResource("plugin.yml"))
        {
            this.pluginConfig = configFactory.load(PluginConfig.class, is);
        }
        catch (IOException e)
        {
            this.pluginConfig = configFactory.create(PluginConfig.class);
        }

        this.initHooks = Collections.synchronizedList(new LinkedList<Runnable>());

        try
        {
            this.fileManager = new FileManager(this.getLogger(), this.getDataFolder().toPath());
        }
        catch (IOException e)
        {
            this.getLogger().log(java.util.logging.Level.SEVERE, "Failed to initialize the FileManager", e);
            return;
        }
        this.fileManager.dropResources(CoreResource.values());

        // depends on: file manager
        this.config = configFactory.load(BukkitCoreConfiguration.class, this.fileManager.getDataPath().resolve("core.yml").toFile());

        this.fileManager.clearTempDir();

        if (!this.config.logging.logCommands)
        {
            BukkitUtils.disableCommandLogging();
        }

        // depends on: core config, server
        this.taskManager = new BukkitTaskManager(this, this.getServer().getScheduler());

        // depends on: taskmanager
        this.logFactory = new LogFactory(this, this.getLogger()); // , BukkitUtils.isAnsiSupported(server)

        // depends on: taskmanager
        this.logger = logFactory.getCoreLog();

        // depends on: object mapper, logger
        this.apiServer = new ApiServer(this);
        this.apiServer.configure(configFactory.load(ApiConfig.class, this.fileManager.getDataPath().resolve("webapi.yml").toFile()));

        // depends on: logger
        if (this.config.catchSystemSignals)
        {
            BukkitUtils.setSignalHandlers(this);
        }

        if (this.config.useWebapi)
        {
            try
            {
                this.apiServer.start();
            }
            catch (ApiStartupException ex)
            {
                this.logger.error(ex, "The web API will not be available as the server failed to start properly...");
            }
        }

        // depends on: core config, file manager, task manager
        getLog().info("Connecting to the database...");
        this.database = MySQLDatabase.loadFromConfig(this, this.fileManager.getDataPath().resolve("database.yml"));
        if (this.database == null)
        {
            getLog().error("Failed to connect to the database, aborting...");
            return;
        }

        // depends on: database
        this.database.registerTable(TableUser.class);
        this.database.registerTable(TableWorld.class);

        // depends on: plugin manager
        this.eventManager = new EventManager(this);

        // depends on: executor, database, Server, core config and event registration
        this.userManager = new BukkitUserManager(this);

        // depends on: file manager, core config
        this.i18n = new I18n(this);

        // depends on: database
        this.moduleManager = new BukkitModuleManager(this, this.getClassLoader());

        // depends on: user manager, world manager
        ArgumentReader.init(this);

        // depends on: server, config
        this.commandManager = new BukkitCommandManager(this, new CommandInjector(this));
        this.addInitHook(new Runnable() {
            @Override
            public void run()
            {
                pm.registerEvents(new PreCommandListener(BukkitCore.this), BukkitCore.this);
            }
        });
        this.commandManager.registerCommandFactory(new ReflectedCommandFactory());

        // depends on: plugin manager, module manager
        this.permissionManager = new BukkitPermissionManager(this);

        // depends on: core module
        this.corePerms = new CorePerms(this.moduleManager.getCoreModule());

        // depends on: server, module manager
        this.commandManager.registerCommand(new ModuleCommands(this.moduleManager));
        this.commandManager.registerCommand(new CoreCommands(this));
        if (this.config.improveVanilla)
        {
            this.commandManager.registerCommands(this.getModuleManager().getCoreModule(), new VanillaCommands(this));
            this.commandManager.registerCommand(new WhitelistCommand(this));
        }

        this.matcherManager = new Match();
        this.inventoryGuard = new InventoryGuardFactory(this);

        // depends on loaded worlds
        this.worldManager = new BukkitWorldManager(BukkitCore.this);
        // depends on worldManager
        this.getConfigFactory().getDefaultConverterManager().registerConverter(ConfigWorld.class, new ConfigWorldConverter(worldManager));

        // depends on: file manager
        this.moduleManager.loadModules(this.fileManager.getModulesPath());

        this.loaded = true;
    }

    @Override
    public void onEnable()
    {
        if (!this.loaded)
        {
            if (this.started)
            {
                this.onLoad();
            }
            if (!this.loaded)
            {
                this.getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }
        Iterator<Runnable> it = this.initHooks.iterator();
        while (it.hasNext())
        {
            try
            {
                it.next().run();
            }
            catch (Exception ex)
            {
                this.getLog().error(ex, "An error occurred during startup!");
            }
            it.remove();
        }

        this.banManager = new BukkitBanManager(this);

        if (this.config.preventSpamKick)
        {
            this.getServer().getPluginManager().registerEvents(new PreventSpamKickListener(this), this);
        }

        this.getServer().getPluginManager().registerEvents(new CoreListener(this), this);

        this.moduleManager.init();
        this.moduleManager.enableModules();
        this.permissionManager.calculatePermissions();

        this.freezeDetection = new FreezeDetection(this, 20);
        this.freezeDetection.addListener(new Runnable() {
            @Override
            public void run()
            {
                dumpThreads();
            }
        });
        this.freezeDetection.start();

        this.started = true;
    }

    @Override
    public void onDisable()
    {
        this.loaded = false;
        this.logger.debug("utils cleanup");
        BukkitUtils.cleanup();

        if (freezeDetection != null)
        {
            this.freezeDetection.shutdown();
            this.freezeDetection = null;
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
            // TODO i18n cleanup? this.i18n.clean();
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
        Profiler.clean();

        if (this.fileManager != null)
        {
            this.logger.debug("file manager cleanup");
            this.fileManager.clean();
        }

        if (this.logFactory != null)
        {
            this.logFactory.shutdown();
        }

        this.fileManager = null;
    }

    public void addInitHook(Runnable runnable)
    {
        expectNotNull(runnable, "The runnble must not be null!");

        this.initHooks.add(runnable);
    }

    public void dumpThreads()
    {
        Path threadDumpFolder = this.getDataFolder().toPath().resolve("thread-dumps");
        try
        {
            Files.createDirectories(threadDumpFolder);
        }
        catch (IOException ex)
        {
            this.getLog().warn(ex, "Failed to create the folder for the thread dumps!");
            return;
        }

        try (BufferedWriter writer = Files.newBufferedWriter(threadDumpFolder.resolve(new SimpleDateFormat("yyyy.MM.dd--HHmmss", Locale.US).format(new Date()) + ".dump"), CubeEngine.CHARSET))
        {
            Thread main = CubeEngine.getMainThread();
            int i = 1;

            dumpStackTrace(writer, main, main.getStackTrace(), i);
            for (Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet())
            {
                if (entry.getKey() != main)
                {
                    dumpStackTrace(writer, entry.getKey(), entry.getValue(), ++i);
                }
            }
        }
        catch (IOException ex)
        {
            this.getLog().warn(ex, "Failed to write a thread dump!");
        }
    }

    private static void dumpStackTrace(Writer writer, Thread t, StackTraceElement[] trace, int i) throws IOException
    {
        writer.write("Thread #" + i + "\n");
        writer.write("ID: " + t.getId() + "\n");
        writer.write("Name: " + t.getName() + "\n");
        writer.write("State: " + t.getState().name() + "\n");
        writer.write("Stacktrace:\n");

        int j = 0;
        for (StackTraceElement e : trace)
        {
            writer.write("  #" + ++j + " " + e.getClassName() + '.' + e.getMethodName() + '(' + e.getFileName() + ':' + e.getLineNumber() + ")\n");
        }

        writer.write("\n\n\n");
    }


    //region Plugin overrides
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
    //endregion

    //region Core getters
    @Override
    public Version getVersion()
    {
        return this.version;
    }

    @Override
    public String getSourceVersion()
    {
        return this.pluginConfig.sourceVersion;
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
    public Log getLog()
    {
        return this.logger;
    }

    @Override
    public EventManager getEventManager()
    {
        return this.eventManager;
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

    @Override
    public BukkitBanManager getBanManager()
    {
        return this.banManager;
    }

    public LogFactory getLogFactory()
    {
        return logFactory;
    }

    @Override
    public Reflector getConfigFactory()
    {
        return configFactory;
    }

    public CorePerms perms()
    {
        return corePerms;
    }

    //endregion
}
