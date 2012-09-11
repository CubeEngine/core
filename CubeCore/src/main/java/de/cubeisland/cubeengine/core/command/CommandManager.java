package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.core.BukkitDependend;
import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.module.Module;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang.Validate;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author Phillip Schichtel
 */
@BukkitDependend("Injects into Bukkit's command API")
public class CommandManager
{
    private CommandMap commandMap;
    private Map<String, Command> knownCommands;
    private static final String[] NO_PARENTS = {};

    public CommandManager(Core core)
    {
        try
        {
            PluginManager pm = ((Plugin)core).getServer().getPluginManager();
            for (Field field : pm.getClass().getDeclaredFields())
            {
                if (CommandMap.class.isAssignableFrom(field.getType()))
                {
                    this.commandMap = (CommandMap)field.get(pm);
                    break;
                }
            }
            for (Field field : this.commandMap.getClass().getDeclaredFields())
            {
                if (Map.class.isAssignableFrom(field.getType()))
                {
                    this.knownCommands = (Map<String, Command>)field.get(this.commandMap);
                    break;
                }
            }
        }
        catch (Exception e)
        {
        }
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
    
    public void registerCommand(Module module, ContainerCommand command)
    {
        
    }

    public void registerCommands(Module module, Object commandHolder)
    {
        this.registerCommands(module, commandHolder, NO_PARENTS);
    }
    
    public void registerCommand(ContainerCommand command, String... parents)
    {
        this.registerCommand(command, parents);
        
        String[] newParents = new String[parents.length + 1];
        newParents[0] = command.getName();
        System.arraycopy(parents, 0, newParents, 1, parents.length);
        
        this.registerCommands(command.getModule(), command, newParents);
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
            Class<?>[] params = method.getParameterTypes();
            if (params.length == 1 && params[0] == CommandContext.class)
            {
                continue;
            }

            commandAnnotation = method.getAnnotation(de.cubeisland.cubeengine.core.command.annotation.Command.class);
            if (commandAnnotation == null)
            {
                continue;
            }
            
            
            String[] names = commandAnnotation.names();
            Validate.notEmpty(names, "The given command has no names defined!");

            String name = names[0].trim().toLowerCase(Locale.ENGLISH);
            List<String> aliases;
            if (names.length > 1)
            {
                aliases = Arrays.asList(Arrays.copyOfRange(names, 1, names.length - 1));
                
                // make sure the aliases are lowercased
                for (int i = 0; i < aliases.size(); ++i)
                {
                    aliases.set(i, aliases.get(i).toLowerCase(Locale.ENGLISH));
                }
            }
            else
            {
                aliases = Collections.EMPTY_LIST;
            }

            this.registerCommand(new ReflectedCommand(
                module,
                commandHolder,
                method,
                commandAnnotation,
                name,
                commandAnnotation.desc(),
                commandAnnotation.usage(),
                aliases
            ));
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