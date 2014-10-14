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
package de.cubeisland.engine.core.bukkit;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Difficulty;
import org.bukkit.DyeColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.inventory.ItemStack;

import de.cubeisland.engine.command.CommandBase;
import de.cubeisland.engine.command.CommandBuilder;
import de.cubeisland.engine.command.Dispatcher;
import de.cubeisland.engine.command.DispatcherCommand;
import de.cubeisland.engine.command.completer.Completer;
import de.cubeisland.engine.command.methodic.BasicMethodicCommand;
import de.cubeisland.engine.command.methodic.CompositeCommandBuilder;
import de.cubeisland.engine.command.methodic.MethodicBuilder;
import de.cubeisland.engine.command.parameter.reader.ReaderManager;
import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.bukkit.command.CommandInjector;
import de.cubeisland.engine.core.command.CommandOrigin;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.MethodicCommandBuilder;
import de.cubeisland.engine.core.command.ParametricCommandBuilder;
import de.cubeisland.engine.core.command.property.Loggable;
import de.cubeisland.engine.core.command_old.CommandManager;
import de.cubeisland.engine.core.command_old.parameterized.completer.PlayerCompleter;
import de.cubeisland.engine.core.command_old.parameterized.completer.PlayerListCompleter;
import de.cubeisland.engine.core.command_old.parameterized.completer.WorldCompleter;
import de.cubeisland.engine.core.command_old.readers.BooleanReader;
import de.cubeisland.engine.core.command_old.readers.ByteReader;
import de.cubeisland.engine.core.command_old.readers.DifficultyReader;
import de.cubeisland.engine.core.command_old.readers.DoubleReader;
import de.cubeisland.engine.core.command_old.readers.DyeColorReader;
import de.cubeisland.engine.core.command_old.readers.EnchantmentReader;
import de.cubeisland.engine.core.command_old.readers.EntityTypeReader;
import de.cubeisland.engine.core.command_old.readers.EnvironmentReader;
import de.cubeisland.engine.core.command_old.readers.FloatReader;
import de.cubeisland.engine.core.command_old.readers.IntReader;
import de.cubeisland.engine.core.command_old.readers.ItemStackReader;
import de.cubeisland.engine.core.command_old.readers.LogLevelReader;
import de.cubeisland.engine.core.command_old.readers.LongReader;
import de.cubeisland.engine.core.command_old.readers.OfflinePlayerReader;
import de.cubeisland.engine.core.command_old.readers.ProfessionReader;
import de.cubeisland.engine.core.command_old.readers.ShortReader;
import de.cubeisland.engine.core.command_old.readers.UserReader;
import de.cubeisland.engine.core.command_old.readers.WorldReader;
import de.cubeisland.engine.core.command_old.readers.WorldTypeReader;
import de.cubeisland.engine.core.command_old.result.confirm.ConfirmManager;
import de.cubeisland.engine.core.command_old.result.paginated.PaginationManager;
import de.cubeisland.engine.core.command_old.sender.ConsoleCommandSender;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.user.User;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.logging.Log;
import de.cubeisland.engine.logging.LogLevel;

import static de.cubeisland.engine.core.contract.Contract.expect;

public class BukkitCommandManager extends DispatcherCommand implements CommandManager
{
    private final CommandInjector injector;
    private final ConsoleCommandSender consoleSender;
    private final Log commandLogger;
    private final ConfirmManager confirmManager;
    private final PaginationManager paginationManager;
    private final ReaderManager readerManager;
    private final CommandBuilder<BasicMethodicCommand, CommandOrigin> builder;

    private Map<Class, Completer> completers = new HashMap<>();

    public BukkitCommandManager(BukkitCore core, CommandInjector injector)
    {
        this.consoleSender = new ConsoleCommandSender(core);
        this.injector = injector;

        this.builder = new CompositeCommandBuilder<>(new MethodicCommandBuilder(), new ParametricCommandBuilder());

        this.commandLogger = core.getLogFactory().getLog(Core.class, "Commands");
        // TODO finish ConfirmManager
        this.confirmManager = new ConfirmManager(this, core);
        this.paginationManager = new PaginationManager(core);

        this.registerDefaultCompleter(new PlayerCompleter(), User.class);
        this.registerDefaultCompleter(new WorldCompleter(), World.class);

        this.registerDefaultCompleter(new PlayerListCompleter(core), PlayerListCompleter.class);

        this.readerManager = new ReaderManager();

        // TODO Manager to unregister reader from modules
        readerManager.registerReader(new ByteReader(), Byte.class, byte.class);
        readerManager.registerReader(new ShortReader(), Short.class, short.class);
        readerManager.registerReader(new IntReader(), Integer.class, int.class);
        readerManager.registerReader(new LongReader(), Long.class, long.class);
        readerManager.registerReader(new FloatReader(), Float.class, float.class);
        readerManager.registerReader(new DoubleReader(), Double.class, double.class);

        readerManager.registerReader(new BooleanReader(core), Boolean.class, boolean.class);
        readerManager.registerReader(new EnchantmentReader(), Enchantment.class);
        readerManager.registerReader(new ItemStackReader(), ItemStack.class);
        readerManager.registerReader(new UserReader(core), User.class);
        readerManager.registerReader(new WorldReader(core), World.class);
        readerManager.registerReader(new EntityTypeReader(), EntityType.class);
        readerManager.registerReader(new DyeColorReader(), DyeColor.class);
        readerManager.registerReader(new ProfessionReader(), Profession.class);
        readerManager.registerReader(new OfflinePlayerReader(core), OfflinePlayer.class);
        readerManager.registerReader(new EnvironmentReader(), Environment.class);
        readerManager.registerReader(new WorldTypeReader(), WorldType.class);
        readerManager.registerReader(new DifficultyReader(), Difficulty.class);
        readerManager.registerReader(new LogLevelReader(), LogLevel.class);
    }

    @Override
    public ReaderManager getReaderManager()
    {
        return readerManager;
    }

    public CommandInjector getInjector()
    {
        return injector;
    }

    public void removeCommand(String name, boolean completely)
    {
        this.injector.removeCommand(name, completely);
    }

    @Override
    public CommandBuilder<BasicMethodicCommand, CommandOrigin> getCommandBuilder()
    {
        return this.builder;
    }

    public void removeCommands(Module module)
    {
        this.injector.removeCommands(module);
    }

    public void removeCommands()
    {
        this.injector.removeCommands();
    }

    public void clean()
    {
        this.injector.shutdown();
    }

    @Override
    public boolean addCommand(CommandBase command)
    {
        boolean b = super.addCommand(command);
        // TODO perm registration
        // TODO handle perm when removing cmd from parent
        // TODO if container add subcmds (prob. not here)
        this.injector.registerCommand(command); // register at bukkit
        return b;
    }

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
    public void logExecution(CommandSender sender, CommandBase command, String[] args)
    {
        if (command.getDescriptor().valueFor(Loggable.class))
        {
            this.commandLogger.debug("execute {} {} {}", sender.getName(), command.getDescriptor().getName(), StringUtils.implode(" ", args));
        }
    }

    @Override
    public void logTabCompletion(CommandSender sender, CommandBase command, String[] args)
    {
        if (command.getDescriptor().valueFor(Loggable.class))
        {
            this.commandLogger.debug("complete {} {} {}", sender.getName(), command.getDescriptor().getName(), StringUtils.implode(" ", args));
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
    public Completer getDefaultCompleter(Class... types)
    {
        for (Class type : types)
        {
            Completer completer = this.completers.get(type);
            if (completer != null)
            {
                return completer;
            }
        }
        return null;
    }

    @Override
    public void registerDefaultCompleter(Completer completer, Class... types)
    {
        for (Class type : types)
        {
            this.completers.put(type, completer);
        }
    }

    @Override
    public Dispatcher getBaseDispatcher()
    {
        return this;
    }


    /**
     * Creates {@link de.cubeisland.engine.command.methodic.BasicMethodicCommand} for all methods annotated as a command
     * in the given commandHolder and add them to the given dispatcher
     *
     * @param dispatcher    the dispatcher to add the commands to
     * @param module        the module owning the commands
     * @param commandHolder the command holder containing the command-methods
     */
    @SuppressWarnings("unchecked")
    public void addCommands(Dispatcher dispatcher, Module module, Object commandHolder)
    {
        for (Method method : MethodicBuilder.getMethods(commandHolder.getClass()))
        {
            BasicMethodicCommand cmd = module.getCore().getCommandManager().getCommandBuilder().buildCommand(
                new CommandOrigin(method, commandHolder, module));
            if (cmd != null)
            {
                dispatcher.addCommand(cmd);
            }
        }
    }
}