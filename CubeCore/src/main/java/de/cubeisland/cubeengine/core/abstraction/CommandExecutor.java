package de.cubeisland.cubeengine.core.abstraction;

/**
 *
 * @author CodeInfection
 */
public interface CommandExecutor
{
    public boolean executeCommand(CommandSender sender, Command command, String label, String args[]);
}
