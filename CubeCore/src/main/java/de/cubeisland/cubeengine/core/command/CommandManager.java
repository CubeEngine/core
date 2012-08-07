package de.cubeisland.cubeengine.core.command;

import de.cubeisland.cubeengine.BukkitDependend;
import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.module.Module;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
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
        {}
    }

    private void inject(Command command)
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
            parentCommand = this.getCommand(parent);
            if (parentCommand == null)
            {
                throw new IllegalArgumentException("Parent command '" + parent + "' is not registered!");
            }
        }

        if (parentCommand == null)
        {
            this.inject(command);
        }
        else
        {
            parentCommand.addSubCommand(command);
        }
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
            Class<?>[] params = method.getParameterTypes();
            if (params.length >= 1 && params[0] == CommandContext.class)
            {
                continue;
            }
            
            commandAnnotation = method.getAnnotation(de.cubeisland.cubeengine.core.command.annotation.Command.class);
            if (commandAnnotation == null)
            {
                continue;
            }
            
            String name = commandAnnotation.name().trim().toLowerCase(Locale.ENGLISH);
            if ("".equals(name))
            {
                name = method.getName();
            }
            
            this.registerCommand(new ReflectedCommand(
                module,
                commandHolder,
                method,
                commandAnnotation,
                name,
                commandAnnotation.desc(),
                commandAnnotation.usage(),
                Arrays.asList(commandAnnotation.aliases())
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
