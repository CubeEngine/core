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
package de.cubeisland.engine.service.command;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import javax.inject.Inject;
import com.google.common.base.Optional;
import de.cubeisland.engine.butler.CommandBase;
import de.cubeisland.engine.butler.CommandBuilder;
import de.cubeisland.engine.butler.CommandDescriptor;
import de.cubeisland.engine.butler.CommandInvocation;
import de.cubeisland.engine.butler.CommandSource;
import de.cubeisland.engine.butler.Dispatcher;
import de.cubeisland.engine.butler.DispatcherCommand;
import de.cubeisland.engine.butler.ProviderManager;
import de.cubeisland.engine.butler.alias.AliasCommand;
import de.cubeisland.engine.butler.parametric.BasicParametricCommand;
import de.cubeisland.engine.butler.parametric.CompositeCommandBuilder;
import de.cubeisland.engine.butler.parametric.ParametricBuilder;
import de.cubeisland.engine.logscribe.LogFactory;
import de.cubeisland.engine.logscribe.target.file.AsyncFileTarget;
import de.cubeisland.engine.modularity.core.marker.Disable;
import de.cubeisland.engine.modularity.core.marker.Enable;
import de.cubeisland.engine.modularity.asm.marker.ServiceImpl;
import de.cubeisland.engine.modularity.asm.marker.Version;
import de.cubeisland.engine.modularity.core.Module;

import de.cubeisland.engine.module.core.util.matcher.EnchantMatcher;
import de.cubeisland.engine.module.core.util.matcher.EntityMatcher;
import de.cubeisland.engine.module.core.util.matcher.MaterialMatcher;
import de.cubeisland.engine.module.core.util.matcher.ProfessionMatcher;
import de.cubeisland.engine.service.command.completer.ModuleCompleter;
import de.cubeisland.engine.service.command.completer.PlayerCompleter;
import de.cubeisland.engine.service.command.completer.PlayerListCompleter;
import de.cubeisland.engine.service.command.completer.WorldCompleter;
import de.cubeisland.engine.service.command.readers.BooleanReader;
import de.cubeisland.engine.service.command.readers.ByteReader;
import de.cubeisland.engine.service.command.readers.CommandSenderReader;
import de.cubeisland.engine.service.command.readers.DifficultyReader;
import de.cubeisland.engine.service.command.readers.DimensionTypeReader;
import de.cubeisland.engine.service.command.readers.DoubleReader;
import de.cubeisland.engine.service.command.readers.DyeColorReader;
import de.cubeisland.engine.service.command.readers.EnchantmentReader;
import de.cubeisland.engine.service.command.readers.EntityTypeReader;
import de.cubeisland.engine.service.command.readers.FloatReader;
import de.cubeisland.engine.service.command.readers.IntReader;
import de.cubeisland.engine.service.command.readers.ItemStackReader;
import de.cubeisland.engine.service.command.readers.LogLevelReader;
import de.cubeisland.engine.service.command.readers.LongReader;
import de.cubeisland.engine.service.command.readers.OfflinePlayerReader;
import de.cubeisland.engine.service.command.readers.ProfessionReader;
import de.cubeisland.engine.service.command.readers.ShortReader;
import de.cubeisland.engine.service.command.readers.UserReader;
import de.cubeisland.engine.service.command.readers.WorldReader;
import de.cubeisland.engine.service.command.sender.ConsoleCommandSender;
import de.cubeisland.engine.service.command.sender.WrappedCommandSender;
import de.cubeisland.engine.service.filesystem.FileManager;
import de.cubeisland.engine.service.i18n.I18n;
import de.cubeisland.engine.service.logging.LoggingUtil;
import de.cubeisland.engine.service.permission.PermissionManager;
import de.cubeisland.engine.module.core.sponge.CoreModule;
import de.cubeisland.engine.module.core.sponge.EventManager;
import de.cubeisland.engine.service.user.User;
import de.cubeisland.engine.service.user.UserList;
import de.cubeisland.engine.service.user.UserList.UserListReader;
import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.logscribe.LogLevel;
import de.cubeisland.engine.module.core.util.matcher.MaterialDataMatcher;
import de.cubeisland.engine.service.user.UserManager;
import de.cubeisland.engine.service.world.WorldManager;
import org.spongepowered.api.Game;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.command.CommandService;
import org.spongepowered.api.service.permission.PermissionDescription;
import org.spongepowered.api.util.command.CommandMapping;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.difficulty.Difficulty;

import static de.cubeisland.engine.module.core.contract.Contract.expect;
import static de.cubeisland.engine.module.core.sponge.CoreModule.isMainThread;

@ServiceImpl(CommandManager.class)
@Version(1)
public class SpongeCommandManager extends DispatcherCommand implements CommandManager
{
    private final ConsoleCommandSender consoleSender;
    private final Log commandLogger;
    private final ProviderManager providerManager;
    private final CommandBuilder<BasicParametricCommand, CommandOrigin> builder;
    private final CommandService baseDispatcher;

    private final Map<Module, Set<CommandMapping>> mappings = new HashMap<>();
    private final Object plugin;
    private CoreModule core;
    private I18n i18n;

    private FileManager fm;
    @Inject private CommandManager cm;
    @Inject private UserManager um;
    @Inject private PermissionManager pm;
    @Inject private EventManager em;
    @Inject private ThreadFactory tf;
    @Inject private Game game;

    @Inject
    public SpongeCommandManager(CoreModule core, Game game, LogFactory logFactory, I18n i18n, FileManager fm)
    {
        super(new CommandManagerDescriptor());
        this.fm = fm;
        this.core = core;
        this.i18n = i18n;
        this.plugin = game.getPluginManager().getPlugin("CubeEngine").get().getInstance();
        this.baseDispatcher = core.getGame().getCommandDispatcher();

        this.consoleSender = new ConsoleCommandSender(i18n, core.getGame().getServer().getConsole());

        this.builder = new CompositeCommandBuilder<>(new ParametricCommandBuilder());

        this.commandLogger = logFactory.getLog(CoreModule.class, "Commands");


        this.providerManager = new ProviderManager();
        providerManager.getExceptionHandler().addHandler(new ExceptionHandler(core));
    }

    @Enable
    public void onEnable()
    {
        registerReaders(core, em);
    }

    @Override
    public CommandManagerDescriptor getDescriptor()
    {
        return (CommandManagerDescriptor)super.getDescriptor();
    }


    public void registerReaders(CoreModule core, EventManager em)
    {
        I18n i18n = core.getModularity().provide(I18n.class);

        MaterialDataMatcher materialDataMatcher = core.getModularity().provide(MaterialDataMatcher.class);
        EnchantMatcher enchantMatcher = core.getModularity().provide(EnchantMatcher.class);
        MaterialMatcher materialMatcher = core.getModularity().provide(MaterialMatcher.class);
        ProfessionMatcher professionMatcher = core.getModularity().provide(ProfessionMatcher.class);
        EntityMatcher entityMatcher = core.getModularity().provide(EntityMatcher.class);
        WorldManager wm = core.getModularity().provide(WorldManager.class);

        providerManager.register(core, new PlayerCompleter(um), User.class, org.spongepowered.api.entity.player.User.class);
        providerManager.register(core, new WorldCompleter(core.getGame().getServer()), World.class);
        providerManager.register(core, new PlayerListCompleter(um), PlayerListCompleter.class);

        providerManager.register(core, new ByteReader(i18n), Byte.class, byte.class);
        providerManager.register(core, new ShortReader(i18n), Short.class, short.class);
        providerManager.register(core, new IntReader(i18n), Integer.class, int.class);
        providerManager.register(core, new LongReader(i18n), Long.class, long.class);
        providerManager.register(core, new FloatReader(i18n), Float.class, float.class);
        providerManager.register(core, new DoubleReader(i18n), Double.class, double.class);

        providerManager.register(core, new BooleanReader(i18n), Boolean.class, boolean.class);
        providerManager.register(core, new EnchantmentReader(enchantMatcher, core.getGame()), Enchantment.class);
        providerManager.register(core, new ItemStackReader(materialMatcher, i18n), ItemStack.class);
        providerManager.register(core, new UserReader(um, i18n), User.class);
        providerManager.register(core, new CommandSenderReader(cm), CommandSender.class);
        providerManager.register(core, new WorldReader(wm, i18n), World.class);
        providerManager.register(core, new EntityTypeReader(entityMatcher), EntityType.class);

        providerManager.register(core, new DyeColorReader(materialDataMatcher), DyeColor.class);
        providerManager.register(core, new ProfessionReader(professionMatcher), Profession.class);
        providerManager.register(core, new OfflinePlayerReader(game), org.spongepowered.api.entity.player.User.class);
        providerManager.register(core, new DimensionTypeReader(core.getGame()), DimensionType.class);
        providerManager.register(core, new DifficultyReader(i18n, core.getGame()), Difficulty.class);
        providerManager.register(core, new LogLevelReader(i18n), LogLevel.class);

        UserListReader userListReader = new UserListReader(um);
        providerManager.register(core, userListReader, UserList.class);

        providerManager.register(core, userListReader, UserList.class);

        providerManager.register(this, new ModuleCompleter(core.getModularity()), Module.class);

        em.registerListener(core, new PreCommandListener(core)); // TODO register later?
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
    @Disable
    public void clean() // TODO shutdown service
    {
        removeCommands();
    }

    @Override
    public boolean addCommand(CommandBase command)
    {
        Module module = core;
        if (command.getDescriptor() instanceof CubeDescriptor)
        {
            CubeDescriptor descriptor = (CubeDescriptor)command.getDescriptor();
            module = descriptor.getModule();

            PermissionDescription modulePerm = pm.getModulePermission(module);
            pm.register(module, "command." + descriptor.getPermission(), "Allows using the command " + command.getDescriptor().getName(), modulePerm);
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
    public boolean runCommand(CommandSender sender, String commandLine)
    {
        expect(isMainThread(), "Commands may only be called synchronously!");
        org.spongepowered.api.util.command.CommandSource source = null;
        if (sender instanceof User)
        {
            source = ((User)sender).asPlayer();
        }
        else if (sender instanceof WrappedCommandSender)
        {
            source = ((WrappedCommandSender)sender).getWrappedSender();
        }
        if (source == null)
        {
            return execute(new CommandInvocation(sender, commandLine, providerManager));
        }
        return baseDispatcher.process(source, commandLine).getSuccessCount().isPresent();
    }

    @Override
    public ConsoleCommandSender getConsoleSender()
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
     * Creates {@link de.cubeisland.engine.butler.parametric.BasicParametricCommand} for all methods annotated as a command
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
