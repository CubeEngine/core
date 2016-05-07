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
package org.cubeengine.libcube.service.command;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.logscribe.LogFactory;
import de.cubeisland.engine.logscribe.LogLevel;
import de.cubeisland.engine.modularity.asm.marker.ServiceImpl;
import de.cubeisland.engine.modularity.asm.marker.Version;
import de.cubeisland.engine.modularity.core.Modularity;
import de.cubeisland.engine.modularity.core.ModularityHandler;
import de.cubeisland.engine.modularity.core.PostInjectionHandler;
import de.cubeisland.engine.modularity.core.graph.DependencyInformation;
import de.cubeisland.engine.modularity.core.graph.meta.ModuleMetadata;
import de.cubeisland.engine.modularity.core.marker.Enable;
import de.cubeisland.engine.reflect.Reflector;
import org.cubeengine.butler.CommandBase;
import org.cubeengine.butler.CommandDescriptor;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.Dispatcher;
import org.cubeengine.butler.DispatcherCommand;
import org.cubeengine.butler.ProviderManager;
import org.cubeengine.butler.alias.AliasCommand;
import org.cubeengine.butler.builder.CommandBuilder;
import org.cubeengine.butler.parametric.InvokableMethod;
import org.cubeengine.butler.parametric.builder.ParametricBuilder;
import org.cubeengine.libcube.service.command.completer.PlayerCompleter;
import org.cubeengine.libcube.service.command.completer.PlayerListCompleter;
import org.cubeengine.libcube.service.command.exception.CommandExceptionHandler;
import org.cubeengine.libcube.service.command.exception.UnknownExceptionHandler;
import org.cubeengine.libcube.service.command.exception.UnknownSourceExceptionHandler;
import org.cubeengine.libcube.service.command.readers.BooleanReader;
import org.cubeengine.libcube.service.command.readers.ByteReader;
import org.cubeengine.libcube.service.command.readers.CommandSourceReader;
import org.cubeengine.libcube.service.command.readers.ContextReader;
import org.cubeengine.libcube.service.command.readers.DifficultyReader;
import org.cubeengine.libcube.service.command.readers.DimensionTypeReader;
import org.cubeengine.libcube.service.command.readers.DoubleReader;
import org.cubeengine.libcube.service.command.readers.DyeColorReader;
import org.cubeengine.libcube.service.command.readers.EnchantmentReader;
import org.cubeengine.libcube.service.command.readers.EntityTypeReader;
import org.cubeengine.libcube.service.command.readers.FindUserReader;
import org.cubeengine.libcube.service.command.readers.FloatReader;
import org.cubeengine.libcube.service.command.readers.GameModeReader;
import org.cubeengine.libcube.service.command.readers.GeneratorTypeReader;
import org.cubeengine.libcube.service.command.readers.IntReader;
import org.cubeengine.libcube.service.command.readers.ItemStackReader;
import org.cubeengine.libcube.service.command.readers.LogLevelReader;
import org.cubeengine.libcube.service.command.readers.LongReader;
import org.cubeengine.libcube.service.command.readers.PlayerList;
import org.cubeengine.libcube.service.command.readers.PlayerList.UserListReader;
import org.cubeengine.libcube.service.command.readers.ProfessionReader;
import org.cubeengine.libcube.service.command.readers.ShortReader;
import org.cubeengine.libcube.service.command.readers.UserReader;
import org.cubeengine.libcube.service.command.readers.WorldPropertiesReader;
import org.cubeengine.libcube.service.command.readers.WorldReader;
import org.cubeengine.libcube.service.filesystem.FileManager;
import org.cubeengine.libcube.service.filesystem.ModuleConfig;
import org.cubeengine.libcube.service.i18n.I18n;
import org.cubeengine.libcube.service.matcher.EnchantMatcher;
import org.cubeengine.libcube.service.matcher.EntityMatcher;
import org.cubeengine.libcube.service.matcher.MaterialDataMatcher;
import org.cubeengine.libcube.service.matcher.MaterialMatcher;
import org.cubeengine.libcube.service.matcher.ProfessionMatcher;
import org.cubeengine.libcube.service.matcher.StringMatcher;
import org.cubeengine.libcube.service.matcher.UserMatcher;
import org.cubeengine.libcube.service.permission.Permission;
import org.cubeengine.libcube.service.permission.PermissionManager;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.storage.WorldProperties;

import static com.google.common.base.Preconditions.checkArgument;
import static org.spongepowered.api.Sponge.getGame;
import static org.spongepowered.api.Sponge.getPluginManager;

@ServiceImpl(CommandManager.class)
@Version(1)
public class CubeCommandManager extends DispatcherCommand implements CommandManager, PostInjectionHandler<ModuleCommand>, ModularityHandler
{
    private static Thread mainThread = Thread.currentThread();

    public static boolean isMainThread()
    {
        return Thread.currentThread().equals(mainThread);
    }

    private final Log commandLogger;
    private final ProviderManager providerManager;
    private final org.spongepowered.api.command.CommandManager baseDispatcher;

    private final Object plugin;

    private I18n i18n;
    @Inject private PermissionManager pm;

    @Inject private MaterialDataMatcher materialDataMatcher;
    @Inject private EnchantMatcher enchantMatcher;
    @Inject private MaterialMatcher materialMatcher;
    @Inject private ProfessionMatcher professionMatcher;
    @Inject private EntityMatcher entityMatcher;
    @Inject private StringMatcher stringMatcher;
    @Inject private UserMatcher um;
    @Inject private Modularity modularity;

    @Inject Log logger;

    private CommandConfiguration config; // TODO use config

    private Map<Object, Set<CommandBase>> commands = new HashMap<>();
    private Map<CommandBase, CommandMapping> mappings = new HashMap<>();

    @Inject
    public CubeCommandManager(LogFactory logFactory, I18n i18n, Log logger, Modularity modularity, Reflector reflector, FileManager fm)
    {
        super(new CommandManagerDescriptor());

        this.config = reflector.load(CommandConfiguration.class, fm.getDataPath().resolve("command.yml").toFile());
        this.i18n = i18n;
        this.plugin = modularity.provide(PluginContainer.class).getInstance().get();
        this.baseDispatcher = getGame().getCommandManager();

        this.providerManager = new ProviderManager();
        this.providerManager.registerBuilder(InvokableMethod.class, new ParametricCommandBuilder(i18n));

        this.commandLogger = logFactory.getLog(CommandManager.class, "Commands");

        providerManager.getExceptionHandler().addHandler(new UnknownSourceExceptionHandler(logger));
        providerManager.getExceptionHandler().addHandler(new CommandExceptionHandler(logger, i18n));
        providerManager.getExceptionHandler().addHandler(new UnknownExceptionHandler(logger, i18n));

        providerManager.register(this, new CommandContextValue(i18n), CommandContext.class);
        providerManager.register(this, new LocaleContextValue(i18n), Locale.class);

        modularity.registerPostInjectAnnotation(ModuleCommand.class, this);
        modularity.registerHandler(this);
    }

    @Enable
    public void enable()
    {
        providerManager.register(CommandManager.class, new PlayerCompleter(getGame()), User.class);
        providerManager.register(CommandManager.class, new PlayerListCompleter(getGame()), PlayerListCompleter.class);

        providerManager.register(CommandManager.class, new ByteReader(i18n), Byte.class, byte.class);
        providerManager.register(CommandManager.class, new ShortReader(i18n), Short.class, short.class);
        providerManager.register(CommandManager.class, new IntReader(i18n), Integer.class, int.class);
        providerManager.register(CommandManager.class, new LongReader(i18n), Long.class, long.class);
        providerManager.register(CommandManager.class, new FloatReader(i18n), Float.class, float.class);
        providerManager.register(CommandManager.class, new DoubleReader(i18n), Double.class, double.class);

        providerManager.register(CommandManager.class, new BooleanReader(i18n), Boolean.class, boolean.class);
        providerManager.register(CommandManager.class, new EnchantmentReader(enchantMatcher, getGame(), i18n), Enchantment.class);
        providerManager.register(CommandManager.class, new ItemStackReader(materialMatcher, i18n), ItemStack.class);
        providerManager.register(CommandManager.class, new CommandSourceReader(), CommandSource.class, Player.class);
        providerManager.register(CommandManager.class, new WorldReader(i18n), World.class);
        providerManager.register(CommandManager.class, new WorldPropertiesReader(i18n), WorldProperties.class);
        providerManager.register(CommandManager.class, new EntityTypeReader(entityMatcher), EntityType.class);

        providerManager.register(CommandManager.class, new DyeColorReader(materialDataMatcher), DyeColor.class);
        providerManager.register(CommandManager.class, new ProfessionReader(professionMatcher), Profession.class);
        providerManager.register(CommandManager.class, new UserReader(i18n, um), User.class);
        providerManager.register(CommandManager.class, new FindUserReader(i18n, um));
        providerManager.register(CommandManager.class, new DimensionTypeReader(), DimensionType.class);
        providerManager.register(CommandManager.class, new GameModeReader(), GameMode.class);
        providerManager.register(CommandManager.class, new DifficultyReader(i18n), Difficulty.class);
        providerManager.register(CommandManager.class, new GeneratorTypeReader(), GeneratorType.class);
        providerManager.register(CommandManager.class, new LogLevelReader(i18n), LogLevel.class);

        providerManager.register(CommandManager.class, new UserListReader(getGame()), PlayerList.class);

        providerManager.register(CommandManager.class, new ContextReader(), Context.class);


        Sponge.getEventManager().registerListeners(getPluginManager().getPlugin("org.cubeengine").get().getInstance().get(), new PreCommandListener(i18n, stringMatcher));
    }

    @Override
    public CommandManagerDescriptor getDescriptor()
    {
        return (CommandManagerDescriptor)super.getDescriptor();
    }

    @Override
    public ProviderManager getProviderManager()
    {
        return providerManager;
    }

    @Override
    public void removeCommand(String name, boolean completely)
    {
        Optional<? extends CommandMapping> mapping = baseDispatcher.get(name);
        if (mapping.isPresent())
        {
            if (completely)
            {
                baseDispatcher.removeMapping(mapping.get());
                // TODO remove all alias
            }
            else
            {
                if (mapping.get().getCallable() instanceof ProxyCallable)
                {
                    baseDispatcher.removeMapping(mapping.get());
                    removeCommand(getCommand(((ProxyCallable)mapping.get().getCallable()).getAlias()));
                }
                else
                {
                    throw new UnsupportedOperationException("Only WrappedCommands can ");
                }
            }
        }
    }

    @Override
    public boolean addCommand(CommandBase command)
    {
        if (command instanceof AliasCommand)
        {
            Set<CommandBase> cmds = commands.get(((AliasCommand)command).getTarget());
            if (cmds == null)
            {
                cmds = new HashSet<>();
                commands.put(((AliasCommand)command).getTarget(), cmds);
            }
            cmds.add(command);
        }
        if (command.getDescriptor() instanceof CubeDescriptor)
        {
            CubeDescriptor descriptor = (CubeDescriptor)command.getDescriptor();
            DependencyInformation dep = modularity.getLifecycle(descriptor.getOwner()).getInformation();
            Permission parent = pm.register(descriptor.getOwner(), "command", "Allows using all commands of " +
                (dep instanceof ModuleMetadata ? ((ModuleMetadata)dep).getName() : dep.getClassName()), null);
            descriptor.registerPermission(pm, parent);
        }

        boolean b = super.addCommand(command);

        Optional<CommandMapping> mapping = registerSpongeCommand(command.getDescriptor().getName());
        if (mapping.isPresent())
        {
            mappings.put(command, mapping.get());
            commandLogger.info("Registered command: " + mapping.get().getPrimaryAlias());
            return b;
        }
        commandLogger.warn("Command was not registered successfully!");
        return b;
    }

    private Optional<CommandMapping> registerSpongeCommand(String name)
    {
        return baseDispatcher.register(plugin, new ProxyCallable(this, name, logger), name);
    }

    @Override
    public boolean runCommand(CommandSource sender, String commandLine)
    {
        checkArgument(isMainThread(), "Commands may only be called synchronously!");
        if (sender == null)
        {
            return execute(new CommandInvocation(sender, commandLine, providerManager));
        }
        return baseDispatcher.process(sender, commandLine).getSuccessCount().isPresent();
    }

    @Override
    public void logExecution(CommandSource sender, boolean ran, String command, String args)
    {
        CommandDescriptor descriptor = getCommand(command).getDescriptor();
        if (descriptor instanceof CubeCommandDescriptor && ((CubeCommandDescriptor)descriptor).isLoggable())
        {
            if (ran)
            {
                this.commandLogger.debug("execute {} {}: {}", sender.getName(), descriptor.getName(), args);
            }
            else
            {
                this.commandLogger.debug("failed {} {}; {}", sender.getName(), descriptor.getName(), args);
            }
        }
    }

    @Override
    public void logTabCompletion(CommandSource sender, String command, String args)
    {
        CommandDescriptor descriptor = getCommand(command).getDescriptor();
        if (descriptor instanceof CubeCommandDescriptor && ((CubeCommandDescriptor)descriptor).isLoggable())
        {
            this.commandLogger.debug("getSuggestions {} {}: {}", sender.getName(), descriptor.getName(), args);
        }
    }

    @Override
    public org.cubeengine.butler.CommandManager getManager()
    {
        return this;
    }

    /**
     * Creates {@link org.cubeengine.butler.parametric.BasicParametricCommand} for all methods annotated as a command
     * in the given commandHolder and add them to the given dispatcher
     *
     * @param dispatcher    the dispatcher to add the commands to
     * @param owner        the module owning the commands
     * @param commandHolder the command holder containing the command-methods
     */
    @SuppressWarnings("unchecked")
    @Override
    public void addCommands(Dispatcher dispatcher, Object owner, Object commandHolder)
    {
        Set<CommandBase> cmds = this.commands.get(owner);
        if (cmds == null)
        {
            cmds = new HashSet<>();
        }
        this.commands.put(owner, cmds);
        dispatcher = new OwnedDispatcher(dispatcher, owner);
        for (Method method : ParametricBuilder.getMethods(commandHolder.getClass()))
        {
            CommandBuilder<InvokableMethod> builder = getProviderManager().getBuilder(InvokableMethod.class);
            CommandBase cmd = builder.buildCommand(dispatcher, new InvokableMethod(method, commandHolder));
            if (cmd != null)
            {
                dispatcher.addCommand(cmd);
                cmds.add(cmd);
            }
        }
    }

    @Override
    public void addCommands(Object owner, Object commandHolder)
    {
        this.addCommands(this, owner, commandHolder);
    }


    @Override
    public void handle(ModuleCommand annotation, Object injected, Object owner)
    {
        Dispatcher dispatcher = this;
        if (annotation.value() != ContainerCommand.class)
        {
            dispatcher = modularity.provide(annotation.value());
        }
        boolean isCommand = CommandBase.class.isAssignableFrom(injected.getClass());
        if (isCommand)
        {
            dispatcher.addCommand(((CommandBase)injected));
        }
        else
        {
            addCommands(dispatcher, owner, injected);
        }
    }

    @Override
    public void onEnable(Object instance)
    {}

    @Override
    public void onDisable(Object instance)
    {
        for (Field field : instance.getClass().getDeclaredFields())
        {
            if (field.isAnnotationPresent(ModuleCommand.class))
            {
                try
                {
                    field.setAccessible(true);
                    Object value = field.get(instance);
                    removeMappings(value);
                }
                catch (IllegalAccessException e)
                {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

    private void removeMappings(Object value)
    {
        if (value instanceof Dispatcher)
        {
            ((Dispatcher)value).getCommands().forEach(this::removeMappings);
        }
        else if (value instanceof CommandBase)
        {
            CommandMapping mapping = mappings.remove(value);
            if (mapping != null)
            {
                baseDispatcher.removeMapping(mapping);
            }
            ((CommandBase)value).getDescriptor().getDispatcher().removeCommand(((CommandBase)value));
        }
        else
        {
            Set<CommandBase> cmds = commands.get(value);
            if (cmds != null)
            {
                cmds.forEach(this::removeMappings);
            }
        }
    }

    @Override
    public I18n getI18n()
    {
        return i18n;
    }

    @Override
    public PermissionManager getPermissionManager()
    {
        return pm;
    }
}
