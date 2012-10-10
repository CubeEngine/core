package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.CubeEngine;
import de.cubeisland.cubeengine.core.bukkit.BukkitUtils;
import de.cubeisland.cubeengine.core.command.annotation.Alias;
import de.cubeisland.cubeengine.core.module.Module;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Phillip Schichtel
 */
public class CommandManager
{
    private static final Logger LOGGER = CubeEngine.getLogger();
    
    private CommandMap commandMap;
    private Map<String, Command> knownCommands;

    public CommandManager(Core core)
    {
        this.commandMap = BukkitUtils.getCommandMap(((Plugin)core).getServer().getPluginManager());
        this.knownCommands = BukkitUtils.getKnownCommandMap(this.commandMap);
    }

    /**
     * Injects a command instance into the CommandMap
     *
     * @param command the command to inject
     */
    private void injectIntoRoot(Command command)
    {
        this.commandMap.register(command.getLabel(), command);
    }

    /**
     * Removes a command by its name
     *
     * @param name the name of the command to remove
     */
    public void unregister(String... names)
    {
        for (String name : names)
        {
            Command command = this.knownCommands.remove(name.toLowerCase());
            if (command != null)
            {
                command.unregister(this.commandMap);
            }
        }
    }
    
    /**
     * Unregisters all commands of a module
     *
     * @param module the module
     */
    public void unregister(Module module)
    {
        CubeCommand command;
        List<String> rootCommandsToRemove = new ArrayList<String>();
        for (Map.Entry<String, Command> entry : this.knownCommands.entrySet())
        {
            if (entry.getValue() instanceof CubeCommand)
            {
                command = (CubeCommand)entry.getValue();
                if (command.getModule() == module)
                {
                    rootCommandsToRemove.add(entry.getKey());
                }
                else
                {
                    // TODO go recursively through the child commands
                }
                this.unregister(entry.getKey());
            }
        }
        
        for (String name : rootCommandsToRemove)
        {
            this.unregister(name);
        }
    }
 
    /**
     * Unregisters all commands of the CubeEngine
     */
    public void unregister()
    {
        for (Map.Entry<String, Command> entry : this.knownCommands.entrySet())
        {
            if (entry.getValue() instanceof CubeCommand)
            {
                this.unregister(entry.getKey());
            }
        }
    }

    /**
     * Clears the server's command map (unregisters all commands)
     */
    public void clear()
    {
        this.commandMap.clearCommands();
    }

    /**
     * Registers a command
     *
     * @param command the command to register
     * @param parents the path under which the command should be registered
     */
    public void registerCommand(CubeCommand command, String... parents)
    {
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
            this.injectIntoRoot(command);
        }
        else
        {
            parentCommand.addChild(command);
        }
        
        if (command instanceof ContainerCommand)
        {
            String[] newParents = new String[parents.length + 1];
            newParents[0] = command.getName();
            System.arraycopy(parents, 0, newParents, 1, parents.length);

            this.registerCommands(command.getModule(), command, newParents);
        }
    }

    /**
     * Registers all methods annotated as a command in the given command holder object
     *
     * @param module the module to register them for
     * @param commandHolder the command holder containing the commands
     * @param parents the path under which the command should be registered
     */
    public void registerCommands(Module module, Object commandHolder, String... parents)
    {
        Method[] methods = commandHolder.getClass().getDeclaredMethods();
        de.cubeisland.cubeengine.core.command.annotation.Command commandAnnotation;
        for (Method method : methods)
        {
            if ((method.getModifiers() & Modifier.STATIC) == Modifier.STATIC)
            {
                continue;
            }

            commandAnnotation = method.getAnnotation(de.cubeisland.cubeengine.core.command.annotation.Command.class);
            if (commandAnnotation == null)
            {
                continue;
            }
            
            Class<?>[] params = method.getParameterTypes();
            if (params.length != 1 || params[0] != CommandContext.class)
            {
                LOGGER.log(Level.WARNING, "The method ''{0}.{1}'' does not match the required method signature: public void {2}(CommandContext context)", new Object[]{commandHolder.getClass().getSimpleName(), method.getName(), method.getName()});
                continue;
            }
            
            
            String[] names = commandAnnotation.names();
            if (names.length == 0)
            {
                names = new String[] {method.getName()};
            }

            String name = names[0].trim().toLowerCase(Locale.ENGLISH);
            List<String> aliases = new ArrayList<String>(names.length - 1);
            for (int i = 1; i < names.length; ++i)
            {
                aliases.add(names[i].toLowerCase(Locale.ENGLISH));
            }
            
            ReflectedCommand cmd = new ReflectedCommand(
                module,
                commandHolder,
                method,
                commandAnnotation,
                name,
                commandAnnotation.desc(),
                commandAnnotation.usage(),
                aliases
            );

            this.registerCommand(cmd);
            
            Alias aliasAnnotation = method.getAnnotation(Alias.class);
            if (aliasAnnotation != null && aliasAnnotation.names().length > 0)
            {
                names = aliasAnnotation.names();
                if (names.length > 1)
                {
                    aliases = new ArrayList<String>(names.length - 1);
                    for (int i = 1; i < names.length; ++i)
                    {
                        aliases.add(names[i]);
                    }
                }
                else
                {
                    aliases = Collections.<String>emptyList();
                }
                this.registerCommand(new AliasCommand(names[0], aliases, cmd));
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
}