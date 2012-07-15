package de.cubeisland.cubeengine.core.bukkit;

import de.cubeisland.cubeengine.core.Core;
import de.cubeisland.cubeengine.core.command.CommandManager;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author Phillip Schichtel
 */
public class BukkitCommandManager implements CommandManager
{
    private CommandMap commandMap;
    private Map<String, Command> knownCommands;

    public BukkitCommandManager(Core core)
    {
        try
        {
            PluginManager pm = ((Plugin)core.getBootstrapper()).getServer().getPluginManager();
            this.commandMap = (CommandMap)pm.getClass().getField("commandMap").get(pm);
            this.knownCommands = (Map<String, Command>)this.commandMap.getClass().getField("knownCommands").get(this.commandMap);
        }
        catch (Exception e)
        {}
    }

    public void inject(BukkitCommandWrapper commandWrapper)
    {
        Command command = (Command)(BukkitCommandWrapper)commandWrapper;
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
}
