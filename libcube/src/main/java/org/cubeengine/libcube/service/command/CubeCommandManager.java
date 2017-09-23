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
package org.cubeengine.libcube.service.command;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Injector;
import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.logscribe.LogFactory;
import de.cubeisland.engine.logscribe.LogLevel;
import org.cubeengine.butler.alias.AliasConfiguration;
import org.cubeengine.butler.alias.AliasDescriptor;
import org.cubeengine.libcube.LibCube;
import org.cubeengine.libcube.ModuleManager;
import org.cubeengine.libcube.service.command.parser.BlockTypeParser;
import org.cubeengine.libcube.service.command.parser.ItemTypeParser;
import org.cubeengine.reflect.Reflector;
import org.cubeengine.butler.CommandBase;
import org.cubeengine.butler.CommandDescriptor;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.Dispatcher;
import org.cubeengine.butler.DispatcherCommand;
import org.cubeengine.butler.provider.Providers;
import org.cubeengine.butler.alias.AliasCommand;
import org.cubeengine.butler.builder.CommandBuilder;
import org.cubeengine.butler.parametric.InvokableMethod;
import org.cubeengine.butler.parametric.builder.ParametricBuilder;
import org.cubeengine.libcube.service.command.completer.PlayerCompleter;
import org.cubeengine.libcube.service.command.completer.PlayerListCompleter;
import org.cubeengine.libcube.service.command.exception.CommandExceptionHandler;
import org.cubeengine.libcube.service.command.exception.UnknownExceptionHandler;
import org.cubeengine.libcube.service.command.exception.UnknownSourceExceptionHandler;
import org.cubeengine.libcube.service.command.parser.BooleanParser;
import org.cubeengine.libcube.service.command.parser.ByteParser;
import org.cubeengine.libcube.service.command.parser.CommandSourceParser;
import org.cubeengine.libcube.service.command.parser.ContextParser;
import org.cubeengine.libcube.service.command.parser.DifficultyParser;
import org.cubeengine.libcube.service.command.parser.DimensionTypeParser;
import org.cubeengine.libcube.service.command.parser.DoubleParser;
import org.cubeengine.libcube.service.command.parser.DyeColorParser;
import org.cubeengine.libcube.service.command.parser.EnchantmentParser;
import org.cubeengine.libcube.service.command.parser.EntityTypeParser;
import org.cubeengine.libcube.service.command.parser.FindUserParser;
import org.cubeengine.libcube.service.command.parser.FloatParser;
import org.cubeengine.libcube.service.command.parser.GameModeParser;
import org.cubeengine.libcube.service.command.parser.GeneratorTypeParser;
import org.cubeengine.libcube.service.command.parser.IntParser;
import org.cubeengine.libcube.service.command.parser.ItemStackParser;
import org.cubeengine.libcube.service.command.parser.LogLevelParser;
import org.cubeengine.libcube.service.command.parser.LongParser;
import org.cubeengine.libcube.service.command.parser.PlayerList;
import org.cubeengine.libcube.service.command.parser.PlayerList.UserListParser;
import org.cubeengine.libcube.service.command.parser.ProfessionParser;
import org.cubeengine.libcube.service.command.parser.ShortParser;
import org.cubeengine.libcube.service.command.parser.UserParser;
import org.cubeengine.libcube.service.command.parser.WorldPropertiesParser;
import org.cubeengine.libcube.service.command.parser.WorldParser;
import org.cubeengine.libcube.service.filesystem.FileManager;
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
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.difficulty.Difficulty;
import org.spongepowered.api.world.storage.WorldProperties;

import static com.google.common.base.Preconditions.checkArgument;
import static org.spongepowered.api.Sponge.getGame;

@Singleton
public class CubeCommandManager extends DispatcherCommand implements CommandManager
{
    private static Thread mainThread = Thread.currentThread();
    public static boolean isMainThread()
    {
        return Thread.currentThread().equals(mainThread);
    }

    private Log commandLogger;

    private Providers providers;
    private org.spongepowered.api.command.CommandManager baseDispatcher;

    @Inject private I18n i18n;

    @Inject private ModuleManager mm;
    @Inject private FileManager fm;
    @Inject private PermissionManager pm;
    @Inject private Reflector reflector;

    @Inject private MaterialDataMatcher materialDataMatcher;
    @Inject private EnchantMatcher enchantMatcher;
    @Inject private MaterialMatcher materialMatcher;
    @Inject private ProfessionMatcher professionMatcher;
    @Inject private EntityMatcher entityMatcher;
    @Inject private StringMatcher stringMatcher;
    @Inject private UserMatcher um;
    @Inject private LogFactory lf;

    private Log logger;

    private CommandConfiguration config; // TODO use config

    private Map<Object, Set<CommandBase>> commands = new HashMap<>();
    private Map<CommandBase, CommandMapping> mappings = new HashMap<>();

    @Inject
    public CubeCommandManager()
    {
        super(new CommandManagerDescriptor());
    }

    public Object getPlugin()
    {
        return mm.getPlugin(LibCube.class).get();
    }

    @Inject
    public void init()
    {
        this.logger = mm.getLoggerFor(CommandManager.class);
        this.baseDispatcher = getGame().getCommandManager();

        this.providers = new Providers();
        this.providers.registerBuilder(InvokableMethod.class, new ParametricCommandBuilder(i18n));

        this.commandLogger = lf.getLog(CommandManager.class, "Commands");

        this.config = reflector.load(CommandConfiguration.class, fm.getDataPath().resolve("command.yml").toFile());

        providers.getExceptionHandler().addHandler(new UnknownSourceExceptionHandler(logger));
        providers.getExceptionHandler().addHandler(new CommandExceptionHandler(logger, i18n));
        providers.getExceptionHandler().addHandler(new UnknownExceptionHandler(logger, i18n));

        providers.register(this, new CommandContextValue(i18n), CommandContext.class);
        providers.register(this, new LocaleContextValue(i18n), Locale.class);

        providers.register(CommandManager.class, new PlayerCompleter(), User.class);
        providers.register(CommandManager.class, new PlayerListCompleter(getGame()), PlayerListCompleter.class);

        providers.register(CommandManager.class, new ByteParser(i18n), Byte.class, byte.class);
        providers.register(CommandManager.class, new ShortParser(i18n), Short.class, short.class);
        providers.register(CommandManager.class, new IntParser(i18n), Integer.class, int.class);
        providers.register(CommandManager.class, new LongParser(i18n), Long.class, long.class);
        providers.register(CommandManager.class, new FloatParser(i18n), Float.class, float.class);
        providers.register(CommandManager.class, new DoubleParser(i18n), Double.class, double.class);

        providers.register(CommandManager.class, new BooleanParser(i18n), Boolean.class, boolean.class);
        providers.register(CommandManager.class, new EnchantmentParser(enchantMatcher, getGame(), i18n), Enchantment.class);
        providers.register(CommandManager.class, new ItemStackParser(materialMatcher, i18n), ItemStack.class);
        providers.register(CommandManager.class, new ItemTypeParser(i18n), ItemType.class);
        providers.register(CommandManager.class, new BlockTypeParser(i18n), BlockType.class);
        providers.register(CommandManager.class, new CommandSourceParser(), CommandSource.class, Player.class);
        providers.register(CommandManager.class, new WorldParser(i18n), World.class);
        providers.register(CommandManager.class, new WorldPropertiesParser(i18n), WorldProperties.class);
        providers.register(CommandManager.class, new EntityTypeParser(entityMatcher), EntityType.class);

        providers.register(CommandManager.class, new DyeColorParser(materialDataMatcher), DyeColor.class);
        providers.register(CommandManager.class, new ProfessionParser(professionMatcher), Profession.class);
        providers.register(CommandManager.class, new UserParser(i18n, um), User.class);
        providers.register(CommandManager.class, new FindUserParser(i18n, um));
        providers.register(CommandManager.class, new DimensionTypeParser(), DimensionType.class);
        providers.register(CommandManager.class, new GameModeParser(), GameMode.class);
        providers.register(CommandManager.class, new DifficultyParser(i18n), Difficulty.class);
        providers.register(CommandManager.class, new GeneratorTypeParser(), GeneratorType.class);
        providers.register(CommandManager.class, new LogLevelParser(i18n), LogLevel.class);

        providers.register(CommandManager.class, new UserListParser(getGame()), PlayerList.class);

        providers.register(CommandManager.class, new ContextParser(), Context.class);


        Sponge.getEventManager().registerListeners(mm.getPlugin(LibCube.class).get(), new PreCommandListener(i18n, stringMatcher));
    }

    @Override
    public CommandManagerDescriptor getDescriptor()
    {
        return (CommandManagerDescriptor)super.getDescriptor();
    }

    @Override
    public Providers getProviders()
    {
        return providers;
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
            Set<CommandBase> cmds = commands.computeIfAbsent(((AliasCommand) command).getTarget(), k -> new HashSet<>());
            cmds.add(command);
        }
        if (command.getDescriptor() instanceof CubeDescriptor)
        {
            CubeDescriptor descriptor = (CubeDescriptor)command.getDescriptor();

            String name = mm.getModuleName(descriptor.getOwner()).orElse(descriptor.getOwner().getSimpleName());
            Permission parent = pm.register(descriptor.getOwner(), "command", "Allows using all commands of " + name, null);
            descriptor.registerPermission(pm, parent);
        }

        boolean b = super.addCommand(command);

        if (!(command instanceof AliasCommand) || ((AliasDescriptor) command.getDescriptor()).mainDescriptor().getDispatcher() != this)
        {
            Optional<CommandMapping> mapping = registerSpongeCommand(command.getDescriptor());
            if (mapping.isPresent())
            {
                mappings.put(command, mapping.get());
                commandLogger.debug("Registered command: " + mapping.get().getPrimaryAlias());
                return b;
            }
            commandLogger.warn("Command was not registered successfully!");
        }
        return b;
    }

    private Optional<CommandMapping> registerSpongeCommand(CommandDescriptor descriptor)
    {
        ArrayList<String> aliasList = new ArrayList<>();
        aliasList.add(descriptor.getName());
        for (AliasConfiguration alias : descriptor.getAliases())
        {
            if ((alias.getDispatcher() == null || (alias.getDispatcher() != null && alias.getDispatcher().length == 0)))
            {
                aliasList.add(alias.getName().toLowerCase());
            }
        }
        PluginContainer plugin = mm.getPlugin(descriptor.getOwner()).orElse(mm.getPlugin(LibCube.class).get());
        return baseDispatcher.register(plugin, new ProxyCallable(this, descriptor.getName(), logger), aliasList);
    }

    @Override
    public boolean runCommand(CommandSource sender, String commandLine)
    {
        checkArgument(isMainThread(), "Commands may only be called synchronously!");
        if (sender == null)
        {
            return execute(new CommandInvocation(null, commandLine, providers));
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
            CommandBuilder<InvokableMethod> builder = getProviders().getBuilder(InvokableMethod.class);
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

    private Map<Class, Dispatcher> injectedDispatchers = new HashMap<>();

    public void injectCommands(Injector injector, Object module, List<Field> fields)
    {
        for (Field field : fields)
        {
            Class<? extends ContainerCommand> parent = field.getAnnotation(ModuleCommand.class).value();
            Dispatcher dispatcher = parent == ContainerCommand.class ? this : getDispatcher(parent);
            Object command = injector.getInstance(field.getType());
            boolean isCommand = CommandBase.class.isAssignableFrom(command.getClass());
            if (isCommand)
            {
                dispatcher.addCommand(((CommandBase)command));
                if (command instanceof Dispatcher)
                {
                    this.injectedDispatchers.put(command.getClass(), ((Dispatcher) command));
                }
            }
            else
            {
                addCommands(dispatcher, module, command);
            }
            try
            {
                field.setAccessible(true);
                field.set(module, command);
            }
            catch (IllegalAccessException e)
            {
                throw new IllegalStateException(e);
            }
        }
    }

    private Dispatcher getDispatcher(Class<? extends ContainerCommand> command)
    {
        return this.injectedDispatchers.get(command); // TODO
    }

    // TODO remove?
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
