package de.cubeisland.cubeengine.auctions;

import java.util.Collection;
import java.util.HashMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 * This class is the base for all sub commands
 *
 * @author Phillip Schichtel
 */
public class BaseCommand implements CommandExecutor
{
    public final String permissinBase;
    private final Plugin plugin;
    private final PluginManager pm;
    private final HashMap<String, AbstractCommand> allSubCommands;
    private final HashMap<String, AbstractCommand> subCommands;
    private final Permission parentPermission;
    private String defaultCommand;
    private String label;

    public BaseCommand(Plugin plugin)
    {
        this.plugin = plugin;
        this.pm = plugin.getServer().getPluginManager();
        this.permissinBase = this.plugin.getDescription().getName().toLowerCase() + ".commands.";
        this.defaultCommand = null;
        this.allSubCommands = new HashMap<String, AbstractCommand>();
        this.subCommands = new HashMap<String, AbstractCommand>();
        this.parentPermission = new Permission(permissinBase + "*", PermissionDefault.OP);
        try
        {
            this.pm.addPermission(this.parentPermission);
        }
        catch (IllegalArgumentException e)
        {
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        this.label = label;
        if (args.length > 0)
        {
            AbstractCommand cmd = this.allSubCommands.get(args[0].toLowerCase());
            if (cmd != null)
            {
                return executeSub(sender, cmd, args);
            }
        }

        if (this.defaultCommand != null)
        {
            return executeSub(sender, this.allSubCommands.get(this.defaultCommand),new String[]{this.defaultCommand} );
        }

        sender.sendMessage("Available commands:");
        for (String commandLabel : this.allSubCommands.keySet())
        {
            sender.sendMessage(" - " + commandLabel);
        }

        return true;
    }

    private boolean executeSub(CommandSender sender, AbstractCommand command, String[] args)
    {
        /*
         * 
        if (!sender.hasPermission(command.getPermission()))
        {
            sender.sendMessage(ChatColor.RED + "Permission denied!");
            return true;
        }
        * 
        */
        return command.execute(sender, new CommandArgs(args));
    }

    /**
     * Registeres a sub command
     *
     * @param command the command to register
     * @return fluent interface
     */
    public BaseCommand registerSubCommand(AbstractCommand command)
    {
        for (String subLabel : command.getLabels())
        {
            this.allSubCommands.put(subLabel.toLowerCase(), command);
        }
        this.subCommands.put(command.getLabel().toLowerCase(), command);
        final Permission perm = command.getPermission();
        try
        {
            this.pm.addPermission(perm);
        }
        catch (IllegalArgumentException e)
        {
        }
        perm.addParent(this.parentPermission, true);
        perm.recalculatePermissibles();
        return this;
    }

    /**
     * Unregisteres a sub command
     *
     * @param name the name of the sub command to unregister
     * @return fluent interface
     */
    public BaseCommand unregisterSubCommand(String name)
    {
        this.allSubCommands.remove(name);
        if (name.equals(this.defaultCommand))
        {
            this.defaultCommand = null;
        }
        return this;
    }

    /**
     * Unregisteres all sub commands
     *
     * @return fluent interface
     */
    public BaseCommand unregisterAllSubCommands()
    {
        this.allSubCommands.clear();
        return this;
    }

    /**
     * Sets the default command
     *
     * @param name the name of a registered command
     * @return fluent interface
     */
    public BaseCommand setDefaultCommand(String name)
    {
        name = name.toLowerCase();
        if (this.allSubCommands.containsKey(name))
        {
            this.defaultCommand = name;
        }
        return this;
    }

    /**
     * Returns a collection of the registered sub commands with alias
     *
     * @return the commands
     */
    public Collection<AbstractCommand> getAllRegisteredCommands()
    {
        return this.allSubCommands.values();
    }
    
    /**
     * Returns a collection of the registered sub commands without alias
     *
     * @return the commands
     */
    public Collection<AbstractCommand> getRegisteredCommands()
    {
        return this.subCommands.values();
    }

    /**
     * Returns the label of this command
     * This is only used by sub commands
     *
     * @return
     */
    public String getLabel()
    {
        return this.label;
    }

    /**
     * Returns the corresponding plugin
     *
     * @return this plugin
     */
    public Plugin getPlugin()
    {
        return this.plugin;
    }
}
