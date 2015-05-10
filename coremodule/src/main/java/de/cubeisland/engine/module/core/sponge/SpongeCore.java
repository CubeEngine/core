package de.cubeisland.engine.module.core.sponge;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.InetAddress;
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
import java.util.concurrent.ThreadFactory;
import javax.inject.Inject;
import de.cubeisland.engine.converter.ConverterManager;
import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.logscribe.LogLevel;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.module.core.Core;
import de.cubeisland.engine.module.core.CoreCommands;
import de.cubeisland.engine.module.core.CorePerms;
import de.cubeisland.engine.module.core.CoreResource;
import de.cubeisland.engine.module.core.CubeEngine;
import de.cubeisland.engine.module.core.command.completer.ModuleCompleter;
import de.cubeisland.engine.module.core.command.result.paginated.PaginationCommands;
import de.cubeisland.engine.module.core.filesystem.FileManager;
import de.cubeisland.engine.module.core.i18n.I18n;
import de.cubeisland.engine.module.core.logging.LogFactory;
import de.cubeisland.engine.module.core.module.ModuleCommands;
import de.cubeisland.engine.module.core.permission.Permission;
import de.cubeisland.engine.module.core.sponge.command.PreCommandListener;
import de.cubeisland.engine.module.core.storage.database.Database;
import de.cubeisland.engine.module.core.storage.database.mysql.MySQLDatabase;
import de.cubeisland.engine.module.core.user.TableUser;
import de.cubeisland.engine.module.core.user.User;
import de.cubeisland.engine.module.core.util.FreezeDetection;
import de.cubeisland.engine.module.core.util.InventoryGuardFactory;
import de.cubeisland.engine.module.core.util.Profiler;
import de.cubeisland.engine.module.core.util.Version;
import de.cubeisland.engine.module.core.util.WorldLocation;
import de.cubeisland.engine.module.core.util.converter.BlockVector3Converter;
import de.cubeisland.engine.module.core.util.converter.DurationConverter;
import de.cubeisland.engine.module.core.util.converter.EnchantmentConverter;
import de.cubeisland.engine.module.core.util.converter.ItemStackConverter;
import de.cubeisland.engine.module.core.util.converter.LevelConverter;
import de.cubeisland.engine.module.core.util.converter.LocationConverter;
import de.cubeisland.engine.module.core.util.converter.MaterialConverter;
import de.cubeisland.engine.module.core.util.converter.PlayerConverter;
import de.cubeisland.engine.module.core.util.converter.UserConverter;
import de.cubeisland.engine.module.core.util.converter.VersionConverter;
import de.cubeisland.engine.module.core.util.converter.WorldConverter;
import de.cubeisland.engine.module.core.util.converter.WorldLocationConverter;
import de.cubeisland.engine.module.core.util.matcher.Match;
import de.cubeisland.engine.module.core.util.math.BlockVector3;
import de.cubeisland.engine.module.webapi.ApiConfig;
import de.cubeisland.engine.module.webapi.ApiServer;
import de.cubeisland.engine.module.webapi.CommandController;
import de.cubeisland.engine.module.webapi.ConsoleLogEvent;
import de.cubeisland.engine.module.webapi.InetAddressConverter;
import de.cubeisland.engine.module.webapi.exception.ApiStartupException;
import de.cubeisland.engine.module.core.world.ConfigWorld;
import de.cubeisland.engine.module.core.world.ConfigWorldConverter;
import de.cubeisland.engine.module.core.world.TableWorld;
import de.cubeisland.engine.reflect.Reflector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.joda.time.Duration;
import org.spongepowered.api.Game;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import static de.cubeisland.engine.module.core.contract.Contract.expectNotNull;

/**
 * This represents the Bukkit-JavaPlugin that gets loaded and implements the Core
 */

public final class SpongeCore extends Module implements Core
{
    //region Core fields
    private Database database;
    private SpongePermissionManager permissionManager;
    private SpongeUserManager userManager;
    private FileManager fileManager;
    private I18n i18n;
    private BukkitCoreConfiguration config;
    private EventManager eventManager;
    private SpongeCommandManager commandManager;
    private SpongeTaskManager taskManager;
    private ApiServer apiServer;
    private BukkitWorldManager worldManager;
    private Match matcherManager;
    private InventoryGuardFactory inventoryGuard;
    private CorePerms corePerms;
    private SpongeBanManager banManager;
    private LogFactory logFactory;
    private Reflector reflector;
    //endregion

    private List<Runnable> initHooks = Collections.synchronizedList(new LinkedList<>());
    private FreezeDetection freezeDetection;

    @Inject private Game game;
    @Inject private Path dataFolder;
    @Inject private org.slf4j.Logger pluginLogger;
    private Log logger;

    private final ThreadFactory threadFactory;

    public SpongeCore()
    {
        CubeEngine.initialize(this);

        ThreadFactoryProvider threadFactoryProvider = new ThreadFactoryProvider();
        threadFactory = threadFactoryProvider.get(getInformation(), getModulatiry());

        // FileManager
        try
        {
            this.fileManager = new FileManager(pluginLogger, dataFolder);
        }
        catch (IOException e)
        {
            logger.error("Failed to initialize the FileManager", e);
            return;
        }
        this.fileManager.dropResources(CoreResource.values());
        this.fileManager.clearTempDir();

        // Reflector
        this.reflector = new Reflector();
        registerConverters();

        // Core Configuration - depends on Reflector
        this.config = reflector.load(BukkitCoreConfiguration.class, dataFolder.resolve("core.yml").toFile());

        // LogFactory - depends on FileManager / CoreConfig TODO make it does not need core config anymore
        this.logFactory = new LogFactory(this, (Logger)LogManager.getLogger(SpongeCore.class.getName()), threadFactory);
        logger = logFactory.getCoreLog();
        getModulatiry().registerProvider(Log.class, new LogProvider(logFactory));
        getModulatiry().registerProvider(ThreadFactory.class, threadFactoryProvider);
        getModulatiry().registerProvider(Permission.class, new BasePermissionProvider(Permission.BASE));
        // TODO register PermissionManager as Service
        // TODO register CommandManager as Service

        // I18n - depends on FileManager / CoreConfig
        this.i18n = new I18n(this);

    }

    @Override
    public void onLoad()
    {
        // TaskManager
        this.taskManager = new SpongeTaskManager(this, game.getAsyncScheduler(), game.getSyncScheduler());

        // EventManager
        this.eventManager = new EventManager(this);

        // SIG INT Handler - depends on TaskManager / CoreConfig / Logger
        if (this.config.catchSystemSignals)
        {
            BukkitUtils.setSignalHandlers(this);
        }
    }


    @Override
    public void onEnable()
    {
        // PermissionManager - depends on LogFactory / ThreadFactory
        this.permissionManager = new SpongePermissionManager(this);

        // CorePermissions - depends on PermissionManager
        this.corePerms = new CorePerms(this);

        // ApiServer - depends on Reflector
        this.apiServer = new ApiServer(this);
        this.apiServer.configure(reflector.load(ApiConfig.class, dataFolder.resolve("webapi.yml").toFile()));

        if (this.config.useWebapi)
        {
            try
            {
                this.apiServer.start();
                ConsoleLogEvent e = new ConsoleLogEvent(apiServer);
                e.start();
                ((Logger)LogManager.getLogger()).addAppender(e);
            }
            catch (ApiStartupException ex)
            {
                this.logger.error(ex, "The web API will not be available as the server failed to start properly...");
            }
        }

        if (!this.config.logging.logCommands)
        {
            BukkitUtils.disableCommandLogging();
        }

        // DataBase - depends on CoreConfig / ThreadFactory
        getLog().info("Connecting to the database...");
        this.database = MySQLDatabase.loadFromConfig(this, dataFolder.resolve("database.yml"));
        if (this.database == null)
        {
            getLog().error("Failed to connect to the database, aborting...");
            return;
        }
        this.database.registerTable(TableUser.class);
        this.database.registerTable(TableWorld.class);

        // UserManager - depends on Database / CoreConfig / EventManager
        this.userManager = new SpongeUserManager(this); // TODO register as service

        // CommandManager - depends on LogFactory
        this.commandManager = new SpongeCommandManager(this);
        commandManager.registerReaders(this); // depends on a lot
        commandManager.getProviderManager().register(this, new ModuleCompleter(getModulatiry()), Module.class);
        this.addInitHook(() -> game.getEventManager().register(SpongeCore.this, new PreCommandListener(
            SpongeCore.this)));


        this.matcherManager = new Match(game);
        this.inventoryGuard = new InventoryGuardFactory(this);


        // WorldManager - depends on Database
        this.worldManager = new BukkitWorldManager(SpongeCore.this);
        reflector.getDefaultConverterManager().registerConverter(new ConfigWorldConverter(worldManager), ConfigWorld.class);

        // BanManager
        this.banManager = new SpongeBanManager(this);

        registerCommands();

        if (this.config.preventSpamKick)
        {
            game.getEventManager().register(this, new PreventSpamKickListener(this)); // TODO is this even needed anymore
        }

        this.apiServer.registerApiHandlers(this, new CommandController(this));

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

        this.freezeDetection = new FreezeDetection(this, 20);
        this.freezeDetection.addListener(this::dumpThreads);
        this.freezeDetection.start();
    }

    private void registerCommands()
    {
        // depends on: server, module manager, ban manager
        this.commandManager.addCommand(new ModuleCommands(this, getModulatiry(), game.getPluginManager()));
        this.commandManager.addCommand(new CoreCommands(this));
        if (this.config.improveVanilla)
        {
            this.commandManager.addCommands(commandManager, this,
                                            new VanillaCommands(this));
            this.commandManager.addCommand(new WhitelistCommand(this));
        }
        commandManager.addCommands(commandManager, this, new PaginationCommands(commandManager.getPaginationManager()));
        eventManager.registerListener(this, commandManager.getPaginationManager());
    }

    private void registerConverters()
    {
        ConverterManager manager = this.reflector.getDefaultConverterManager();
        manager.registerConverter(new LevelConverter(), LogLevel.class);
        manager.registerConverter(new ItemStackConverter(), ItemStack.class);
        manager.registerConverter(new MaterialConverter(), ItemType.class);
        manager.registerConverter(new EnchantmentConverter(), Enchantment.class);
        manager.registerConverter(new UserConverter(), User.class);
        manager.registerConverter(new WorldConverter(game.getServer()), World.class);
        manager.registerConverter(new DurationConverter(), Duration.class);
        manager.registerConverter(new VersionConverter(), Version.class);
        manager.registerConverter(new PlayerConverter(game.getServer()), org.spongepowered.api.entity.player.User.class);
        manager.registerConverter(new LocationConverter(this), Location.class);
        manager.registerConverter(new WorldLocationConverter(), WorldLocation.class);
        manager.registerConverter(new BlockVector3Converter(), BlockVector3.class);

        manager.registerConverter(new InetAddressConverter(), InetAddress.class);
    }



    public void onDisable()
    {
        this.logger.debug("utils cleanup");
        BukkitUtils.cleanup();

        if (freezeDetection != null)
        {
            this.freezeDetection.shutdown();
            this.freezeDetection = null;
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
        Path threadDumpFolder = dataFolder.resolve("thread-dumps");
        try
        {
            Files.createDirectories(threadDumpFolder);
        }
        catch (IOException ex)
        {
            this.getLog().warn(ex, "Failed to create the folder for the thread dumps!");
            return;
        }

        try (BufferedWriter writer = Files.newBufferedWriter(threadDumpFolder.resolve(new SimpleDateFormat(
            "yyyy.MM.dd--HHmmss", Locale.US).format(new Date()) + ".dump"), CubeEngine.CHARSET))
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
            writer.write("  #" + ++j + " " + e.getClassName() + '.' + e.getMethodName() + '(' + e.getFileName() + ':'
                             + e.getLineNumber() + ")\n");
        }

        writer.write("\n\n\n");
    }

    //region Core getters
    @Override
    public String getVersion()
    {
        return this.getInformation().getVersion();
    }

    @Override
    public String getSourceVersion()
    {
        return this.getInformation().getSourceVersion();
    }

    @Override
    public Database getDB()
    {
        return this.database;
    }

    @Override
    public SpongePermissionManager getPermissionManager()
    {
        return this.permissionManager;
    }

    @Override
    public SpongeUserManager getUserManager()
    {
        return this.userManager;
    }

    @Override
    public FileManager getFileManager()
    {
        return this.fileManager;
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
    public SpongeCommandManager getCommandManager()
    {
        return this.commandManager;
    }

    @Override
    public SpongeTaskManager getTaskManager()
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
    public SpongeBanManager getBanManager()
    {
        return this.banManager;
    }

    @Override
    public LogFactory getLogFactory()
    {
        return logFactory;
    }

    @Override
    public Reflector getReflector()
    {
        return reflector;
    }

    @Override
    public ThreadFactory getThreadFactory()
    {
        return threadFactory;
    }

    public CorePerms perms()
    {
        return corePerms;
    }

    public Game getGame()
    {
        return game;
    }

    //endregion
}
