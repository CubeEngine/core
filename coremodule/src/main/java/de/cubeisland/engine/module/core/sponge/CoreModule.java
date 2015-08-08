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
import de.cubeisland.engine.modularity.core.marker.Disable;
import de.cubeisland.engine.modularity.core.marker.Enable;
import de.cubeisland.engine.modularity.asm.marker.ModuleInfo;
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.module.core.CoreCommands;
import de.cubeisland.engine.module.core.CorePerms;
import de.cubeisland.engine.module.core.CoreResource;
import de.cubeisland.engine.module.core.module.ModuleCommands;
import de.cubeisland.engine.service.command.CommandManager;
import de.cubeisland.engine.service.filesystem.FileManager;
import de.cubeisland.engine.service.i18n.I18n;
import de.cubeisland.engine.service.i18n.I18nLanguageLoader;
import de.cubeisland.engine.service.logging.LoggingUtil;
import de.cubeisland.engine.module.core.util.FreezeDetection;
import de.cubeisland.engine.module.core.util.Profiler;
import de.cubeisland.engine.module.core.util.Version;
import de.cubeisland.engine.module.core.util.WorldLocation;
import de.cubeisland.engine.module.core.util.converter.BlockVector3Converter;
import de.cubeisland.engine.module.core.util.converter.DurationConverter;
import de.cubeisland.engine.module.core.util.converter.EnchantmentConverter;
import de.cubeisland.engine.module.core.util.converter.ItemStackConverter;
import de.cubeisland.engine.module.core.util.converter.LevelConverter;
import de.cubeisland.engine.module.core.util.converter.MaterialConverter;
import de.cubeisland.engine.module.core.util.converter.VersionConverter;
import de.cubeisland.engine.module.core.util.converter.WorldConverter;
import de.cubeisland.engine.module.core.util.converter.WorldLocationConverter;
import de.cubeisland.engine.module.core.util.matcher.EnchantMatcher;
import de.cubeisland.engine.module.core.util.matcher.MaterialMatcher;
import de.cubeisland.engine.module.core.util.math.BlockVector3;
import de.cubeisland.engine.service.task.TaskManager;
import de.cubeisland.engine.reflect.Reflector;
import de.cubeisland.engine.service.user.UserManager;
import org.joda.time.Duration;
import org.spongepowered.api.Game;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.World;

import static de.cubeisland.engine.module.core.contract.Contract.expectNotNull;
import static de.cubeisland.engine.service.logging.LoggingUtil.*;

@ModuleInfo(name = "CoreModule", description = "The core module of CubeEngine")
public final class CoreModule extends Module
{
    public static final Charset CHARSET = Charset.forName("UTF-8");

    //region Core fields
    private SpongeCoreConfiguration config;
    private CorePerms corePerms;
    //endregion

    private List<Runnable> initHooks = Collections.synchronizedList(new LinkedList<>());
    private FreezeDetection freezeDetection;

    @Inject private Game game;
    @Inject private Path moduleFolder;
    @Inject private File pluginFolder;
    @Inject private org.slf4j.Logger pluginLogger;
    @Inject private TaskManager tm;
    @Inject private FileManager fm;
    @Inject private Reflector reflector;
    @Inject private Log logger;
    @Inject private ThreadFactory tf;
    @Inject private LogFactory logFactory;
    @Inject private I18n i18n;
    @Inject private CommandManager cm;
    @Inject private UserManager um;

    private static Thread mainThread = Thread.currentThread();

    public static boolean isMainThread()
    {
        return Thread.currentThread().equals(mainThread);
    }

    public static Thread getMainThread()
    {
        return mainThread;
    }

    @Enable
    public void onEnable()
    {
        ((I18nLanguageLoader)i18n.getService().getLanguageLoader()).provideLanguages(this);
        i18n.registerModule(this);

        registerConverters(reflector);
        fm.dropResources(CoreResource.values());

        this.config = reflector.load(SpongeCoreConfiguration.class, moduleFolder.resolve("core.yml").toFile());

        AsyncFileTarget target = new AsyncFileTarget(LoggingUtil.getLogFile(fm, "Core"),
                                                     LoggingUtil.getFileFormat(true, true),
                                                     true, LoggingUtil.getCycler(),
                                                     tf);
        target.setLevel(getConfiguration().logging.fileLevel);
        logger.addTarget(target);
        logFactory.getLog(CoreModule.class).getTargets().forEach(t -> t.setLevel(getConfiguration().logging.consoleLevel));

        Log exceptionLogger = logFactory.getLog(CoreModule.class, "Exceptions");
        exceptionLogger.addTarget(new AsyncFileTarget(getLogFile(fm, "Exceptions"), getFileFormat(true, false), true, getCycler(), tf));

        // SIG INT Handler - depends on TaskManager / CoreConfig / Logger
        if (this.config.catchSystemSignals)
        {
            BukkitUtils.setSignalHandlers(this);
        }

        // CorePermissions - depends on PermissionManager
        this.corePerms = new CorePerms(this);

        if (!this.config.logging.logCommands)
        {
            BukkitUtils.disableCommandLogging();
        }

        if (this.config.preventSpamKick)
        {
            game.getEventManager().register(this, new PreventSpamKickListener(this)); // TODO is this even needed anymore
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

        this.freezeDetection = new FreezeDetection(this, tm, 20);
        this.freezeDetection.addListener(this::dumpThreads);
        this.freezeDetection.start();


        cm.logCommands(getConfiguration().logging.logCommands);

        // depends on: server, module manager, ban manager
        cm.addCommand(new ModuleCommands(this, getModularity(), game.getPluginManager(), cm, fm, i18n));
        cm.addCommand(new CoreCommands(this, cm, um));
    }

    private void registerConverters(Reflector reflector)
    {
        ConverterManager manager = reflector.getDefaultConverterManager();
        manager.registerConverter(new LevelConverter(), LogLevel.class);
        manager.registerConverter(new ItemStackConverter(getModularity().provide(MaterialMatcher.class)), ItemStack.class);
        manager.registerConverter(new MaterialConverter(getModularity().provide(MaterialMatcher.class)), ItemType.class);
        manager.registerConverter(new EnchantmentConverter(getModularity().provide(EnchantMatcher.class)), Enchantment.class);
        manager.registerConverter(new WorldConverter(game.getServer()), World.class);
        manager.registerConverter(new DurationConverter(), Duration.class);
        manager.registerConverter(new VersionConverter(), Version.class);
        manager.registerConverter(new WorldLocationConverter(), WorldLocation.class);
        manager.registerConverter(new BlockVector3Converter(), BlockVector3.class);
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


    public SpongeCoreConfiguration getConfiguration()
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
