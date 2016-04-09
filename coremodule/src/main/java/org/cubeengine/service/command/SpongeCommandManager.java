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
package org.cubeengine.service.command;

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
import de.cubeisland.engine.modularity.core.Module;
import de.cubeisland.engine.modularity.core.ModuleHandler;
import de.cubeisland.engine.modularity.core.marker.Enable;
import org.cubeengine.butler.CommandBase;
import org.cubeengine.butler.CommandDescriptor;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.Dispatcher;
import org.cubeengine.butler.DispatcherCommand;
import org.cubeengine.butler.ProviderManager;
import org.cubeengine.butler.alias.AliasCommand;
import org.cubeengine.butler.builder.CommandBuilder;
import org.cubeengine.butler.parametric.BasicParametricCommand;
import org.cubeengine.butler.parametric.CompositeCommandBuilder;
import org.cubeengine.butler.parametric.builder.ParametricBuilder;
import org.cubeengine.service.command.completer.PlayerCompleter;
import org.cubeengine.service.command.completer.PlayerListCompleter;
import org.cubeengine.service.command.exception.CommandExceptionHandler;
import org.cubeengine.service.command.exception.UnknownExceptionHandler;
import org.cubeengine.service.command.exception.UnknownSourceExceptionHandler;
import org.cubeengine.service.command.readers.BooleanReader;
import org.cubeengine.service.command.readers.ByteReader;
import org.cubeengine.service.command.readers.CommandSourceReader;
import org.cubeengine.service.command.readers.DifficultyReader;
import org.cubeengine.service.command.readers.DimensionTypeReader;
import org.cubeengine.service.command.readers.DoubleReader;
import org.cubeengine.service.command.readers.DyeColorReader;
import org.cubeengine.service.command.readers.EnchantmentReader;
import org.cubeengine.service.command.readers.EntityTypeReader;
import org.cubeengine.service.command.readers.FindUserReader;
import org.cubeengine.service.command.readers.FloatReader;
import org.cubeengine.service.command.readers.GameModeReader;
import org.cubeengine.service.command.readers.GeneratorTypeReader;
import org.cubeengine.service.command.readers.IntReader;
import org.cubeengine.service.command.readers.ItemStackReader;
import org.cubeengine.service.command.readers.LogLevelReader;
import org.cubeengine.service.command.readers.LongReader;
import org.cubeengine.service.command.readers.ProfessionReader;
import org.cubeengine.service.command.readers.ShortReader;
import org.cubeengine.service.command.readers.UserReader;
import org.cubeengine.service.command.readers.WorldPropertiesReader;
import org.cubeengine.service.command.readers.WorldReader;
import org.cubeengine.service.command.readers.ContextReader;
import org.cubeengine.service.i18n.I18n;
import org.cubeengine.service.matcher.EnchantMatcher;
import org.cubeengine.service.matcher.EntityMatcher;
import org.cubeengine.service.matcher.MaterialDataMatcher;
import org.cubeengine.service.matcher.MaterialMatcher;
import org.cubeengine.service.matcher.ProfessionMatcher;
import org.cubeengine.service.matcher.StringMatcher;
import org.cubeengine.service.matcher.UserMatcher;
import org.cubeengine.service.permission.PermissionManager;
import org.cubeengine.service.user.UserList;
import org.cubeengine.service.user.UserList.UserListReader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.inventory.ItemStack;
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
public class SpongeCommandManager extends DispatcherCommand implements CommandManager, ModuleHandler
{
    private static Thread mainThread = Thread.currentThread();

    public static boolean isMainThread()
    {
        return Thread.currentThread().equals(mainThread);
    }

    private final ConsoleSource consoleSender;
    private final Log commandLogger;
    private final ProviderManager providerManager;
    private final CommandBuilder<BasicParametricCommand, CommandOrigin> builder;
    private final org.spongepowered.api.command.CommandManager baseDispatcher;

    private final Map<Module, Set<CommandMapping>> mappings = new HashMap<>();
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
    
    @Inject Log logger;

    @Inject
    public SpongeCommandManager(LogFactory logFactory, I18n i18n, Log logger, Modularity modularity)
    {
        super(new CommandManagerDescriptor());
        this.i18n = i18n;
        this.plugin = getGame().getPluginManager().getPlugin("org.cubeengine").get().getInstance().get();
        this.baseDispatcher = getGame().getCommandManager();

        this.consoleSender = getGame().getServer().getConsole();

        this.providerManager = new ProviderManager();

        this.builder = new CompositeCommandBuilder(new ParametricCommandBuilder(i18n));

        this.commandLogger = logFactory.getLog(CommandManager.class, "Commands");

        providerManager.getExceptionHandler().addHandler(new UnknownSourceExceptionHandler(logger));
        providerManager.getExceptionHandler().addHandler(new CommandExceptionHandler(logger, i18n));
        providerManager.getExceptionHandler().addHandler(new UnknownExceptionHandler(logger, i18n));

        providerManager.register(this, new CommandContextValue(i18n), CommandContext.class);
        providerManager.register(this, new LocaleContextValue(i18n), Locale.class);

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
        providerManager.register(CommandManager.class, new CommandSourceReader(this, getGame()), CommandSource.class, Player.class);
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

        providerManager.register(CommandManager.class, new UserListReader(getGame()), UserList.class);

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
    public CommandBuilder<BasicParametricCommand, CommandOrigin> getCommandBuilder()
    {
        return this.builder;
    }

    private void removeCommands(Module module)
    {
        Set<CommandMapping> byModule = mappings.get(module);
        if (byModule != null)
        {
            byModule.forEach(baseDispatcher::removeMapping);
        }
    }

    @Override
    public boolean addCommand(CommandBase command)
    {
        Module module = null;
        if (command.getDescriptor() instanceof CubeDescriptor)
        {
            CubeDescriptor descriptor = (CubeDescriptor)command.getDescriptor();
            module = descriptor.getModule();

            PermissionDescription parent = pm.register(module, "command", "Allows using all commands of " + module.getInformation().getName(), null);
            descriptor.registerPermission(pm, parent);
        }
        else if (command instanceof AliasCommand)
        {
            if (((AliasCommand)command).getTarget().getDescriptor() instanceof CubeDescriptor)
            {
                module = ((CubeDescriptor)((AliasCommand)command).getTarget().getDescriptor()).getModule();
            }
        }

        if (module == null)
        {
            throw new IllegalArgumentException("Tried to register a non CubeEngine command: " + command.getDescriptor().getName());
        }

        boolean b = super.addCommand(command);

        Optional<CommandMapping> mapping = registerSpongeCommand(command.getDescriptor().getName());
        if (mapping.isPresent())
        {
            Set<CommandMapping> byModule = mappings.get(module);
            if (byModule == null)
            {
                byModule = new HashSet<>();
                mappings.put(module, byModule);
            }
            byModule.add(mapping.get());
            return b;
        }
        module.getProvided(Log.class).warn("Command was not registered successfully!");
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
    public ConsoleSource getConsoleSender()
    {
        return this.consoleSender;
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
    public Dispatcher getBaseDispatcher()
    {
        return this;
    }

    /**
     * Creates {@link org.cubeengine.butler.parametric.BasicParametricCommand} for all methods annotated as a command
     * in the given commandHolder and add them to the given dispatcher
     *
     * @param dispatcher    the dispatcher to add the commands to
     * @param module        the module owning the commands
     * @param commandHolder the command holder containing the command-methods
     */
    @SuppressWarnings("unchecked")
    @Override
    public void addCommands(Dispatcher dispatcher, Module module, Object commandHolder)
    {
        for (Method method : ParametricBuilder.getMethods(commandHolder.getClass()))
        {
            BasicParametricCommand cmd = this.getCommandBuilder().buildCommand(new CommandOrigin(method, commandHolder, module));
            if (cmd != null)
            {
                dispatcher.addCommand(cmd);
            }
        }
    }

    @Override
    public void addCommands(Module module, Object commandHolder)
    {
        this.addCommands(this, module, commandHolder);
    }


    @Override
    public void onEnable(Module module)
    {
        // TODO automagically register commands?
    }

    @Override
    public void onDisable(Module module)
    {
        removeCommands(module);
    }
}
