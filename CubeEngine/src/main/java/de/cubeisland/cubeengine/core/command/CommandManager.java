package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.BukkitDependend;
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
import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author Phillip Schichtel
 */
@BukkitDependend("Injects into Bukkit's command API")
public class CommandManager
{
    private static final Logger LOGGER = CubeEngine.getLogger();
    
    private CommandMap commandMap;
    private Map<String, Command> knownCommands;
    private static final String[] NO_PARENTS = {};

    public CommandManager(Core core)
    {
        this.commandMap = BukkitUtils.getCommandMap(((Plugin)core).getServer().getPluginManager());
        this.knownCommands = BukkitUtils.getKnownCommandMap(this.commandMap);
    }

    private void injectIntoRoot(Command command)
    {
        this.commandMap.register(command.getLabel(), command);
    }

    public void remove(String name)
    {
        Command command = this.knownCommands.remove(name.toLowerCase());
        if (command != null)
        {
            command.unregister(this.commandMap);
        }
    }

    public void clear()
    {
        this.commandMap.clearCommands();
    }

    public void registerCommand(CubeCommand command)
    {
        this.registerCommand(command, NO_PARENTS);
    }

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
    }
    
    public void registerCommand(ContainerCommand command)
    {
        this.registerCommand(command, NO_PARENTS);
    }
    
    public void registerCommand(ContainerCommand command, String... parents)
    {
        this.registerCommand(command, parents);
        
        String[] newParents = new String[parents.length + 1];
        newParents[0] = command.getName();
        System.arraycopy(parents, 0, newParents, 1, parents.length);
        
        this.registerCommands(command.getModule(), command, newParents);
    }

    public void registerCommands(Module module, Object commandHolder)
    {
        this.registerCommands(module, commandHolder, NO_PARENTS);
    }

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
                LOGGER.warning("The method '" + commandHolder.getClass().getSimpleName() + "." + method.getName() + "' does not match the required method signature: public void " + method.getName() + "(CommandContext context)");
                continue;
            }
            
            
            String[] names = commandAnnotation.names();
            if (names.length == 0)
            {
                names = new String[] {method.getName()};
            }

            String name = names[0].trim().toLowerCase(Locale.ENGLISH);
            List<String> aliases = new ArrayList<String>(names.length - 1);
            for (int i = 1; i < aliases.size(); ++i)
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

    public void removeCommand(String... names)
    {
        for (String name : names)
        {
            this.knownCommands.remove(name);
        }
    }

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