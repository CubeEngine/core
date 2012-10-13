package de.cubeisland.cubeengine.core.commands;

import de.cubeisland.cubeengine.core.command.CommandContext;
import de.cubeisland.cubeengine.core.command.annotation.Command;
import de.cubeisland.cubeengine.core.command.annotation.Flag;

/**
 *
 * @author Phillip Schichtel
 */
public class CoreCommands
{
    @Command(
        names =
    {
        "modules", "mm"
    },
    desc = "This command is used to manage the modules.",
    flags =
    {
        @Flag(name = "a", longName = "all"),
        @Flag(name = "f", longName = "force")
    },
    checkPerm = false)
    public void modules(CommandContext context)
    {
    }
}