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
import de.cubeisland.engine.logscribe.LogFactory;
import de.cubeisland.engine.logscribe.LogLevel;
import de.cubeisland.engine.logscribe.target.file.AsyncFileTarget;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.modularity.core.service.ServiceManager;
import de.cubeisland.engine.module.core.CoreCommands;
import de.cubeisland.engine.module.core.CorePerms;
import de.cubeisland.engine.module.core.CoreResource;
import de.cubeisland.engine.module.core.CubeEngine;
import de.cubeisland.engine.module.core.command.CommandManager;
import de.cubeisland.engine.module.core.database.Database;
import de.cubeisland.engine.module.core.database.mysql.MySQLDatabase;
import de.cubeisland.engine.module.core.filesystem.FileManager;
import de.cubeisland.engine.module.core.i18n.I18n;
import de.cubeisland.engine.module.core.logging.LoggingUtil;
import de.cubeisland.engine.module.core.logging.SpongeLogFactory;
import de.cubeisland.engine.module.core.module.ModuleCommands;
import de.cubeisland.engine.module.core.permission.Permission;
import de.cubeisland.engine.module.core.user.TableUser;
import de.cubeisland.engine.module.core.user.User;
import de.cubeisland.engine.module.core.util.FreezeDetection;
import de.cubeisland.engine.module.core.util.InventoryGuardFactory;
import de.cubeisland.engine.module.core.util.McUUID;
import de.cubeisland.engine.module.core.util.Profiler;
import de.cubeisland.engine.module.core.util.Version;
import de.cubeisland.engine.module.core.util.WorldLocation;
import de.cubeisland.engine.module.core.util.converter.BlockVector3Converter;
import de.cubeisland.engine.module.core.util.converter.DurationConverter;
import de.cubeisland.engine.module.core.util.converter.EnchantmentConverter;
import de.cubeisland.engine.module.core.util.converter.ItemStackConverter;
import de.cubeisland.engine.module.core.util.converter.LevelConverter;
import de.cubeisland.engine.module.core.util.converter.MaterialConverter;
import de.cubeisland.engine.module.core.util.converter.PlayerConverter;
import de.cubeisland.engine.module.core.util.converter.UserConverter;
import de.cubeisland.engine.module.core.util.converter.VersionConverter;
import de.cubeisland.engine.module.core.util.converter.WorldConverter;
import de.cubeisland.engine.module.core.util.converter.WorldLocationConverter;
import de.cubeisland.engine.module.core.util.matcher.Match;
import de.cubeisland.engine.module.core.util.math.BlockVector3;
import de.cubeisland.engine.module.core.world.TableWorld;
import de.cubeisland.engine.module.vanillaplus.VanillaCommands;
import de.cubeisland.engine.module.vanillaplus.WhitelistCommand;
import de.cubeisland.engine.module.webapi.ApiConfig;
import de.cubeisland.engine.module.webapi.ApiServer;
import de.cubeisland.engine.module.webapi.CommandController;
import de.cubeisland.engine.module.webapi.ConsoleLogEvent;
import de.cubeisland.engine.module.webapi.InetAddressConverter;
import de.cubeisland.engine.module.webapi.exception.ApiStartupException;
import de.cubeisland.engine.reflect.Reflector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.joda.time.Duration;
import org.spongepowered.api.Game;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.scheduler.AsynchronousScheduler;
import org.spongepowered.api.service.scheduler.SynchronousScheduler;
import org.spongepowered.api.world.World;

import static de.cubeisland.engine.module.core.contract.Contract.expectNotNull;

public final class CoreModule extends Module
{
    //region Core fields
    private BukkitCoreConfiguration config;
    private Match matcherManager;
    private InventoryGuardFactory inventoryGuard;
    private CorePerms corePerms;
    //endregion

    private List<Runnable> initHooks = Collections.synchronizedList(new LinkedList<>());
    private FreezeDetection freezeDetection;

    @Inject private Game game;
    @Inject private Path dataFolder;
    @Inject private org.slf4j.Logger pluginLogger;
    private Log logger;

      /* TODO configprovider
        T config = this.core.getReflector().create(clazz);
        config.setFile(this.getFolder().resolve("config." + config.getCodec().getExtension()).toFile());
        if (config.reload(true))
        {
            this.getLog().info("Saved new configuration file! config.{}", config.getCodec().getExtension());
        }
        return config;
        */
    @Override
    public void onEnable()
    {
        ServiceManager serviceManager = getModularity().getServiceManager();

        serviceManager.registerService(McUUID.class, new McUUID(this));

        ThreadFactoryProvider threadFactoryProvider = new ThreadFactoryProvider();
        ThreadFactory threadFactory = threadFactoryProvider.get(getInformation(), getModularity());

        // FileManager
        FileManager fileManager = new FileManager(pluginLogger, dataFolder);

        serviceManager.registerService(FileManager.class, fileManager);

        fileManager.dropResources(CoreResource.values());
        fileManager.clearTempDir();

        // Reflector
        Reflector reflector = new Reflector();

        serviceManager.registerService(Reflector.class, new Reflector());
        registerConverters(reflector);

        // Core Configuration - depends on Reflector
        this.config = reflector.load(BukkitCoreConfiguration.class, dataFolder.resolve("core.yml").toFile());

        // LogFactory - depends on FileManager / CoreConfig TODO make it does not need core config anymore
        SpongeLogFactory logFactory = new SpongeLogFactory(this, (Logger)LogManager.getLogger(CoreModule.class.getName()), threadFactory);
        serviceManager.registerService(LogFactory.class, logFactory);

        logger = logFactory.getLog(CoreModule.class, "Core");
        AsyncFileTarget target = new AsyncFileTarget(LoggingUtil.getLogFile(fileManager, "Core"),
                                                     LoggingUtil.getFileFormat(true, true),
                                                     true, LoggingUtil.getCycler(),
                                                     threadFactory);
        target.setLevel(getConfiguration().logging.fileLevel);
        logger.addTarget(target);
        logger.addDelegate(logFactory.getParent());

        getModularity().registerProvider(Log.class, new LogProvider(logFactory));
        getModularity().registerProvider(ThreadFactory.class, threadFactoryProvider);
        getModularity().registerProvider(Permission.class, new BasePermissionProvider(Permission.BASE));
        serviceManager.registerService(AsynchronousScheduler.class, game.getAsyncScheduler());
        serviceManager.registerService(SynchronousScheduler.class, game.getSyncScheduler());

        // I18n - depends on FileManager / CoreConfig
        I18n i18n = new I18n(this, fileManager.getTranslationPath());
        serviceManager.registerService(I18n.class, i18n);


        // SIG INT Handler - depends on TaskManager / CoreConfig / Logger
        if (this.config.catchSystemSignals)
        {
            BukkitUtils.setSignalHandlers(this);
        }

        // CorePermissions - depends on PermissionManager
        this.corePerms = new CorePerms(this);

        // ApiServer - depends on Reflector
        ApiServer apiServer = new ApiServer(this);
        apiServer.configure(reflector.load(ApiConfig.class, dataFolder.resolve("webapi.yml").toFile()));

        if (this.config.useWebapi)
        {
            try
            {
                apiServer.start();
                serviceManager.registerService(ApiServer.class, apiServer);
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
        Database database = MySQLDatabase.loadFromConfig(this, dataFolder.resolve("database.yml"));
        if (database == null)
        {
            getLog().error("Failed to connect to the database, aborting...");
            return;
        }
        database.registerTable(TableUser.class);
        database.registerTable(TableWorld.class);

        serviceManager.registerService(Database.class, database);


        this.matcherManager = new Match(this, game);
        this.inventoryGuard = new InventoryGuardFactory(this);

        // WorldManager - depends on Database

        if (this.config.preventSpamKick)
        {
            game.getEventManager().register(this, new PreventSpamKickListener(this)); // TODO is this even needed anymore
        }

        apiServer.registerApiHandlers(this, new CommandController(this));

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

    private void registerCommands(CommandManager manager) // TODO module dependent on CommandManager
    {
        // depends on: server, module manager, ban manager
        manager.addCommand(new ModuleCommands(this, getModularity(), game.getPluginManager()));
        manager.addCommand(new CoreCommands(this));
        if (this.config.improveVanilla)
        {
            manager.addCommands(manager, this, new VanillaCommands(this));
            manager.addCommand(new WhitelistCommand(this));
        }
    }

    private void registerConverters(Reflector reflector)
    {
        ConverterManager manager = reflector.getDefaultConverterManager();
        manager.registerConverter(new LevelConverter(), LogLevel.class);
        manager.registerConverter(new ItemStackConverter(this), ItemStack.class);
        manager.registerConverter(new MaterialConverter(this), ItemType.class);
        manager.registerConverter(new EnchantmentConverter(this), Enchantment.class);
        manager.registerConverter(new UserConverter(this), User.class);
        manager.registerConverter(new WorldConverter(game.getServer()), World.class);
        manager.registerConverter(new DurationConverter(), Duration.class);
        manager.registerConverter(new VersionConverter(), Version.class);
        manager.registerConverter(new PlayerConverter(game.getServer()),
                                  org.spongepowered.api.entity.player.User.class);
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

        Profiler.clean();
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
    public String getVersion()
    {
        return this.getInformation().getVersion();
    }

    public String getSourceVersion()
    {
        return this.getInformation().getSourceVersion();
    }

    public Log getLog()
    {
        return this.logger;
    }


    public BukkitCoreConfiguration getConfiguration()
    {
        return this.config;
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
