package de.cubeisland.cubeengine.core.bukkit;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;

import de.cubeisland.cubeengine.core.command.AliasCommand;
import de.cubeisland.cubeengine.core.command.CommandFactory;
import de.cubeisland.cubeengine.core.command.CommandHolder;
import de.cubeisland.cubeengine.core.command.CommandManager;
import de.cubeisland.cubeengine.core.command.ConsoleCommandCompleter;
import de.cubeisland.cubeengine.core.command.CubeCommand;
import de.cubeisland.cubeengine.core.command.CommandSender;
import de.cubeisland.cubeengine.core.command.sender.ConsoleCommandSender;
import de.cubeisland.cubeengine.core.module.Module;

import gnu.trove.map.hash.THashMap;

public class BukkitCommandManager implements CommandManager
{
    private final Server server;
    final CubeCommandMap commandMap;
    private final Map<String, Command> knownCommands;
    private final Map<Class<? extends CubeCommand>, CommandFactory> commandFactories;
    private final ConsoleCommandCompleter completer;
    private final ConsoleCommandSender consoleSender;

    public BukkitCommandManager(BukkitCore core)
    {
        this.server = core.getServer();
        SimpleCommandMap oldMap = (SimpleCommandMap)BukkitUtils.getCommandMap(this.server);
        this.commandMap = new CubeCommandMap(core, this.server, oldMap);
        this.knownCommands = this.commandMap.getKnownCommands();
        this.commandFactories = new THashMap<Class<? extends CubeCommand>, CommandFactory>();
        BukkitUtils.swapCommandMap(this.commandMap);
        this.consoleSender = new ConsoleCommandSender(core, this.server.getConsoleSender());

        this.completer = new ConsoleCommandCompleter(core, this.commandMap);
        BukkitUtils.getConsoleReader(this.server).addCompleter(completer);
    }

    /**
     * Removes a command by its name
     *
     * @param name the name of the command to remove
     */
    public void removeCommands(String name)
    {
        Command command = this.knownCommands.remove(name.toLowerCase());
        if (command != null)
        {
            command.unregister(this.commandMap);
            if (command instanceof CubeCommand)
            {
                ((CubeCommand)command).onRemove();
            }
        }
    }

    /**
     * Removes all commands of a module
     *
     * @param module the module
     */
    public void removeCommands(Module module)
    {
        Command command;
        CubeCommand cubeCommand;
        Iterator<Command> iter = this.knownCommands.values().iterator();
        while (iter.hasNext())
        {
            command = iter.next();
            if (command instanceof CubeCommand)
            {
                cubeCommand = (CubeCommand)command;
                if (cubeCommand.getModule() == module)
                {
                    iter.remove();
                    command.unregister(this.commandMap);
                    cubeCommand.onRemove();
                }
                else
                {
                    this.removeSubCommands(module, cubeCommand);
                }
            }
        }
    }

    private void removeSubCommands(Module module, CubeCommand command)
    {
        if (!command.hasChildren())
        {
            return;
        }
        Iterator<CubeCommand> iter = command.getChildren().iterator();
        CubeCommand child;
        while (iter.hasNext())
        {
            child = iter.next();
            if (child.getModule() == module)
            {
                child.onRemove();
                iter.remove();
            }
            else
            {
                this.removeSubCommands(module, child);
            }
        }
    }

    /**
     * Removes all commands of the CubeEngine
     */
    public void removeCommands()
    {
        Iterator<Map.Entry<String, Command>> iter = this.knownCommands.entrySet().iterator();
        Entry<String, Command> entry;
        while (iter.hasNext())
        {
            entry = iter.next();
            if (entry.getValue() instanceof CubeCommand)
            {
                ((CubeCommand)entry.getValue()).onRemove();
                iter.remove();
            }
        }
    }

    /**
     * Clears the server's command map (unregisters all commands)
     */
    public void clean()
    {
        this.removeCommands();
        this.commandMap.clearCommands();
        this.commandFactories.clear();
        BukkitUtils.getConsoleReader(this.server).removeCompleter(this.completer);
    }

    /**
     * Registers a command
     *
     * @param command the command to register
     * @param parents the path under which the command should be registered
     */
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
            this.commandMap.register(command.getModule().getId(), command);
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

        // if the module is already enabled we have to reload the help map
        if (command.getModule().isEnabled())
        {
            BukkitUtils.reloadHelpMap();
        }
    }

    public void registerCommands(Module module, CommandHolder commandHolder, String... parents)
    {
        this.registerCommands(module, commandHolder, commandHolder.getCommandType(), parents);
    }

    /**
     * Registers all methods annotated as a command in the given command holder object
     *
     * @param module        the module to register them for
     * @param commandHolder the command holder containing the commands
     * @param parents       the path under which the command should be registered
     */
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

    public void registerCommandFactory(CommandFactory factory)
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

        Iterator<Entry<Class<? extends CubeCommand>, CommandFactory>> iter = this.commandFactories.entrySet().iterator();
        while (iter.hasNext())
        {
            if (iter.next().getValue().getClass() == clazz)
            {
                iter.remove();
            }
        }
    }

    /**
     * Gets a CubeCommand by its name
     *
     * @param name the name
     * @return the CubeCommand instance or null if not found
     */
    public CubeCommand getCommand(String name)
    {
        Command command = this.knownCommands.get(name);
        if (command != null && command instanceof CubeCommand)
        {
            return (CubeCommand)command;
        }
        return null;
    }

    public boolean runCommand(CommandSender sender, String commandLine)
    {
        return this.commandMap.dispatch(sender, commandLine);
    }

    @Override
    public ConsoleCommandSender getConsoleSender()
    {
        return this.consoleSender;
    }
}
