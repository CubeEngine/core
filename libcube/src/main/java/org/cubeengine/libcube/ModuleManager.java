/*
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
package org.cubeengine.libcube;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.logscribe.LogFactory;
import org.cubeengine.libcube.service.ReflectorProvider;
import org.cubeengine.libcube.service.command.CommandManager;
import org.cubeengine.libcube.service.command.CubeCommandManager;
import org.cubeengine.libcube.service.command.ModuleCommand;
import org.cubeengine.libcube.service.database.Database;
import org.cubeengine.libcube.service.database.ModuleTables;
import org.cubeengine.libcube.service.database.Table;
import org.cubeengine.libcube.service.database.mysql.MySQLDatabase;
import org.cubeengine.libcube.service.event.EventManager;
import org.cubeengine.libcube.service.event.ModuleListener;
import org.cubeengine.libcube.service.filesystem.FileManager;
import org.cubeengine.libcube.service.filesystem.ModuleConfig;
import org.cubeengine.libcube.service.i18n.I18n;
import org.cubeengine.libcube.service.logging.LogProvider;
import org.cubeengine.libcube.service.logging.SpongeLogFactory;
import org.cubeengine.libcube.service.task.ModuleThreadFactory;
import org.cubeengine.libcube.service.task.SpongeTaskManager;
import org.cubeengine.libcube.service.task.TaskManager;
import org.cubeengine.reflect.Reflector;
import org.slf4j.Logger;
import org.spongepowered.api.plugin.PluginContainer;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadFactory;

@Singleton
public class ModuleManager
{
    private final FileManager fm;
    private final Reflector reflector;
    private final File path;
    private final EventManager em;
    private LibCube plugin;
    private final SpongeLogFactory logFactory;
    private final ModuleThreadFactory tf;
    private final ThreadGroup threadGroup = new ThreadGroup("CubeEngine");;
    private Module guiceModule = new CubeEngineGuiceModule();
    private Map<Class, PluginContainer> modulePlugins = new HashMap<>();
    private Map<Class, Object> modules = new HashMap<>();
    private LogProvider logProvider;
    private CubeCommandManager cm;
    private I18n i18n;
    private Injector injector;

    public ModuleManager(File path, Logger logger, LibCube libCube, PluginContainer container, Injector injector)
    {
        this.reflector = new ReflectorProvider().get();
        this.modulePlugins.put(LibCube.class, container);
        this.modules.put(LibCube.class, libCube);
        this.path = path;
        this.plugin = libCube;
        this.fm = new FileManager(this, path, logger, reflector);
        this.logFactory = new SpongeLogFactory(this.reflector, this.fm, this);
        this.tf = new ModuleThreadFactory(this.threadGroup, this.logFactory.getLog(ThreadFactory.class));
        this.logProvider = new LogProvider(this.logFactory, this.fm, this);
        this.logFactory.init(tf);

        this.injector = injector.createChildInjector(guiceModule);
        this.i18n = new I18n(fm, reflector, logProvider, this);
        this.injector.injectMembers(this.i18n);
        this.cm = this.injector.getInstance(CubeCommandManager.class);
        this.em = this.injector.getInstance(EventManager.class);
    }

    public Object registerAndCreate(Class<?> module, PluginContainer plugin, Injector injector)
    {
        this.modulePlugins.put(module, plugin);
        Injector moduleInjector = injector.createChildInjector(guiceModule);
        Object instance = moduleInjector.getInstance(module);
        this.modules.put(module, instance);

        this.fm.injectConfig(instance, getAnnotatedFields(instance, ModuleConfig.class));
        this.i18n.registerPlugin(plugin);
        this.cm.injectCommands(moduleInjector, instance, getAnnotatedFields(instance, ModuleCommand.class));
        this.em.injectListeners(moduleInjector, instance, getAnnotatedFields(instance, ModuleListener.class));
        this.em.registerListener(module, instance); // TODO is this working for modules without listener?

        if (module.isAnnotationPresent(ModuleTables.class))
        {
            for (Class<? extends Table<?>> table : module.getAnnotation(ModuleTables.class).value())
            {
                this.injector.getInstance(Database.class).registerTable(table);
            }
        }

        // TODO do stuff with my module

        return instance;
    }

    public Map<Class, PluginContainer> getModulePlugins()
    {
        return Collections.unmodifiableMap(modulePlugins);
    }

    public class CubeEngineGuiceModule extends AbstractModule
    {

        @Override
        protected void configure()
        {
            Matchers.annotatedWith(ModuleConfig.class);
            this.bind(ModuleManager.class).toInstance(ModuleManager.this);
            this.bind(FileManager.class).toInstance(fm); // TODO how to organize our folders?
            this.bind(Reflector.class).toInstance(reflector);
            this.bind(LogFactory.class).toInstance(logFactory);
            this.bind(LogProvider.class).toInstance(logProvider);

            this.bind(TaskManager.class).toProvider(() -> injector.getInstance(SpongeTaskManager.class));
            this.bind(I18n.class).toProvider(() -> i18n);
            this.bind(CommandManager.class).toProvider(() -> injector.getInstance(CubeCommandManager.class));
            this.bind(Database.class).toProvider(() -> injector.getInstance(MySQLDatabase.class));
        }
    }

    public void init()
    {
        this.i18n.enable();
    }

    public File getBasePath()
    {
        return path;
    }

    public static List<Field> getAnnotatedFields(Object instance, Class<? extends Annotation> annotation)
    {
        List<Field> fields = new ArrayList<>();
        for (Field field : instance.getClass().getDeclaredFields())
        {
            if (field.isAnnotationPresent(annotation))
            {
                fields.add(field);
            }
        }
        return fields;
    }

    public Path getPathFor(Class module)
    {
        Optional<String> moduleID = getModuleID(module);
        if (!moduleID.isPresent())
        {
            throw new UnsupportedOperationException("Path for non-modules is not supported yet");
        }
        Path modulePath = this.path.toPath().resolve("modules").resolve(moduleID.get());
        try
        {
            Files.createDirectories(modulePath);
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
        return modulePath;
    }

    public Log getLoggerFor(Class module)
    {
        PluginContainer container = this.modulePlugins.get(module);
        String name;
        if (container == null)
        {
            name = module.getSimpleName();
        }
        else
        {
            name = container.getName();
            if (name.startsWith("CubeEngine - "))
            {
                name = name.substring("CubeEngine - ".length());
            }
        }

        return this.logProvider.getLogger(module, name, container != null);
    }

    public ThreadFactory getThreadFactory(Class module)
    {
        return new ModuleThreadFactory(this.threadGroup, this.logProvider.getLogger(ThreadFactory.class, getModuleName(module).orElse(module.getSimpleName()), getPlugin(module).isPresent()));
    }

    public Object getModule(Class module)
    {
        return this.modules.get(module);
    }


    public Optional<PluginContainer> getPlugin(Class owner)
    {
        return Optional.ofNullable(this.modulePlugins.get(owner));
    }


    public Optional<String> getModuleID(Class clazz)
    {
        Optional<PluginContainer> container = getPlugin(clazz);
        if (container.isPresent())
        {
            String id = container.get().getId();
            if (id.startsWith("cubeengine-"))
            {
                id = id.substring("cubeengine-".length());
            }
            return Optional.of(id);
        }
        return Optional.empty();
    }

    public Optional<String> getModuleName(Class clazz)
    {
        Optional<PluginContainer> container = getPlugin(clazz);
        if (container.isPresent())
        {
            String name = container.get().getName();
            if (name.startsWith("CubeEngine - "))
            {
                name = name.substring("CubeEngine - ".length());
            }
            return Optional.of(name);
        }
        return Optional.empty();
    }
}
