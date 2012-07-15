package de.cubeisland.cubeengine.core.bukkit;

import de.cubeisland.cubeengine.core.command.CommandHolder;
import de.cubeisland.cubeengine.core.command.CommandWrapper;
import gnu.trove.map.hash.THashMap;
import java.lang.reflect.Method;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Phillip Schichtel
 */
public class BukkitCommandWrapper extends Command implements CommandWrapper
{
    private final THashMap<String, BukkitSubCommand> subCommands;

    public BukkitCommandWrapper(BukkitCommandManager commandManager, Method method, String name)
    {
        super("");
        this.subCommands = new THashMap<String, BukkitSubCommand>();
    }

    @Override
    public boolean execute(CommandSender cs, String string, String[] strings)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void registerSubCommands(CommandHolder commandHolder)
    {}
}
