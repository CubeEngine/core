package de.cubeisland.cubeengine.core.command.bukkit;

import de.cubeisland.cubeengine.core.command.CommandWrapper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Phillip Schichtel
 */
public class BukkitCommandWrapper extends Command implements CommandWrapper
{
    public BukkitCommandWrapper()
    {
        super("");
    }

    @Override
    public boolean execute(CommandSender cs, String string, String[] strings)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
