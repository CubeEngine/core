package de.cubeisland.cubeengine.core.bukkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Phillip Schichtel
 */
public class CommandWrapper extends Command
{
    public CommandWrapper()
    {
        super("");
    }

    @Override
    public boolean execute(CommandSender cs, String string, String[] strings)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
