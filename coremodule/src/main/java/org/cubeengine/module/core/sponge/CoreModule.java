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
package org.cubeengine.module.core.sponge;

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
import de.cubeisland.engine.modularity.core.marker.Setup;
import org.cubeengine.module.core.CoreCommands;
import org.cubeengine.module.core.CorePerms;
import org.cubeengine.module.core.CoreResource;
import org.cubeengine.module.core.module.ModuleCommands;
import org.cubeengine.service.command.CommandManager;
import org.cubeengine.service.filesystem.FileManager;
import org.cubeengine.service.i18n.I18n;
import org.cubeengine.service.i18n.I18nLanguageLoader;
import org.cubeengine.service.logging.LoggingUtil;
import org.cubeengine.module.core.util.FreezeDetection;
import org.cubeengine.module.core.util.Profiler;
import org.cubeengine.module.core.util.Version;
import org.cubeengine.module.core.util.WorldLocation;
import org.cubeengine.module.core.util.converter.BlockVector3Converter;
import org.cubeengine.module.core.util.converter.DurationConverter;
import org.cubeengine.module.core.util.converter.EnchantmentConverter;
import org.cubeengine.module.core.util.converter.ItemStackConverter;
import org.cubeengine.module.core.util.converter.LevelConverter;
import org.cubeengine.module.core.util.converter.MaterialConverter;
import org.cubeengine.module.core.util.converter.VersionConverter;
import org.cubeengine.module.core.util.converter.WorldConverter;
import org.cubeengine.module.core.util.converter.WorldLocationConverter;
import org.cubeengine.module.core.util.matcher.EnchantMatcher;
import org.cubeengine.module.core.util.matcher.MaterialMatcher;
import org.cubeengine.module.core.util.math.BlockVector3;
import org.cubeengine.service.task.TaskManager;
import de.cubeisland.engine.reflect.Reflector;
import org.cubeengine.service.user.UserManager;
import org.cubeengine.module.core.contract.Contract;
import org.joda.time.Duration;
import org.spongepowered.api.Game;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.World;

import static org.cubeengine.module.core.contract.Contract.expectNotNull;
import static org.cubeengine.service.logging.LoggingUtil.getCycler;
import static org.cubeengine.service.logging.LoggingUtil.getFileFormat;
import static org.cubeengine.service.logging.LoggingUtil.getLogFile;

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

    @Setup
    public void onSetup()
    {
        ConverterManager manager = reflector.getDefaultConverterManager();
        manager.registerConverter(new WorldLocationConverter(), WorldLocation.class);
        manager.registerConverter(new BlockVector3Converter(), BlockVector3.class);
        manager.registerConverter(new DurationConverter(), Duration.class);
        manager.registerConverter(new VersionConverter(), Version.class);
        manager.registerConverter(new LevelConverter(), LogLevel.class);
        manager.registerConverter(new WorldConverter(game.getServer()), World.class);
    }

    @Enable
    public void onEnable()
    {
        ((I18nLanguageLoader)i18n.getService().getLanguageLoader()).provideLanguages(this);
        i18n.registerModule(this);

        ConverterManager manager = reflector.getDefaultConverterManager();
        manager.registerConverter(new ItemStackConverter(getModularity().provide(MaterialMatcher.class)), ItemStack.class);
        manager.registerConverter(new MaterialConverter(getModularity().provide(MaterialMatcher.class)), ItemType.class);
        manager.registerConverter(new EnchantmentConverter(getModularity().provide(EnchantMatcher.class)), Enchantment.class);

        fm.dropResources(CoreResource.values());

        this.config = reflector.load(SpongeCoreConfiguration.class, moduleFolder.resolve("core.yml").toFile());

        AsyncFileTarget target = new AsyncFileTarget(getLogFile(fm, "Core"),
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
            game.getEventManager().registerListeners(this, new PreventSpamKickListener(this)); // TODO is this even needed anymore
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

        this.cm.removeCommands(this);

        Profiler.clean();
    }

    public void addInitHook(Runnable runnable)
    {
        Contract.expectNotNull(runnable, "The runnble must not be null!");

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
