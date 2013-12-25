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

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.command.Command;

import de.cubeisland.engine.core.Core;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.bukkit.command.CommandInjector;
import de.cubeisland.engine.core.command.AliasCommand;
import de.cubeisland.engine.core.command.CommandFactory;
import de.cubeisland.engine.core.command.CommandHolder;
import de.cubeisland.engine.core.command.CommandManager;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.CubeCommand;
import de.cubeisland.engine.core.command.result.confirm.ConfirmManager;
import de.cubeisland.engine.core.command.sender.ConsoleCommandSender;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.util.StringUtils;
import de.cubeisland.engine.logging.Log;
import gnu.trove.map.hash.THashMap;

public class BukkitCommandManager implements CommandManager
{
    private final CommandInjector injector;
    private final Map<Class<? extends CubeCommand>, CommandFactory> commandFactories;
    private final ConsoleCommandSender consoleSender;
    private final Log commandLogger;
    private final ConfirmManager confirmManager;

    public BukkitCommandManager(BukkitCore core, CommandInjector injector)
    {
        this.consoleSender = new ConsoleCommandSender(core);
        this.injector = injector;
        this.commandFactories = new THashMap<>();

        this.commandLogger = core.getLogFactory().getLog(Core.class, "Commands");
        // TODO finish ConfirmManager
        this.confirmManager = new ConfirmManager(this, core);
    }

    public CommandInjector getInjector()
    {
        return injector;
    }

    public void removeCommand(String name, boolean completely)
    {
        this.injector.removeCommand(name, completely);
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
        this.commandFactories.clear();
    }

    public void registerCommand(CubeCommand command, String... parents)
    {
        if (command.getParent() != null)
        {
            throw new IllegalArgumentException("The given command is already registered!");
        }
        CubeCommand parentCommand = null;
        for (String parent : parents)
        {
            if (parentCommand == null)
            {
                parentCommand = this.getCommand(parent);
            }
            else
            {
                parentCommand = parentCommand.getChild(parent);
            }
            if (parentCommand == null)
            {
                throw new IllegalArgumentException("Parent command '" + parent + "' is not registered!");
            }
        }

        if (parentCommand == null)
        {
            this.injector.registerCommand(command);
        }
        else
        {
            parentCommand.addChild(command);
        }
        command.onRegister();
        if (!(command instanceof AliasCommand))
        {
            command.updateGeneratedPermission();
        }

        if (command instanceof CommandHolder)
        {
            String[] newParents = new String[parents.length + 1];
            newParents[parents.length] = command.getName();
            System.arraycopy(parents, 0, newParents, 0, parents.length);

            this.registerCommands(command.getModule(), (CommandHolder)command, newParents);
        }
    }

    public void registerCommands(Module module, CommandHolder commandHolder, String... parents)
    {
        this.registerCommands(module, commandHolder, commandHolder.getCommandType(), parents);
    }

    @SuppressWarnings("unchecked")
    public void registerCommands(Module module, Object commandHolder, Class<? extends CubeCommand> commandType, String... parents)
    {
        CommandFactory<? extends CubeCommand> commandFactory = this.getCommandFactory(commandType);
        if (commandFactory == null)
        {
            throw new IllegalArgumentException("The given command factory is not registered!");
        }
        for (CubeCommand command : commandFactory.parseCommands(module, commandHolder))
        {
            this.registerCommand(command, parents);
        }
    }

    public <T extends CubeCommand> void registerCommandFactory(CommandFactory<T> factory)
    {
        this.commandFactories.put(factory.getCommandType(), factory);
    }

    public CommandFactory getCommandFactory(Class<? extends CubeCommand> type)
    {
        return this.commandFactories.get(type);
    }

    public void removeCommandFactory(Class clazz)
    {
        this.commandFactories.remove(clazz);

        Iterator<Entry<Class<? extends CubeCommand>, CommandFactory>> it = this.commandFactories.entrySet().iterator();
        while (it.hasNext())
        {
            if (it.next().getValue().getClass() == clazz)
            {
                it.remove();
            }
        }
    }

    public CubeCommand getCommand(String name)
    {
        Command command = this.injector.getCommand(name);
        if (command != null && command instanceof CubeCommand)
        {
            return (CubeCommand)command;
        }
        return null;
    }

    public boolean runCommand(CommandSender sender, String commandLine)
    {
        assert CubeEngine.isMainThread(): "Commands may only be called synchronously!";

        return this.injector.dispatchCommand(sender, commandLine);
    }

    @Override
    public ConsoleCommandSender getConsoleSender()
    {
        return this.consoleSender;
    }

    @Override
    public void logExecution(CommandSender sender, CubeCommand command, String[] args)
    {
        if (command.isLoggable())
        {
            this.commandLogger.debug("execute {} {} {}", sender.getName(), command.getName(), StringUtils.implode(" ", args));
        }
    }

    @Override
    public void logTabCompletion(CommandSender sender, CubeCommand command, String[] args)
    {
        if (command.isLoggable())
        {
            this.commandLogger.debug("complete {} {} {}", sender.getName(), command.getName(), StringUtils.implode(" ", args));
        }
    }

    @Override
    public ConfirmManager getConfirmManager()
    {
        return this.confirmManager;
    }
}
