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
import de.cubeisland.engine.butler.parametric.BasicParametricCommand;
import de.cubeisland.engine.butler.parametric.CompositeCommandBuilder;
import de.cubeisland.engine.butler.parametric.ParametricBuilder;
import de.cubeisland.engine.logscribe.LogFactory;
import de.cubeisland.engine.logscribe.target.file.AsyncFileTarget;
import de.cubeisland.engine.modularity.asm.marker.ServiceImpl;
import de.cubeisland.engine.modularity.asm.marker.Version;
import de.cubeisland.engine.modularity.core.Module;

import de.cubeisland.engine.module.core.CubeEngine;
import de.cubeisland.engine.module.core.command.CommandManager;
import de.cubeisland.engine.module.core.command.CommandManagerDescriptor;
import de.cubeisland.engine.module.core.command.CommandOrigin;
import de.cubeisland.engine.module.core.command.CommandSender;
import de.cubeisland.engine.module.core.command.CubeCommandDescriptor;
import de.cubeisland.engine.module.core.command.CubeDescriptor;
import de.cubeisland.engine.module.core.command.ExceptionHandler;
import de.cubeisland.engine.module.core.command.ParametricCommandBuilder;
import de.cubeisland.engine.module.core.command.completer.ModuleCompleter;
import de.cubeisland.engine.module.core.command.completer.PlayerCompleter;
import de.cubeisland.engine.module.core.command.completer.PlayerListCompleter;
import de.cubeisland.engine.module.core.command.completer.WorldCompleter;
import de.cubeisland.engine.module.core.command.readers.BooleanReader;
import de.cubeisland.engine.module.core.command.readers.ByteReader;
import de.cubeisland.engine.module.core.command.readers.CommandSenderReader;
import de.cubeisland.engine.module.core.command.readers.DifficultyReader;
import de.cubeisland.engine.module.core.command.readers.DimensionTypeReader;
import de.cubeisland.engine.module.core.command.readers.DoubleReader;
import de.cubeisland.engine.module.core.command.readers.DyeColorReader;
import de.cubeisland.engine.module.core.command.readers.EnchantmentReader;
import de.cubeisland.engine.module.core.command.readers.EntityTypeReader;
import de.cubeisland.engine.module.core.command.readers.FloatReader;
import de.cubeisland.engine.module.core.command.readers.IntReader;
import de.cubeisland.engine.module.core.command.readers.ItemStackReader;
import de.cubeisland.engine.module.core.command.readers.LogLevelReader;
import de.cubeisland.engine.module.core.command.readers.LongReader;
import de.cubeisland.engine.module.core.command.readers.OfflinePlayerReader;
import de.cubeisland.engine.module.core.command.readers.ProfessionReader;
import de.cubeisland.engine.module.core.command.readers.ShortReader;
import de.cubeisland.engine.module.core.command.readers.UserReader;
import de.cubeisland.engine.module.core.command.readers.WorldReader;
import de.cubeisland.engine.module.core.command.sender.ConsoleCommandSender;
import de.cubeisland.engine.module.core.command.sender.WrappedCommandSender;
import de.cubeisland.engine.module.core.filesystem.FileManager;
import de.cubeisland.engine.module.core.i18n.I18n;
import de.cubeisland.engine.module.core.logging.LoggingUtil;
import de.cubeisland.engine.module.core.permission.Permission;
import de.cubeisland.engine.module.core.permission.PermissionManager;
import de.cubeisland.engine.module.core.sponge.command.PreCommandListener;
import de.cubeisland.engine.module.core.sponge.command.ProxyCallable;
import de.cubeisland.engine.module.core.user.User;
import de.cubeisland.engine.module.core.user.UserList;
import de.cubeisland.engine.module.core.user.UserList.UserListReader;
import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.logscribe.LogLevel;
import de.cubeisland.engine.module.core.util.matcher.MaterialDataMatcher;
import org.spongepowered.api.data.types.DyeColor;
import org.spongepowered.api.data.types.Profession;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.item.Enchantment;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.service.command.CommandService;
import org.spongepowered.api.util.command.CommandMapping;
import org.spongepowered.api.world.DimensionType;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.difficulty.Difficulty;

import static de.cubeisland.engine.module.core.contract.Contract.expect;

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

    private SpongeCore core;

    @Override
    public CommandManagerDescriptor getDescriptor()
    {
        return (CommandManagerDescriptor)super.getDescriptor();
    }

    @Inject
    public SpongeCommandManager(SpongeCore core)
    {
        super(new CommandManagerDescriptor());
        this.core = core;
        this.baseDispatcher = core.getGame().getCommandDispatcher();

        this.consoleSender = new ConsoleCommandSender(core);

        this.builder = new CompositeCommandBuilder<>(new ParametricCommandBuilder());



        this.commandLogger = core.getModularity().start(LogFactory.class).getLog(SpongeCore.class, "Commands");
        if (core.getConfiguration().logging.logCommands)
        {
            commandLogger.addTarget(new AsyncFileTarget(LoggingUtil.getLogFile(core.getModularity().start(FileManager.class), "Commands"),
                                                   LoggingUtil.getFileFormat(true, false),
                                                   true, LoggingUtil.getCycler(),
                                                   core.getProvided(ThreadFactory.class)));
        }

        this.providerManager = new ProviderManager();
        providerManager.getExceptionHandler().addHandler(new ExceptionHandler(core));

        registerReaders(core, core.getModularity().start(EventManager.class));

    }

    public void registerReaders(SpongeCore core, EventManager em)
    {
        I18n i18n = core.getModularity().start(I18n.class);

        providerManager.register(core, new PlayerCompleter(core), User.class, org.spongepowered.api.entity.player.User.class);
        providerManager.register(core, new WorldCompleter(core.getGame().getServer()), World.class);
        providerManager.register(core, new PlayerListCompleter(core), PlayerListCompleter.class);

        providerManager.register(core, new ByteReader(i18n), Byte.class, byte.class);
        providerManager.register(core, new ShortReader(i18n), Short.class, short.class);
        providerManager.register(core, new IntReader(i18n), Integer.class, int.class);
        providerManager.register(core, new LongReader(i18n), Long.class, long.class);
        providerManager.register(core, new FloatReader(i18n), Float.class, float.class);
        providerManager.register(core, new DoubleReader(i18n), Double.class, double.class);

        providerManager.register(core, new BooleanReader(core), Boolean.class, boolean.class);
        providerManager.register(core, new EnchantmentReader(core), Enchantment.class);
        providerManager.register(core, new ItemStackReader(core.getModularity()), ItemStack.class);
        providerManager.register(core, new UserReader(core), User.class);
        providerManager.register(core, new CommandSenderReader(core), CommandSender.class);
        providerManager.register(core, new WorldReader(core), World.class);
        providerManager.register(core, new EntityTypeReader(core), EntityType.class);
        providerManager.register(core, new DyeColorReader(core.getModularity().start(MaterialDataMatcher.class)), DyeColor.class);
        providerManager.register(core, new ProfessionReader(), Profession.class);
        providerManager.register(core, new OfflinePlayerReader(core), org.spongepowered.api.entity.player.User.class);
        providerManager.register(core, new DimensionTypeReader(core.getGame()), DimensionType.class);
        providerManager.register(core, new DifficultyReader(core.getGame()), Difficulty.class);
        providerManager.register(core, new LogLevelReader(i18n), LogLevel.class);

        UserListReader userListReader = new UserListReader(core.getModularity());
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
            module = ((CubeDescriptor)command.getDescriptor()).getModule();
            Permission perm = module.getProvided(Permission.class).childWildcard("command");
            Permission childPerm = ((CubeDescriptor)command.getDescriptor()).getPermission();
            childPerm.setParent(perm);
            this.core.getModularity().start(PermissionManager.class).registerPermission(module, childPerm);
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
        return baseDispatcher.register(core, new ProxyCallable(core, this, name), name);
    }

    @Override
    public boolean runCommand(CommandSender sender, String commandLine)
    {
        expect(CubeEngine.isMainThread(), "Commands may only be called synchronously!");
        org.spongepowered.api.util.command.CommandSource source = null;
        if (sender instanceof User)
        {
            source = ((User)sender).getPlayer().get();
        }
        else if (sender instanceof WrappedCommandSender)
        {
            source = ((WrappedCommandSender)sender).getWrappedSender();
        }
        if (source == null)
        {
            return execute(new CommandInvocation(sender, commandLine, providerManager));
        }
        return baseDispatcher.process(source, commandLine).isPresent();
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


}
