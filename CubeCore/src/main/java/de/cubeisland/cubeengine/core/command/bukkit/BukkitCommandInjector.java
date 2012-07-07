package de.cubeisland.cubeengine.core.command.bukkit;

import de.cubeisland.cubeengine.core.CubeCore;
import de.cubeisland.cubeengine.core.command.CommandInjector;
import de.cubeisland.cubeengine.core.command.CommandWrapper;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author Phillip Schichtel
 */
public class BukkitCommandInjector implements CommandInjector
{
    private CommandMap commandMap;
    private Map<String, Command> knownCommands;
    
    public void initialize(CubeCore core)
    {
        try
        {
            PluginManager pm = core.getPluginManager();
            this.commandMap = (CommandMap)pm.getClass().getField("commandMap").get(pm);
            this.knownCommands = (Map<String, Command>)this.commandMap.getClass().getField("knownCommands").get(this.commandMap);
        }
        catch (Exception e)
        {}
    }

    public void inject(CommandWrapper commandWrapper)
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
