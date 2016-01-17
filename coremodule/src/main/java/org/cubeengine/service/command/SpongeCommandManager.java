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
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import javax.inject.Inject;
import java.util.Optional;
import com.google.common.base.Preconditions;
import de.cubeisland.engine.modularity.core.Modularity;
import org.cubeengine.butler.CommandBase;
import org.cubeengine.butler.CommandBuilder;
import org.cubeengine.butler.CommandDescriptor;
import org.cubeengine.butler.CommandInvocation;
import org.cubeengine.butler.Dispatcher;
import org.cubeengine.butler.DispatcherCommand;
import org.cubeengine.butler.ProviderManager;
import org.cubeengine.butler.alias.AliasCommand;
import org.cubeengine.butler.parametric.BasicParametricCommand;
import org.cubeengine.butler.parametric.CompositeCommandBuilder;
import org.cubeengine.butler.parametric.ParametricBuilder;
import de.cubeisland.engine.logscribe.LogFactory;
import de.cubeisland.engine.logscribe.target.file.AsyncFileTarget;
import de.cubeisland.engine.modularity.core.marker.Disable;
import de.cubeisland.engine.modularity.core.marker.Enable;
import de.cubeisland.engine.modularity.asm.marker.ServiceImpl;
import de.cubeisland.engine.modularity.asm.marker.Version;
import de.cubeisland.engine.modularity.core.Module;

import org.cubeengine.service.matcher.*;
import org.cubeengine.service.command.completer.*;
import org.cubeengine.service.command.exception.CommandExceptionHandler;
import org.cubeengine.service.command.exception.UnknownExceptionHandler;
import org.cubeengine.service.command.exception.UnknownSourceExceptionHandler;
import org.cubeengine.service.command.readers.*;
import org.cubeengine.service.filesystem.FileManager;
import org.cubeengine.service.i18n.I18n;
import org.cubeengine.service.logging.LoggingUtil;
import org.cubeengine.service.permission.PermissionManager;
import org.cubeengine.module.core.CoreModule;
import org.cubeengine.service.event.EventManager;
import org.cubeengine.service.user.UserList;
import org.cubeengine.service.user.UserList.UserListReader;
import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.logscribe.LogLevel;
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
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.GeneratorType;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.difficulty.Difficulty;

import static com.google.common.base.Preconditions.checkArgument;
import static org.cubeengine.module.core.CoreModule.isMainThread;

@ServiceImpl(CommandManager.class)
@Version(1)
public class SpongeCommandManager extends DispatcherCommand implements CommandManager
{
    private final ConsoleSource consoleSender;
    private final Log commandLogger;
    private final ProviderManager providerManager;
    private final CommandBuilder<BasicParametricCommand, CommandOrigin> builder;
    private final org.spongepowered.api.command.CommandManager baseDispatcher;

    private final Map<Module, Set<CommandMapping>> mappings = new HashMap<>();
    private final Object plugin;
    private CoreModule core;

    private I18n i18n;
    private FileManager fm;
    @Inject private CommandManager cm;
    @Inject private PermissionManager pm;
    @Inject private EventManager em;
    @Inject private ThreadFactory tf;
    @Inject private org.spongepowered.api.Game game;

    @Inject private MaterialDataMatcher materialDataMatcher;
    @Inject private EnchantMatcher enchantMatcher;
    @Inject private MaterialMatcher materialMatcher;
    @Inject private ProfessionMatcher professionMatcher;
    @Inject private EntityMatcher entityMatcher;
    @Inject private Modularity modularity;
    @Inject private StringMatcher stringMatcher;
    @Inject private UserMatcher um;

    @Inject
    public SpongeCommandManager(CoreModule core, org.spongepowered.api.Game game, LogFactory logFactory, I18n i18n, FileManager fm)
    {
        super(new CommandManagerDescriptor());
        this.core = core;
        this.i18n = i18n;
        this.fm = fm;
        this.plugin = game.getPluginManager().getPlugin("CubeEngine").get().getInstance().get();
        this.baseDispatcher = game.getCommandManager();

        this.consoleSender = game.getServer().getConsole();

        this.builder = new CompositeCommandBuilder(new ParametricCommandBuilder(i18n));

        this.commandLogger = logFactory.getLog(CoreModule.class, "Commands");


        this.providerManager = new ProviderManager();
        providerManager.getExceptionHandler().addHandler(new UnknownSourceExceptionHandler(core.getLog()));
        providerManager.getExceptionHandler().addHandler(new CommandExceptionHandler(core.getLog(), i18n));
        providerManager.getExceptionHandler().addHandler(new UnknownExceptionHandler(core.getLog(), i18n));

        providerManager.register(this, new CommandContextValue(i18n), CommandContext.class);
        providerManager.register(this, new LocaleContextValue(i18n), Locale.class);
    }

    @Enable
    public void onEnable()
    {
        providerManager.register(core, new PlayerCompleter(game), User.class);
        providerManager.register(core, new PlayerListCompleter(game), PlayerListCompleter.class);

        providerManager.register(core, new ByteReader(i18n), Byte.class, byte.class);
        providerManager.register(core, new ShortReader(i18n), Short.class, short.class);
        providerManager.register(core, new IntReader(i18n), Integer.class, int.class);
        providerManager.register(core, new LongReader(i18n), Long.class, long.class);
        providerManager.register(core, new FloatReader(i18n), Float.class, float.class);
        providerManager.register(core, new DoubleReader(i18n), Double.class, double.class);

        providerManager.register(core, new BooleanReader(i18n), Boolean.class, boolean.class);
        providerManager.register(core, new EnchantmentReader(enchantMatcher, game, i18n), Enchantment.class);
        providerManager.register(core, new ItemStackReader(materialMatcher, i18n), ItemStack.class);
        providerManager.register(core, new CommandSourceReader(cm, game), CommandSource.class, Player.class);
        providerManager.register(core, new WorldReader(game, i18n), World.class);
        providerManager.register(core, new EntityTypeReader(entityMatcher), EntityType.class);

        providerManager.register(core, new DyeColorReader(materialDataMatcher), DyeColor.class);
        providerManager.register(core, new ProfessionReader(professionMatcher), Profession.class);
        providerManager.register(core, new UserReader(i18n, um), User.class);
        providerManager.register(core, new FindUserReader(i18n, um));
        providerManager.register(core, new DimensionTypeReader(), DimensionType.class);
        providerManager.register(core, new GameModeReader(), GameMode.class);
        providerManager.register(core, new DifficultyReader(i18n), Difficulty.class);
        providerManager.register(core, new GeneratorTypeReader(), GeneratorType.class);
        providerManager.register(core, new LogLevelReader(i18n), LogLevel.class);

        providerManager.register(core, new UserListReader(game), UserList.class);

        em.registerListener(core, new PreCommandListener(core, i18n, stringMatcher, game)); // TODO register later?
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

    @Override
    public void removeCommands(Module module)
    {
        Set<CommandMapping> byModule = mappings.get(module);
        if (byModule != null)
        {
            byModule.forEach(baseDispatcher::removeMapping);
        }
    }

    @Override
    public void removeCommands()
    {
        // TODO remove all registered commands by us
    }

    @Override
    public boolean addCommand(CommandBase command)
    {
        Module module = core;
        if (command.getDescriptor() instanceof CubeDescriptor)
        {
            CubeDescriptor descriptor = (CubeDescriptor)command.getDescriptor();
            module = descriptor.getModule();

            PermissionDescription parent = pm.register(module, "command", "Allows using all commands of " + module.getInformation().getName(), null);
            descriptor.getPermission().registerPermission(module, pm, parent);
        }
        else if (command instanceof AliasCommand)
        {
            if (((AliasCommand)command).getTarget().getDescriptor() instanceof CubeDescriptor)
            {
                module = ((CubeDescriptor)((AliasCommand)command).getTarget().getDescriptor()).getModule();
            }
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
        return baseDispatcher.register(plugin, new ProxyCallable(core, this, name), name);
    }

    @Override
    public void logCommands(boolean logCommands)
    {
        if (logCommands)
        {
            commandLogger.addTarget(new AsyncFileTarget(LoggingUtil.getLogFile(fm, "Commands"),
                                                        LoggingUtil.getFileFormat(true, false),
                                                        true, LoggingUtil.getCycler(),
                                                        tf));
        }

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
}
