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
package de.cubeisland.cubeengine.core.bukkit;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Server;
import org.bukkit.command.Command;

import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.bukkit.command.CommandBackend;
import de.cubeisland.cubeengine.core.command.AliasCommand;
import de.cubeisland.cubeengine.core.command.CommandFactory;
import de.cubeisland.cubeengine.core.command.CommandHolder;
import de.cubeisland.cubeengine.core.command.CommandManager;
import de.cubeisland.cubeengine.core.command.CommandSender;
import de.cubeisland.cubeengine.core.command.CubeCommand;
import de.cubeisland.cubeengine.core.command.result.confirm.ConfirmManager;
import de.cubeisland.cubeengine.core.command.sender.ConsoleCommandSender;
import de.cubeisland.cubeengine.core.module.Module;
import de.cubeisland.cubeengine.core.util.StringUtils;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import gnu.trove.map.hash.THashMap;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import static de.cubeisland.cubeengine.core.logger.LogLevel.INFO;
import static de.cubeisland.cubeengine.core.logger.LogLevel.WARNING;

public class BukkitCommandManager implements CommandManager
{
    private final Server server;
    private final BukkitCore core;
    private final CommandBackend commandBackend;
    private final Map<Class<? extends CubeCommand>, CommandFactory> commandFactories;
    private final ConsoleCommandSender consoleSender;
    private final Logger commandLogger;
    private final ConfirmManager confirmManager;

    public BukkitCommandManager(BukkitCore core, CommandBackend commandBackend)
    {
        this.core = core;
        this.server = core.getServer();
        this.consoleSender = new ConsoleCommandSender(core);
        this.commandBackend = commandBackend;
        this.commandFactories = new THashMap<Class<? extends CubeCommand>, CommandFactory>();

        this.commandLogger = (Logger) LoggerFactory.getLogger("cubeengine.commands");
        try
        {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            FileAppender<ILoggingEvent> fileAppender = new FileAppender<ILoggingEvent>();
            fileAppender.setFile(new File(core.getFileManager().getLogDir(), this.commandLogger.getName()).getPath());
            PatternLayout pl = new PatternLayout();
            pl.setPattern("%date{yyyy-MM-dd HH:mm:ss} %m%n");
            pl.setContext(context);
            pl.start();
            fileAppender.setLayout(pl);
            fileAppender.setContext(context);
            fileAppender.start();
            commandLogger.addAppender(fileAppender);
        }
        catch (Exception e)
        {
            core.getLog().log(WARNING, "Failed to create the command log!", e);
        }

        this.confirmManager = new ConfirmManager(this, core);
    }

    public void removeCommand(String name, boolean completely)
    {
        this.commandBackend.removeCommand(name, completely);
    }

    public void removeCommands(Module module)
    {
        this.commandBackend.removeCommands(module);
    }

    public void removeCommands()
    {
        this.commandBackend.removeCommands();
    }

    public void clean()
    {
        this.commandBackend.shutdown();
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
            this.commandBackend.registerCommand(command);
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
        Command command = this.commandBackend.getCommand(name);
        if (command != null && command instanceof CubeCommand)
        {
            return (CubeCommand)command;
        }
        return null;
    }

    public boolean runCommand(CommandSender sender, String commandLine)
    {
        assert CubeEngine.isMainThread(): "Commands may only be called synchronously!";

        return this.commandBackend.dispatchCommand(sender, commandLine);
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
            this.commandLogger.info("execute {} {} {}", sender.getName(), command.getName(), StringUtils.implode(" ", args));
        }
    }

    @Override
    public void logTabCompletion(CommandSender sender, CubeCommand command, String[] args)
    {
        if (command.isLoggable())
        {
            this.commandLogger.info("complete {} {} {}", sender.getName(), command.getName(), StringUtils.implode(" ", args));
        }
    }

    @Override
    public ConfirmManager getConfirmManager()
    {
        return this.confirmManager;
    }
}
