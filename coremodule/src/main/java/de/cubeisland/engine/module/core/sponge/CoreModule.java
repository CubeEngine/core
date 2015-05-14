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
package de.cubeisland.engine.module.core.sponge;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.InetAddress;
import java.nio.charset.Charset;
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
import de.cubeisland.engine.modularity.asm.marker.Disable;
import de.cubeisland.engine.modularity.asm.marker.Enable;
import de.cubeisland.engine.modularity.asm.marker.ModuleInfo;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.modularity.core.service.ServiceManager;
import de.cubeisland.engine.module.core.CoreCommands;
import de.cubeisland.engine.module.core.CorePerms;
import de.cubeisland.engine.module.core.CoreResource;
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
import de.cubeisland.engine.module.core.util.matcher.EnchantMatcher;
import de.cubeisland.engine.module.core.util.matcher.EntityMatcher;
import de.cubeisland.engine.module.core.util.matcher.MaterialDataMatcher;
import de.cubeisland.engine.module.core.util.matcher.MaterialMatcher;
import de.cubeisland.engine.module.core.util.matcher.ProfessionMatcher;
import de.cubeisland.engine.module.core.util.matcher.StringMatcher;
import de.cubeisland.engine.module.core.util.matcher.TimeMatcher;
import de.cubeisland.engine.module.core.util.matcher.WorldMatcher;
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

@ModuleInfo(name = "CoreModule", description = "The core module of CubeEngine")
public final class CoreModule extends Module
{
    public static final Charset CHARSET = Charset.forName("UTF-8");

    //region Core fields
    private BukkitCoreConfiguration config;
    private InventoryGuardFactory inventoryGuard;
    private CorePerms corePerms;
    //endregion

    private List<Runnable> initHooks = Collections.synchronizedList(new LinkedList<>());
    private FreezeDetection freezeDetection;

    @Inject private Game game;
    @Inject private Path moduleFolder;
    @Inject private File pluginFolder;
    @Inject private org.slf4j.Logger pluginLogger;
    private Log logger;

    private static Thread mainThread = Thread.currentThread();

    public static boolean isMainThread()
    {
        return Thread.currentThread().equals(mainThread);
    }

    public static Thread getMainThread()
    {
        return mainThread;
    }

    /* TODO configprovider
      T config = this.core.getReflector().create(clazz);
      config.setFile(this.getFolder().resolve("config." + config.getCodec().getExtension()).toFile());
      if (config.reload(true))
      {
          this.getLog().info("Saved new configuration file! config.{}", config.getCodec().getExtension());
      }
      return config;
      */
    @Enable
    public void onEnable()
    {
        System.out.println("CoreModule onEnable...");
        ServiceManager sm = getModularity().getServiceManager();

        sm.registerService(McUUID.class, new McUUID(this));

        // FileManager
        FileManager fileManager = new FileManager(pluginLogger, pluginFolder.toPath(), moduleFolder);
        sm.registerService(FileManager.class, fileManager);

        fileManager.dropResources(CoreResource.values());

        // Reflector
        Reflector reflector = new Reflector();

        sm.registerService(Reflector.class, new Reflector());
        registerConverters(reflector);

        // Core Configuration - depends on Reflector
        this.config = reflector.load(BukkitCoreConfiguration.class, moduleFolder.resolve("core.yml").toFile());

        // LogFactory - depends on FileManager / CoreConfig TODO make it does not need core config anymore
        SpongeLogFactory logFactory = new SpongeLogFactory(this, (Logger)LogManager.getLogger(CoreModule.class.getName()));
        sm.registerService(LogFactory.class, logFactory);

        logger = logFactory.getLog(CoreModule.class, "Core");
        getModularity().registerProvider(Log.class, new LogProvider(logFactory));

        ThreadFactoryProvider threadFactoryProvider = new ThreadFactoryProvider(logger);
        ThreadFactory threadFactory = threadFactoryProvider.get(getInformation(), getModularity());
        getModularity().registerProvider(ThreadFactory.class, threadFactoryProvider);

        logFactory.startExceptionLogger();

        AsyncFileTarget target = new AsyncFileTarget(LoggingUtil.getLogFile(fileManager, "Core"),
                                                     LoggingUtil.getFileFormat(true, true),
                                                     true, LoggingUtil.getCycler(),
                                                     threadFactory);
        target.setLevel(getConfiguration().logging.fileLevel);
        logger.addTarget(target);
        logger.addDelegate(logFactory.getParent());

        getModularity().registerProvider(Permission.class, new BasePermissionProvider(Permission.BASE));
        sm.registerService(AsynchronousScheduler.class, game.getAsyncScheduler());
        sm.registerService(SynchronousScheduler.class, game.getSyncScheduler());

        // I18n - depends on FileManager / CoreConfig
        I18n i18n = new I18n(this, fileManager.getTranslationPath());
        sm.registerService(I18n.class, i18n);


        // SIG INT Handler - depends on TaskManager / CoreConfig / Logger
        if (this.config.catchSystemSignals)
        {
            BukkitUtils.setSignalHandlers(this);
        }

        // CorePermissions - depends on PermissionManager
        this.corePerms = new CorePerms(this);

        // ApiServer - depends on Reflector
        ApiServer apiServer = new ApiServer(this);
        apiServer.configure(reflector.load(ApiConfig.class, moduleFolder.resolve("webapi.yml").toFile()));

        if (this.config.useWebapi)
        {
            try
            {
                apiServer.start();
                sm.registerService(ApiServer.class, apiServer);
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

        sm.registerService(MaterialDataMatcher.class, new MaterialDataMatcher(this, game));
        sm.registerService(MaterialMatcher.class, new MaterialMatcher(this, game));
        sm.registerService(EnchantMatcher.class, new EnchantMatcher(this, game));
        sm.registerService(ProfessionMatcher.class, new ProfessionMatcher(this, game));
        sm.registerService(EntityMatcher.class, new EntityMatcher(this, game));
        sm.registerService(StringMatcher.class, new StringMatcher(logger));
        sm.registerService(TimeMatcher.class, new TimeMatcher(this));
        sm.registerService(WorldMatcher.class, new WorldMatcher(this));
        sm.registerService(EventManager.class, new EventManager(this));

        // DataBase - depends on CoreConfig / ThreadFactory
        getLog().info("Connecting to the database...");
        Database database = MySQLDatabase.loadFromConfig(this, moduleFolder.resolve("database.yml"));
        if (database == null)
        {
            getLog().error("Failed to connect to the database, aborting...");
            return;
        }
        database.registerTable(TableUser.class);
        database.registerTable(TableWorld.class);

        sm.registerService(Database.class, database);




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

        System.out.println("CoreModule enabled...");
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

    @Disable
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
        Path threadDumpFolder = moduleFolder.resolve("thread-dumps");
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
            "yyyy.MM.dd--HHmmss", Locale.US).format(new Date()) + ".dump"), CHARSET))
        {
            Thread main = getMainThread();
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
