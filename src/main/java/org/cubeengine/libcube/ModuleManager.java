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
import org.cubeengine.libcube.service.ModuleInjector;
import org.cubeengine.libcube.service.ReflectorProvider;
import org.cubeengine.libcube.service.command.AnnotationCommandBuilder;
import org.cubeengine.libcube.service.command.annotation.ModuleCommand;
import org.cubeengine.libcube.service.event.EventManager;
import org.cubeengine.libcube.service.event.ModuleListener;
import org.cubeengine.libcube.service.filesystem.FileManager;
import org.cubeengine.libcube.service.filesystem.ModuleConfig;
import org.cubeengine.libcube.service.i18n.I18n;
import org.cubeengine.libcube.service.logging.LogProvider;
import org.cubeengine.libcube.service.logging.SpongeLogFactory;
import org.cubeengine.libcube.service.matcher.MaterialMatcher;
import org.cubeengine.libcube.service.task.ModuleThreadFactory;
import org.cubeengine.logscribe.Log;
import org.cubeengine.logscribe.LogFactory;
import org.cubeengine.reflect.Reflector;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;

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
    private Map<Class, Object> bindings = new HashMap<>();
    private LogProvider logProvider;
    private MaterialMatcher mm;
    private AnnotationCommandBuilder cm;
    private I18n i18n;
    private Injector injector;

    private Map<Class<? extends Annotation>, ModuleInjector<? extends Annotation>> injectors = new HashMap<>();

    public ModuleManager(File path, Logger logger, LibCube libCube, PluginContainer container, Injector injector)
    {
        this.reflector = new ReflectorProvider().get();
        this.modulePlugins.put(LibCube.class, container);
        this.modules.put(LibCube.class, libCube);
        this.path = path;
        this.plugin = libCube;
        this.fm = new FileManager(this, path, reflector);
        this.logFactory = new SpongeLogFactory(this.reflector, this.fm, this);
        this.tf = new ModuleThreadFactory(this.threadGroup, this.logFactory.getLog(ThreadFactory.class));
        this.logProvider = new LogProvider(this.logFactory, this.fm, this);
        this.logFactory.init(tf);
        this.mm = new MaterialMatcher(this);
        this.i18n = new I18n(fm, reflector, logProvider, this);

        this.injector = injector.createChildInjector(guiceModule);
        this.injector.injectMembers(this.i18n);
        this.cm = this.injector.getInstance(AnnotationCommandBuilder.class);
        this.em = this.injector.getInstance(EventManager.class);
    }

    public Object registerAndCreate(Class<?> module, PluginContainer plugin, Injector injector)
    {
        this.modulePlugins.put(module, plugin);
        Injector moduleInjector = injector.createChildInjector(guiceModule);
        Object instance = moduleInjector.getInstance(module);
        this.modules.put(module, instance);

        this.i18n.registerPlugin(plugin);

        this.cm.injectCommands(moduleInjector, instance, getAnnotatedFields(instance, ModuleCommand.class));
        this.em.injectListeners(moduleInjector, instance, getAnnotatedFields(instance, ModuleListener.class));
        this.em.registerListener(module, instance); // TODO is this working for modules without listener?

        injectClassAnnots(module, instance);

        // TODO do stuff with my module

        return instance;
    }



    public void injectClassAnnots(Class<?> module, Object instance) {
        for (Map.Entry<Class<? extends Annotation>, ModuleInjector<?>> entry : this.injectors.entrySet()) {

            if (module.isAnnotationPresent(entry.getKey())) {
                injectClassAnnot(instance, module.getAnnotation(entry.getKey()), entry.getValue());
            }
        }
    }

    private void injectClassAnnot(Object instance, Annotation annotation, ModuleInjector injector) {
        injector.inject(instance, annotation);
    }

    public Map<Class, PluginContainer> getModulePlugins()
    {
        return Collections.unmodifiableMap(modulePlugins);
    }

    public <A extends Annotation> void registerClassInjector(Class<A> annot, ModuleInjector<A> injector) {
        this.injectors.put(annot, injector);

        for (Map.Entry<Class, Object> moduleEntry : this.modules.entrySet()) {
            Class module = moduleEntry.getKey();
            injectClassAnnots(module, moduleEntry.getValue());
        }

    }

    public <T> void registerBinding(Class<T> clazz, T db) {
        this.bindings.put(clazz, db);
    }

    public void registerCommands(RegisterCommandEvent<Command.Parameterized> event, PluginContainer container, Object module)
    {
        final List<Field> commands = getAnnotatedFields(module, ModuleCommand.class);
        if (module == this.plugin) {
            this.cm.injectCommands(this.injector, module, commands);
        }
        this.cm.registerModuleCommands(this.injector, event, container, module, commands);
    }

    public void loadConfigs(Class<?> module) {
        Object instance = modules.get(module);
        this.fm.injectConfig(instance, getAnnotatedFields(instance, ModuleConfig.class));
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
            this.bind(MaterialMatcher.class).toInstance(mm);

            this.bind(I18n.class).toInstance(i18n);

            for (Map.Entry<Class, Object> entry : bindings.entrySet()) {
                this.bind(entry.getKey()).toInstance(entry.getValue());
            }

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
            name = container.getMetadata().getName().orElse(container.getMetadata().getId());
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
            String id = container.get().getMetadata().getId();
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
            String name = container.get().getMetadata().getName().orElse(container.get().getMetadata().getId());
            if (name.startsWith("CubeEngine - "))
            {
                name = name.substring("CubeEngine - ".length());
            }
            return Optional.of(name);
        }
        return Optional.empty();
    }


}
