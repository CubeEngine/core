package de.cubeisland.cubeengine.core.command;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 * This represents a base command
 *
 * @author Phillip Schichtel
 */
public class BaseCommand implements CommandExecutor
{
    private final Plugin plugin;
    private final PluginManager pm;
    private final Map<Object, Set<String>> objectCommandMap;
    private final Map<String, SubCommand> commands;
    private final Map<String, String> aliases;
    private String defaultCommand;

    private final Permission parentPermission;
    private final String permissionBase;

    public BaseCommand(Plugin plugin, String permissionBase)
    {
        this(plugin, permissionBase, PermissionDefault.OP);
    }

    public BaseCommand(Plugin plugin, String permissionBase, PermissionDefault parentDefault)
    {
        if (plugin == null)
        {
            throw new IllegalArgumentException("The plugin must not be null!");
        }
        if (permissionBase == null)
        {
            throw new IllegalArgumentException("The permission base must not be null!");
        }
        if (parentDefault == null)
        {
            throw new IllegalArgumentException("The parent permission default must not be null!");
        }
        this.plugin = plugin;
        this.pm = plugin.getServer().getPluginManager();
        this.objectCommandMap = new HashMap<Object, Set<String>>();
        this.commands = new HashMap<String, SubCommand>();
        this.aliases = new HashMap<String, String>();

        this.permissionBase = permissionBase;
        this.parentPermission = new Permission(permissionBase + "*", parentDefault);
        this.registerPermission(parentPermission);
        
        this.registerCommands(this);
        this.defaultCommand = "help";


    }


    public BaseCommand setDefaultCommand(String name)
    {
        SubCommand command = this.getCommand(name);
        if (command != null)
        {
            this.defaultCommand = command.getName();
        }
        return this;
    }

    public SubCommand getDefaultCommand()
    {
        return this.getCommandByName(this.defaultCommand);
    }

    public Permission getParentPermission()
    {
        return this.parentPermission;
    }

    public Plugin getPlugin()
    {
        return this.plugin;
    }

    public final BaseCommand registerCommands(Object commandContainer)
    {
        if (commandContainer == null)
        {
            throw new IllegalArgumentException("The command container must not be null!");
        }
        
        try
        {
            Set<String> registeredCommands = new HashSet<String>();
            Command annotation;
            RequiresPermission permissionAnnotation;
            String name;
            Permission permission;
            
            for (Method method : commandContainer.getClass().getDeclaredMethods())
            {
                annotation = method.getAnnotation(Command.class);
                if (annotation != null)
                {
                    name = annotation.name();
                    if ("".equals(name))
                    {
                        name = method.getName();
                    }
                    name = name.toLowerCase();
                    permission = null;
                    boolean addPermissionParent = false;
                    permissionAnnotation = method.getAnnotation(RequiresPermission.class);
                    if (permissionAnnotation != null)
                    {
                        String permissionName = permissionAnnotation.value();
                        if (permissionName.length() == 0)
                        {
                            permissionName = this.permissionBase + name;
                        }

                        permission = new Permission(permissionName, permissionAnnotation.def());
                        addPermissionParent = permissionAnnotation.addParent();
                        this.registerPermission(permission);
                        if (addPermissionParent)
                        {
                            permission.addParent(this.parentPermission, true);
                        }
                    }
                    try
                    {
                        this.commands.put(name, new SubCommand(commandContainer, method, name, annotation.aliases(), permission, addPermissionParent, annotation.usage()));
                        registeredCommands.add(name);

                        for (String alias : annotation.aliases())
                        {
                            this.aliases.put(alias, name);
                        }
                    }
                    catch (IllegalArgumentException e)
                    {
                        e.printStackTrace(System.err);
                    }
                }
            }

            this.objectCommandMap.put(commandContainer, registeredCommands);
        }
        catch (Throwable t)
        {}
        
        return this;
    }

    public BaseCommand unregisterCommands(Object commandContainer)
    {
        Set<String> commandsToRemove = this.objectCommandMap.get(commandContainer);
        if (commandsToRemove != null)
        {
            for (String command : commandsToRemove)
            {
                this.unregisterCommand(command);
            }
        }

        return this;
    }

    public BaseCommand unregisterCommand(String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("the name must not be null!");
        }

        this.commands.remove(name);
        for (Map.Entry<String, String> entry : this.aliases.entrySet())
        {
            if (name.equals(entry.getValue()))
            {
                this.aliases.remove(entry.getKey());
            }
        }
        return this;
    }

    public BaseCommand unregisterCommand(SubCommand command)
    {
        if (command == null)
        {
            throw new IllegalArgumentException("the command must not be null!");
        }
        this.unregisterCommand(command.getName());
        return this;
    }

    public Collection<SubCommand> getAllSubCommands()
    {
        return this.commands.values();
    }

    public SubCommand getCommandByName(String name)
    {
        if (name == null)
        {
            return null;
        }
        return this.commands.get(name.toLowerCase());
    }

    public SubCommand getCommand(String label)
    {
        if (label == null)
        {
            return null;
        }
        label = label.toLowerCase();
        String name = this.aliases.get(label);
        if (name != null)
        {
            return this.getCommandByName(name);
        }
        else
        {
            return this.getCommandByName(label);
        }
    }

    public BaseCommand clearCommands()
    {
        this.commands.clear();
        this.aliases.clear();
        this.objectCommandMap.clear();

        return this;
    }

    private void registerPermission(Permission permission)
    {
        if (permission != null)
        {
            try
            {
                this.pm.addPermission(permission);
            }
            catch(IllegalArgumentException e)
            {}
        }
    }

   

    //@Command
    @RequiresPermission("libminecraft.test")
    private void test(CommandSender sender, CommandArgs args)
    {
        sender.sendMessage("Params:");

        for (String param : args.getParams())
        {
            sender.sendMessage(" - '" + param + "'");
        }
        sender.sendMessage(" ");

        sender.sendMessage("Flags:");
        for (String flag : args.getFlags())
        {
            sender.sendMessage(" - '" + flag + "'");
        }
    }

    public boolean onCommand(CommandSender cs, org.bukkit.command.Command cmnd, String string, String[] strings)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
