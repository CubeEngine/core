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
package de.cubeisland.engine.core.sponge;

import java.lang.reflect.Method;
import de.cubeisland.engine.butler.CommandBase;
import de.cubeisland.engine.butler.CommandBuilder;
import de.cubeisland.engine.butler.CommandDescriptor;
import de.cubeisland.engine.butler.CommandSource;
import de.cubeisland.engine.butler.Dispatcher;
import de.cubeisland.engine.butler.DispatcherCommand;
import de.cubeisland.engine.butler.parametric.CompositeCommandBuilder;
import de.cubeisland.engine.butler.parametric.BasicParametricCommand;
import de.cubeisland.engine.butler.parametric.ParametricBuilder;
import de.cubeisland.engine.butler.ProviderManager;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.sponge.command.CommandInjector;
import de.cubeisland.engine.core.command.CommandManager;
import de.cubeisland.engine.core.command.CommandManagerDescriptor;
import de.cubeisland.engine.core.command.CommandOrigin;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.CubeCommandDescriptor;
import de.cubeisland.engine.core.command.CubeDescriptor;
import de.cubeisland.engine.core.command.ExceptionHandler;
import de.cubeisland.engine.core.command.ParametricCommandBuilder;
import de.cubeisland.engine.core.command.completer.ModuleCompleter;
import de.cubeisland.engine.core.command.completer.PlayerCompleter;
import de.cubeisland.engine.core.command.completer.PlayerListCompleter;
import de.cubeisland.engine.core.command.completer.WorldCompleter;
import de.cubeisland.engine.core.command.readers.BooleanReader;
import de.cubeisland.engine.core.command.readers.ByteReader;
import de.cubeisland.engine.core.command.readers.CommandSenderReader;
import de.cubeisland.engine.core.command.readers.DifficultyReader;
import de.cubeisland.engine.core.command.readers.DoubleReader;
import de.cubeisland.engine.core.command.readers.DyeColorReader;
import de.cubeisland.engine.core.command.readers.EnchantmentReader;
import de.cubeisland.engine.core.command.readers.EntityTypeReader;
import de.cubeisland.engine.core.command.readers.EnvironmentReader;
import de.cubeisland.engine.core.command.readers.FloatReader;
import de.cubeisland.engine.core.command.readers.IntReader;
import de.cubeisland.engine.core.command.readers.ItemStackReader;
import de.cubeisland.engine.core.command.readers.LogLevelReader;
import de.cubeisland.engine.core.command.readers.LongReader;
import de.cubeisland.engine.core.command.readers.OfflinePlayerReader;
import de.cubeisland.engine.core.command.readers.ProfessionReader;
import de.cubeisland.engine.core.command.readers.ShortReader;
import de.cubeisland.engine.core.command.readers.UserReader;
import de.cubeisland.engine.core.command.readers.WorldReader;
import de.cubeisland.engine.core.command.result.confirm.ConfirmManager;
import de.cubeisland.engine.core.command.result.paginated.PaginationManager;
import de.cubeisland.engine.core.command.sender.ConsoleCommandSender;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.permission.Permission;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.user.UserList;
import de.cubeisland.engine.core.user.UserList.UserListReader;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.logscribe.Log;
import de.cubeisland.engine.logscribe.LogLevel;
import org.bukkit.Difficulty;
import org.bukkit.DyeColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.inventory.ItemStack;

import static de.cubeisland.engine.core.contract.Contract.expect;

public class BukkitCommandManager extends DispatcherCommand implements CommandManager
{
    private final CommandInjector injector;
    private final ConsoleCommandSender consoleSender;
    private final Log commandLogger;
    private final ConfirmManager confirmManager;
    private final PaginationManager paginationManager;
    private final ProviderManager providerManager;
    private final CommandBuilder<BasicParametricCommand, CommandOrigin> builder;

    private Core core;

    @Override
    public CommandManagerDescriptor getDescriptor()
    {
        return (CommandManagerDescriptor)super.getDescriptor();
    }

    public BukkitCommandManager(BukkitCore core, CommandInjector injector)
    {
        super(new CommandManagerDescriptor());
        this.core = core;

        this.consoleSender = new ConsoleCommandSender(core);
        this.injector = injector;

        this.builder = new CompositeCommandBuilder<>(new ParametricCommandBuilder());

        this.commandLogger = core.getLogFactory().getLog(Core.class, "Commands");
        this.confirmManager = new ConfirmManager(this, core);
        this.paginationManager = new PaginationManager(core);

        this.providerManager = new ProviderManager();
        providerManager.getExceptionHandler().addHandler(new ExceptionHandler(core));

        providerManager.register(core, new PlayerCompleter(), User.class, OfflinePlayer.class);
        providerManager.register(core, new WorldCompleter(), World.class);
        providerManager.register(core, new ModuleCompleter(core), Module.class);
        providerManager.register(core, new PlayerListCompleter(core), PlayerListCompleter.class);

        providerManager.register(core, new ByteReader(), Byte.class, byte.class);
        providerManager.register(core, new ShortReader(), Short.class, short.class);
        providerManager.register(core, new IntReader(), Integer.class, int.class);
        providerManager.register(core, new LongReader(), Long.class, long.class);
        providerManager.register(core, new FloatReader(), Float.class, float.class);
        providerManager.register(core, new DoubleReader(), Double.class, double.class);

        providerManager.register(core, new BooleanReader(core), Boolean.class, boolean.class);
        providerManager.register(core, new EnchantmentReader(), Enchantment.class);
        providerManager.register(core, new ItemStackReader(), ItemStack.class);
        providerManager.register(core, new UserReader(core), User.class);
        providerManager.register(core, new CommandSenderReader(core), CommandSender.class);
        providerManager.register(core, new WorldReader(core), World.class);
        providerManager.register(core, new EntityTypeReader(), EntityType.class);
        providerManager.register(core, new DyeColorReader(), DyeColor.class);
        providerManager.register(core, new ProfessionReader(), Profession.class);
        providerManager.register(core, new OfflinePlayerReader(core), OfflinePlayer.class);
        providerManager.register(core, new EnvironmentReader(), Environment.class);
        providerManager.register(core, new DifficultyReader(), Difficulty.class);
        providerManager.register(core, new LogLevelReader(), LogLevel.class);

        UserListReader userListReader = new UserListReader();
        providerManager.register(core, userListReader, UserList.class);

        providerManager.register(core, userListReader, UserList.class);
    }

    @Override
    public ProviderManager getProviderManager()
    {
        return providerManager;
    }

    public CommandInjector getInjector()
    {
        return injector;
    }

    @Override
    public void removeCommand(String name, boolean completely)
    {
        this.injector.removeCommand(name, completely);
    }

    @Override
    public CommandBuilder<BasicParametricCommand, CommandOrigin> getCommandBuilder()
    {
        return this.builder;
    }

    @Override
    public void removeCommands(Module module)
    {
        this.injector.removeCommands(module);
    }

    @Override
    public void removeCommands()
    {
        this.injector.removeCommands();
    }

    @Override
    public void clean()
    {
        this.injector.shutdown();
    }

    @Override
    public boolean addCommand(CommandBase command)
    {
        if (command.getDescriptor() instanceof CubeDescriptor)
        {
            Module module = ((CubeDescriptor)command.getDescriptor()).getModule();
            Permission perm = module.getBasePermission().childWildcard("command");
            Permission childPerm = ((CubeDescriptor)command.getDescriptor()).getPermission();
            childPerm.setParent(perm);
            this.core.getPermissionManager().registerPermission(module, childPerm);
        }
        boolean b = super.addCommand(command);
        // TODO handle perm when removing cmd from parent
        this.injector.registerCommand(command); // register at sponge
        return b;
    }

    @Override
    public boolean runCommand(CommandSender sender, String commandLine)
    {
        expect(CubeEngine.isMainThread(), "Commands may only be called synchronously!");

        return this.injector.dispatchCommand(sender, commandLine);
    }

    @Override
    public ConsoleCommandSender getConsoleSender()
    {
        return this.consoleSender;
    }

    @Override
    public void logExecution(CommandSource sender, boolean ran, CommandBase command, String[] args)
    {
        CommandDescriptor descriptor = command.getDescriptor();
        if (descriptor instanceof CubeCommandDescriptor && ((CubeCommandDescriptor)descriptor).isLoggable())
        {
            if (ran)
            {
                this.commandLogger.debug("execute {} {}: {}", sender.getName(), descriptor.getName(), StringUtils.implode(" ", args));
            }
            else
            {
                this.commandLogger.debug("failed {} {}; {}", sender.getName(), descriptor.getName(), StringUtils.implode(" ", args));
            }
        }
    }

    @Override
    public void logTabCompletion(CommandSource sender, CommandBase command, String[] args)
    {
        CommandDescriptor descriptor = command.getDescriptor();
        if (descriptor instanceof CubeCommandDescriptor && ((CubeCommandDescriptor)descriptor).isLoggable())
        {
            this.commandLogger.debug("getSuggestions {} {}: {}", sender.getName(), descriptor.getName(), StringUtils.implode(" ", args));
        }
    }

    @Override
    public ConfirmManager getConfirmManager()
    {
        return this.confirmManager;
    }

    @Override
    public PaginationManager getPaginationManager()
    {
        return paginationManager;
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
