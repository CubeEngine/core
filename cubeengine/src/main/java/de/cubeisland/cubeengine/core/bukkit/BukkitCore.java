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
import java.util.Locale;
import java.util.logging.Level;

import org.bukkit.Server;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CorePerms;
import de.cubeisland.cubeengine.core.CoreResource;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.bukkit.metrics.MetricsInitializer;
import de.cubeisland.cubeengine.core.bukkit.packethook.PacketEventManager;
import de.cubeisland.cubeengine.core.command.commands.CoreCommands;
import de.cubeisland.cubeengine.core.command.commands.ModuleCommands;
import de.cubeisland.cubeengine.core.command.commands.VanillaCommands;
import de.cubeisland.cubeengine.core.command.commands.VanillaCommands.WhitelistCommand;
import de.cubeisland.cubeengine.core.command.reflected.ReflectedCommandFactory;
import de.cubeisland.cubeengine.core.command.reflected.readable.ReadableCommandFactory;
import de.cubeisland.cubeengine.core.config.Configuration;
import de.cubeisland.cubeengine.core.filesystem.FileManager;
import de.cubeisland.cubeengine.core.i18n.I18n;
import de.cubeisland.cubeengine.core.logger.CubeFileHandler;
import de.cubeisland.cubeengine.core.logger.CubeLogger;
import de.cubeisland.cubeengine.core.logger.LogLevel;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.storage.TableManager;
import de.cubeisland.cubeengine.core.storage.database.Database;
import de.cubeisland.cubeengine.core.storage.database.DatabaseFactory;
import de.cubeisland.cubeengine.core.user.UserManager;
import de.cubeisland.cubeengine.core.util.InventoryGuardFactory;
import de.cubeisland.cubeengine.core.util.Profiler;
import de.cubeisland.cubeengine.core.util.Version;
import de.cubeisland.cubeengine.core.util.matcher.Match;
import de.cubeisland.cubeengine.core.util.worker.CubeThreadFactory;
import de.cubeisland.cubeengine.core.webapi.ApiConfig;
import de.cubeisland.cubeengine.core.webapi.ApiServer;
import de.cubeisland.cubeengine.core.webapi.exception.ApiStartupException;
import de.cubeisland.cubeengine.core.world.WorldManager;

import static de.cubeisland.cubeengine.core.logger.LogLevel.*;

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
    private CubeLogger logger;
    private EventManager eventRegistration;
    private BukkitCommandManager commandManager;
    private TaskManager taskManager;
    private TableManager tableManager;
    private ApiServer apiServer;
    private BukkitWorldManager worldManager;
    private Match matcherManager;
    private InventoryGuardFactory inventoryGuard;
    private PacketEventManager packetEventManager;
    private CorePerms corePerms;
    private BukkitBanManager banManager;

    @Override
    public void onLoad()
    {
        this.version = Version.fromString(this.getDescription().getVersion());
        final Server server = this.getServer();
        final PluginManager pm = server.getPluginManager();
        if (!BukkitUtils.isCompatible())
        {
            this.getLogger().log(ERROR, "Your Bukkit server is incompatible with this CubeEngine version.");
            pm.disablePlugin(this);
            return;
        }

        this.logger = new CubeLogger("Core", this.getLogger());
        this.logger.setLevel(Level.ALL);
        // TODO RemoteHandler is not yet implemented this.logger.addHandler(new RemoteHandler(LogLevel.ERROR, this));

        this.banManager = new BukkitBanManager(this);

        CubeEngine.initialize(this);

        try
        {
            this.fileManager = new FileManager(this, this.getDataFolder().getAbsoluteFile());
        }
        catch (IOException e)
        {
            this.logger.log(ERROR, "Failed to initialize the FileManager", e);
            pm.disablePlugin(this);
            return;
        }
        this.fileManager.clearTempDir();
        this.fileManager.dropResources(CoreResource.values());

        // depends on: file manager
        this.config = Configuration.load(BukkitCoreConfiguration.class, new File(this.fileManager.getDataFolder(), "core.yml"));

        this.logger.setLevel(this.config.loggingLevel);

        if (!this.config.logCommands)
        {
            BukkitUtils.disableCommandLogging();
        }

        if (this.config.catchSystemSignals)
        {
            BukkitUtils.setSignalHandlers(this);
        }

        this.packetEventManager = new PacketEventManager(this.logger);
        //TODO this is not working atm BukkitUtils.registerPacketHookInjector(this);

        try
        {
            // depends on: file manager
            this.logger.addHandler(new CubeFileHandler(ALL, new File(this.fileManager.getLogDir(), "core").toString()));
        }
        catch (IOException e)
        {
            this.logger.log(ERROR, e.getLocalizedMessage(), e);
        }

        // depends on: object mapper
        this.apiServer = new ApiServer(this);
        this.apiServer.configure(Configuration.load(ApiConfig.class, new File(this.fileManager.getDataFolder(), "webapi.yml")));

        // depends on: core config, server
        this.taskManager = new TaskManager(this, new CubeThreadFactory("CubeEngine"), this.getServer().getScheduler());

        if (this.config.userWebapi)
        {
            try
            {
                this.apiServer.start();
            }
            catch (ApiStartupException e)
            {
                this.logger.log(ERROR, "The web API will not be available as the server failed to start properly...", e);
            }
        }

        // depends on: core config, file manager, task manager
        this.database = DatabaseFactory.loadDatabase(this.config.database, new File(this.fileManager.getDataFolder(), "database.yml"));
        if (this.database == null)
        {
            this.logger.log(ERROR, "Could not connect to the database type ''{0}''", this.config.database);
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

        // depends on: server
        this.commandManager = new BukkitCommandManager(this);
        this.commandManager.registerCommandFactory(new ReflectedCommandFactory());
        this.commandManager.registerCommandFactory(new ReadableCommandFactory());

        // depends on: database
        this.moduleManager = new BukkitModuleManager(this, this.getClassLoader());

        // depends on: plugin manager, module manager
        this.permissionManager = new BukkitPermissionManager(this);

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
        this.userManager.init();
        this.worldManager.loadWorlds();

        if (this.config.preventSpamKick)
        {
            this.getServer().getPluginManager().registerEvents(new PreventSpamKickListener(), this);
        }

        this.getServer().getPluginManager().registerEvents(new CoreListener(this), this);



//        this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
//        {
//            @Override
//            public void run()
//            {
//            }
//        });
    }

    @Override
    public void onDisable()
    {
        this.logger.log(DEBUG, "utils cleanup");
        BukkitUtils.cleanup();

        if (this.packetEventManager != null)
        {
            this.packetEventManager.clean();
            this.packetEventManager = null;
        }

        if (this.moduleManager != null)
        {
            this.logger.log(DEBUG, "module manager cleanup");
            this.moduleManager.clean();
            this.moduleManager = null;
        }

        if (this.commandManager != null)
        {
            this.logger.log(DEBUG, "command manager cleanup");
            this.commandManager.clean();
            this.commandManager = null;
        }

        if (this.apiServer != null)
        {
            this.logger.log(DEBUG, "api server shutdown and cleanup");
            this.apiServer.stop();
            this.apiServer.unregisterApiHandlers();
            this.apiServer = null;
        }

        if (this.fileManager != null)
        {
            this.logger.log(DEBUG, "file manager cleanup");
            this.fileManager.clean();
            this.fileManager = null;
        }

        if (this.userManager != null)
        {
            this.logger.log(DEBUG, "user manager cleanup");
            this.userManager.shutdown();
            this.userManager = null;
        }

        if (this.permissionManager != null)
        {
            this.logger.log(DEBUG, "permission manager cleanup");
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
            this.logger.log(DEBUG, "database shutdown");
            this.database.shutdown();
            this.database = null;
        }

        if (this.taskManager != null)
        {
            this.logger.log(DEBUG, "task manager cleanup");
            this.taskManager.clean();
            this.taskManager = null;
        }

        CubeEngine.clean();
        Profiler.clean();
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
            this.getLog().log(WARNING, "CubeEngine was specified as a world generator, you have to specify a module!");
            return null;
        }
        Module module = this.getModuleManager().getModule(parts[0]);
        if (module == null)
        {
            this.getLog().log(WARNING, "The module {0} wasn't found!");
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
    public UserManager getUserManager()
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
    public CubeLogger getLog()
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
    public TaskManager getTaskManager()
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
        return this.logger.getLevel().intValue() <= LogLevel.DEBUG.intValue();
    }

    @Override
    public ApiServer getApiServer()
    {
        return this.apiServer;
    }

    @Override
    public WorldManager getWorldManager()
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
