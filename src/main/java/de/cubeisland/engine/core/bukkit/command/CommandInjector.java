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
package de.cubeisland.engine.core.bukkit.command;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.VanillaCommand;
import org.bukkit.help.HelpTopic;

import de.cubeisland.engine.command.CommandBase;
import de.cubeisland.engine.command.CommandDescriptor;
import de.cubeisland.engine.command.CommandInvocation;
import de.cubeisland.engine.command.CommandSource;
import de.cubeisland.engine.command.Dispatcher;
import de.cubeisland.engine.command.UsageProvider;
import de.cubeisland.engine.command.old.IncorrectArgumentException;
import de.cubeisland.engine.command.old.IncorrectUsageException;
import de.cubeisland.engine.command.old.MissingParameterException;
import de.cubeisland.engine.command.parameter.reader.ReaderException;
import de.cubeisland.engine.core.CubeEngine;
import de.cubeisland.engine.core.bukkit.BukkitCore;
import de.cubeisland.engine.core.bukkit.BukkitCoreConfiguration;
import de.cubeisland.engine.core.command.CommandSender;
import de.cubeisland.engine.core.command.ModuleProvider;
import de.cubeisland.engine.core.command_old.exception.PermissionDeniedException;
import de.cubeisland.engine.core.contract.NotNull;
import de.cubeisland.engine.core.module.Module;
import de.cubeisland.engine.core.util.formatter.MessageType;
import gnu.trove.set.hash.THashSet;

import static de.cubeisland.engine.core.util.ReflectionUtils.findFirstField;
import static de.cubeisland.engine.core.util.ReflectionUtils.getFieldValue;
import static de.cubeisland.engine.core.util.formatter.MessageType.CRITICAL;
import static de.cubeisland.engine.core.util.formatter.MessageType.NEGATIVE;

/**
 * Injects CubeEngine commands directly into Bukkits CommandMap
 */
public class CommandInjector
{
    protected final BukkitCore core;
    private final Field commandMapField;
    private SimpleCommandMap commandMap;
    private Map<String, HelpTopic> helpTopicMap;
    private Field knownCommandField;

    @SuppressWarnings("unchecked")
    public CommandInjector(BukkitCore core)
    {
        this.core = core;

        this.commandMapField = findFirstField(core.getServer(), SimpleCommandMap.class);

        this.helpTopicMap = getFieldValue(core.getServer().getHelpMap(), "helpTopics", Map.class);
    }

    @SuppressWarnings("unchecked")
    protected synchronized Map<String, Command> getKnownCommands()
    {
        return getFieldValue(getCommandMap(), knownCommandField, Map.class);
    }

    protected final SimpleCommandMap getCommandMap()
    {
        SimpleCommandMap map = getFieldValue(this.core.getServer(), commandMapField, SimpleCommandMap.class);
        if (this.commandMap != map)
        {
            this.knownCommandField = findFirstField(map, Map.class, 1);
        }
        this.commandMap = map;
        return map;
    }

    public synchronized void registerCommand(CommandBase command)
    {
        WrappedCommand newCommand = new WrappedCommand(command);
        SimpleCommandMap commandMap = getCommandMap();
        Command old = this.getCommand(command.getDescriptor().getName());
        if (old != null)
        {
            BukkitCoreConfiguration config = this.core.getConfiguration();
            if (!config.commands.noOverride.contains(old.getLabel().toLowerCase(Locale.ENGLISH)))
            {
                // CE commands are more important :P
                this.removeCommand(old.getLabel(), false);
                String fallbackPrefix = core.getConfiguration().defaultFallback;
                if (old instanceof PluginCommand)
                {
                    fallbackPrefix = ((PluginCommand)old).getPlugin().getName();
                }
                else if (old instanceof VanillaCommand)
                {
                    fallbackPrefix = "vanilla";
                }
                else if (old instanceof WrappedCommand)
                {
                    fallbackPrefix = ((WrappedCommand)old).getModule().getId();
                }
                getKnownCommands().put(fallbackPrefix + ":" + newCommand.getLabel(), newCommand);
                newCommand.register(commandMap);
            }// sometimes they are not :(
        }
        commandMap.register(newCommand.getModule().getId(), newCommand);
        WrappedCommandHelpTopic topic = new WrappedCommandHelpTopic(newCommand);
        newCommand.setHelpTopic(topic);
        if (helpTopicMap != null)
        {
            this.helpTopicMap.put(topic.getName(), topic);
        }
    }

    public Command getCommand(String name)
    {
        return getCommandMap().getCommand(name);
    }

    public boolean dispatchCommand(CommandSender sender, String commandLine)
    {
        return getCommandMap().dispatch(sender, commandLine);
    }

    public void removeCommand(String name, boolean completely)
    {
        Map<String, Command> knownCommands = this.getKnownCommands();
        Command removed = knownCommands.remove(name.toLowerCase());
        if (removed != null)
        {
            Iterator<Entry<String, Command>> it = knownCommands.entrySet().iterator();
            Command next;
            boolean hasAliases = false;
            while (it.hasNext())
            {
                next = it.next().getValue();
                if (next == removed)
                {
                    hasAliases = true;
                    if (completely)
                    {
                        it.remove();
                    }
                }
            }
            if (hasAliases)
            {
                removed.unregister(getCommandMap());
            }
        }

        if (removed instanceof WrappedCommand)
        {
            if (helpTopicMap != null)
            {
                this.helpTopicMap.values().remove(((WrappedCommand)removed).getHelpTopic());
            }
        }
    }

    public void removeCommands(Module module)
    {
        for (Command command : new THashSet<>(getCommandMap().getCommands()))
        {
            if (command instanceof WrappedCommand)
            {
                if (((WrappedCommand)command).getModule() == module)
                {
                    this.removeCommand(command.getLabel(), true);
                }
                else
                {
                    this.removeSubCommands(module, ((WrappedCommand)command).getCommand());
                }
            }
        }
    }

    private void removeSubCommands(Module module, CommandBase command)
    {
        if (command instanceof Dispatcher)
        {
            Set<CommandBase> subCmds = ((Dispatcher)command).getCommands();
            if (subCmds.isEmpty())
            {
                return;
            }
            Iterator<CommandBase> it = subCmds.iterator();
            CommandBase subCmd;
            while (it.hasNext())
            {
                subCmd = it.next();
                if (subCmd.getDescriptor().valueFor(ModuleProvider.class) == module)
                {
                    it.remove();
                }
                else
                {
                    this.removeSubCommands(module, subCmd);
                }
            }
        }
    }

    public void removeCommands()
    {
        for (Command command : new THashSet<>(getCommandMap().getCommands()))
        {
            if (command instanceof WrappedCommand)
            {
                this.removeCommand(command.getLabel(), true);
            }
        }
    }

    public void shutdown()
    {
        this.removeCommands();
        this.commandMap = null;
        this.knownCommandField = null;
    }

    // TODO handle in cmd via property
    private void handleCommandException(@NotNull final CommandBase command, @NotNull final CommandInvocation invocation, Throwable t)
    {
        final CommandSource source = invocation.getCommandSource();
        final Module module = command.getDescriptor().valueFor(ModuleProvider.class);
        if (!(source instanceof CommandSender))
        {
            module.getLog().info("An unknown CommandSource ({}) caused an exception: {}", source.getClass().getName(), t.getMessage());
            return;
        }
        final CommandSender sender = (CommandSender)source;
        if (!CubeEngine.isMainThread())
        {
            final Throwable tmp = t;
            module.getCore().getTaskManager().callSync(new Callable<Void>()
            {
                @Override
                public Void call() throws Exception
                {
                    handleCommandException(command, invocation, tmp);
                    return null;
                }
            });
            return;
        }
        if (t instanceof InvocationTargetException || t instanceof ExecutionException)
        {
            t = t.getCause();
        }
        if (t instanceof MissingParameterException)
        {
            if (t.getMessage().isEmpty())
            {
                sender.sendTranslated(NEGATIVE, "The parameter {name#parameter} is missing!", ((MissingParameterException)t).getParamName());
            }
            else
            {
                sender.sendMessage(t.getMessage());
            }
        }
        else if (t instanceof IncorrectUsageException)
        {
            IncorrectUsageException e = (IncorrectUsageException)t;
            if (e.getMessage() != null)
            {
                sender.sendMessage(t.getMessage());
            }
            else
            {
                sender.sendTranslated(NEGATIVE, "That seems wrong...");
            }
            if (e.getDisplayUsage())
            {
                final String usage;

                CommandDescriptor descriptor = command.getDescriptor();
                usage = descriptor.valueFor(UsageProvider.class).generateUsage(invocation.getCommandSource(), descriptor);

                sender.sendTranslated(MessageType.NEUTRAL, "Proper usage: {message}", usage);
            }
        }
        else if (t instanceof ReaderException)
        {
            ReaderException e = (ReaderException)t;
            if (e.getMessage() != null)
            {
                sender.sendMessage(t.getMessage());
            }
            else
            {
                sender.sendTranslated(NEGATIVE, "Invalid Argument...");
            }
        }
        else if (t instanceof PermissionDeniedException)
        {
            PermissionDeniedException e = (PermissionDeniedException)t;
            if (e.getMessage() != null)
            {
                sender.sendMessage(e.getMessage());
            }
            else
            {
                sender.sendTranslated(NEGATIVE, "You're not allowed to do this!");
                sender.sendTranslated(NEGATIVE, "Contact an administrator if you think this is a mistake!");
            }
            sender.sendTranslated(NEGATIVE, "Missing permission: {name}", e.getPermission());
        }
        else if (t instanceof IncorrectArgumentException)
        {
            if (((IncorrectArgumentException)t).isNamedArgument())
            {
                sender.sendTranslated(NEGATIVE, "Invalid Argument for {input#named}: {input#reason}", ((IncorrectArgumentException)t).getName(), t.getCause().getMessage());
            }
            else
            {
                sender.sendTranslated(NEGATIVE, "Invalid Argument at {integer#index}: {input#reason}", ((IncorrectArgumentException)t).getIndex(), t.getCause().getMessage());
            }
        }
        else
        {
            sender.sendTranslated(CRITICAL, "An unknown error occurred while executing this command!");
            sender.sendTranslated(CRITICAL, "Please report this error to an administrator.");
            module.getLog().debug(t, t.getLocalizedMessage());
        }
    }
}
