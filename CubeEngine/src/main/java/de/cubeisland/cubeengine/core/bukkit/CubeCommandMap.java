package de.cubeisland.cubeengine.core.bukkit;

import de.cubeisland.cubeengine.core.util.StringUtils;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;

/**
 *
 * @author Phillip Schichtel
 */
public class CubeCommandMap extends SimpleCommandMap
{
    public CubeCommandMap(Server server, SimpleCommandMap oldMap)
    {
        super(server);
        for (Command command : oldMap.getCommands())
        {
            this.knownCommands.put(command.getName().toLowerCase(Locale.ENGLISH), command);
        }
    }
    
    public Map<String, Command> getKnownCommands()
    {
        return this.knownCommands;
    }

    @Override
    public boolean register(String label, String fallbackPrefix, Command command)
    {
        return super.register(label, fallbackPrefix, command);
    }

    @Override
    public Command getCommand(final String name)
    {
        return this.getCommand(name, true);
    }

    public Command getCommand(final String name, final boolean correct)
    {
        if (name == null)
        {
            return null;
        }
        Command cmd = super.getCommand(name.toLowerCase(Locale.ENGLISH));
        if (cmd == null && correct)
        {
            cmd = this.getCommand(StringUtils.getBestMatch(name, this.knownCommands.keySet(), 1), false);
        }
        
        return cmd;
    }
    
    @Override
    public Command getFallback(final String name)
    {
        if (name == null)
        {
            return null;
        }
        return super.getFallback(name);
    }
}
