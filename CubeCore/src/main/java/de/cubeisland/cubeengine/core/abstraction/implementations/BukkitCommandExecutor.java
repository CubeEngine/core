package de.cubeisland.cubeengine.core.abstraction.implementations;

import de.cubeisland.cubeengine.core.abstraction.CommandExecutor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 *
 * @author CodeInfection
 */
public class BukkitCommandExecutor implements org.bukkit.command.CommandExecutor
{
    private final CommandExecutor executor;

    public BukkitCommandExecutor(CommandExecutor executor)
    {
        this.executor = executor;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        return this.executor.executeCommand(new BukkitCommandSender(sender), new BukkitCommand(command), label, args);
    }
}
