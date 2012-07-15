package de.cubeisland.cubeengine.core.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.CommandHolder;
import de.cubeisland.cubeengine.core.command.annotation.Command;

/**
 *
 * @author Phillip Schichtel
 */
public class CoreCommands implements CommandHolder
{
    @Command(
        aliases = {"mm"},
        desc = "This command is used to manage the modules."
    )
    public void modules(CommandContext context)
    {
        
    }
}
