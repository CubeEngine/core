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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;
import java.util.function.Predicate;
import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.util.Modules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cubeengine.libcube.service.ModuleInjector;
import org.cubeengine.libcube.service.ReflectorProvider;
import org.cubeengine.libcube.service.command.AnnotationCommandBuilder;
import org.cubeengine.libcube.service.command.annotation.ModuleCommand;
import org.cubeengine.libcube.service.event.EventManager;
import org.cubeengine.libcube.service.event.ModuleListener;
import org.cubeengine.libcube.service.filesystem.ConfigLoader;
import org.cubeengine.libcube.service.filesystem.FileManager;
import org.cubeengine.libcube.service.filesystem.ModuleConfig;
import org.cubeengine.libcube.service.i18n.I18n;
import org.cubeengine.libcube.service.task.ModuleThreadFactory;
import org.cubeengine.libcube.service.task.SpongeTaskManager;
import org.cubeengine.libcube.service.task.TaskManager;
import org.cubeengine.reflect.ReflectedFile;
import org.cubeengine.reflect.Reflector;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.Command.Parameterized;
import org.spongepowered.api.command.manager.CommandMapping;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.metadata.PluginMetadata;

@Singleton
public class ModuleManager
{
    public static final String MAIN_LOGGER_ID = "CubeEngine";

    private final Game game;
    private final Logger mainLogger;
    private final FileManager fm;
    private final Reflector reflector;
    private final LibCube plugin;
    private final ThreadGroup threadGroup = new ThreadGroup("CubeEngine");
    private final Module guiceModule = new CubeEngineGuiceModule();
    private final Map<Class<?>, PluginContainer> modulePlugins = new HashMap<>();
    private final Map<Class<?>, Map<CommandMapping, Command.Parameterized>> moduleCommands = new HashMap<>();
    private final Map<Class<?>, Object> modules = new HashMap<>();
    private final Map<Class<?>, Injector> moduleInjectors = new HashMap<>();
    private final Map<Class<?>, Consumer<Binder>> bindings = new HashMap<>();
    private final AnnotationCommandBuilder cm;
    private final I18n i18n;
    private final Injector injector;

    private final Map<Class<? extends Annotation>, ModuleInjector<? extends Annotation>> injectors = new HashMap<>();

    public ModuleManager(Game game, Path path, LibCube libCube, PluginContainer container, Injector injector)
    {
        this.game = game;
        this.reflector = new ReflectorProvider().get();
        this.modulePlugins.put(LibCube.class, container);
        this.modules.put(LibCube.class, libCube);
        this.plugin = libCube;
        this.fm = new FileManager(this, path);
        this.mainLogger = configureMainLogger();

        this.injector = injector.createChildInjector(guiceModule);
        this.cm = this.injector.getInstance(AnnotationCommandBuilder.class);
        this.i18n = this.injector.getInstance(I18n.class);
    }

    private Logger configureMainLogger() {
        // create main logger and attach console logger
        return LogManager.getLogger(MAIN_LOGGER_ID);
    }

    public <T> T registerAndCreate(Class<T> module, PluginContainer plugin, Injector injector)
    {
        this.modulePlugins.put(module, plugin);
        Module moduleModule = binder -> {
            final PluginMetadata metadata = plugin.getMetadata();
            final ThreadGroup group = new ThreadGroup(this.threadGroup, metadata.getName().orElse(metadata.getId()));
            binder.bind(TaskManager.class).toInstance(new SpongeTaskManager(game, plugin));
            binder.bind(EventManager.class).toInstance(new EventManager(game, plugin));
            binder.bind(ThreadGroup.class).toInstance(group);
            binder.bind(ThreadFactory.class).toInstance(new ModuleThreadFactory(group, plugin.getLogger(), metadata.getId()));
            binder.bind(ConfigLoader.class).toInstance(new ConfigLoader(this.fm, this.reflector, plugin));
        };
        Injector moduleInjector = injector.createChildInjector(Modules.override(guiceModule).with(moduleModule));
        this.moduleInjectors.put(module, moduleInjector);
        T instance = moduleInjector.getInstance(module);
        this.modules.put(module, instance);

        this.i18n.registerPlugin(plugin);

        final EventManager em = moduleInjector.getInstance(EventManager.class);
        em.injectListeners(moduleInjector, instance, getAnnotatedFields(instance, ModuleListener.class));
        em.registerListener(instance);

        this.cm.injectCommands(moduleInjector, instance, getAnnotatedFields(instance, ModuleCommand.class));

        injectClassAnnotations(module, instance);

        // TODO do stuff with my module

        return instance;
    }


    public void injectClassAnnotations(Class<?> module, Object instance)
    {
        this.injectors.entrySet().stream()
                      .filter(entry -> module.isAnnotationPresent(entry.getKey()))
                      .forEach(entry -> injectClassAnnotation(instance, module.getAnnotation(entry.getKey()), entry.getValue()));
    }

    private void injectClassAnnotation(Object instance, Annotation annotation, ModuleInjector injector)
    {
        injector.inject(instance, annotation);
    }

    public Map<Class<?>, PluginContainer> getModulePlugins()
    {
        return Collections.unmodifiableMap(modulePlugins);
    }

    public <A extends Annotation> void registerClassInjector(Class<A> annotation, ModuleInjector<A> injector)
    {
        this.injectors.put(annotation, injector);

        for (Map.Entry<Class<?>, Object> moduleEntry : this.modules.entrySet())
        {
            Class<?> module = moduleEntry.getKey();
            injectClassAnnotations(module, moduleEntry.getValue());
        }

    }

    public <T> void registerBinding(Class<T> clazz, T db) {
        this.bindings.put(clazz, binder -> binder.bind(clazz).toInstance(db));
    }

    public void registerCommands(RegisterCommandEvent<Command.Parameterized> event, PluginContainer container, Object module)
    {
        final Map<CommandMapping, Parameterized> registered;
        final List<Field> commands = getAnnotatedFields(module, ModuleCommand.class);
        if (module == this.plugin)
        {
            this.cm.injectCommands(this.injector, module, commands);
            registered = this.cm.registerModuleCommands(this.injector, event, container, module, commands);
        }
        else
        {
            registered = this.cm.registerModuleCommands(moduleInjectors.get(module.getClass()), event, container, module, commands);
        }
        this.moduleCommands.put(module.getClass(), registered);
    }

    public <T> T registerCommands(RegisterCommandEvent<Command.Parameterized> event, PluginContainer container, Object module, Class<T> holderClass)
    {
        final Map<CommandMapping, Parameterized> registered = this.moduleCommands.computeIfAbsent(module.getClass(), k -> new HashMap<>());
        final Injector moduleInjector = moduleInjectors.get(module.getClass());
        final T instance = moduleInjector.getInstance(holderClass);
        this.cm.registerCommands(moduleInjector, module.getClass(), event, container, instance, registered);
        return instance;
    }

    public Map<CommandMapping, Parameterized> getBaseCommands(Class<?> module)
    {
        return this.moduleCommands.get(module);
    }

    public void loadConfigs(PluginContainer plugin, Class<?> module, boolean early)
    {
        Object instance = modules.get(module);
        injectConfig(plugin, instance, getAnnotatedFields(instance, ModuleConfig.class, a -> a.early() == early));
    }

    private void injectConfig(PluginContainer plugin, Object instance, List<Field> fields)
    {
        for (Field field : fields)
        {
            @SuppressWarnings("unchecked")
            ReflectedFile<?, ?, ?> loaded = ConfigLoader.loadConfig(fm, reflector, plugin, (Class<? extends ReflectedFile<?, ?, ?>>)field.getType());
            field.setAccessible(true);
            try
            {
                field.set(instance, loaded);
            }
            catch (IllegalAccessException e)
            {
                throw new IllegalStateException(e);
            }
        }
    }

    public class CubeEngineGuiceModule extends AbstractModule
    {

        @Override
        protected void configure()
        {
            this.bind(ModuleManager.class).toInstance(ModuleManager.this);
            this.bind(FileManager.class).toInstance(fm); // TODO how to organize our folders?
            this.bind(Reflector.class).toInstance(reflector);

            bindings.forEach((owner, binding) -> binding.accept(binder()));
        }
    }

    public void init()
    {
        this.i18n.enable();
    }

    public static <T extends Annotation> List<Field> getAnnotatedFields(Object instance, Class<T> annotation)
    {
        return getAnnotatedFields(instance, annotation, a -> true);
    }
    public static <T extends Annotation> List<Field> getAnnotatedFields(Object instance, Class<T> annotation, Predicate<T> predicate)
    {
        List<Field> fields = new ArrayList<>();
        for (Field field : instance.getClass().getDeclaredFields())
        {
            if (field.isAnnotationPresent(annotation) && predicate.test(field.getAnnotation(annotation)))
            {
                fields.add(field);
            }
        }
        return fields;
    }

    public Object getModule(Class<?> module)
    {
        return this.modules.get(module);
    }


    public Optional<PluginContainer> getPlugin(Class<?> owner)
    {
        return Optional.ofNullable(this.modulePlugins.get(owner));
    }


    public Optional<String> getModuleId(Class<?> clazz)
    {
        return getPlugin(clazz).map(this::getModuleId);
    }

    public String getModuleId(PluginContainer plugin)
    {
        String id = plugin.getMetadata().getId();
        if (id.startsWith("cubeengine-"))
        {
            return id.substring("cubeengine-".length());
        }
        return id;
    }

    public Optional<String> getModuleName(Class<?> clazz)
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
